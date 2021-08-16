/*
 * MIT License
 *
 * Copyright (c) 2021 EPAM Systems
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.epam.catgenome.manager.externaldb.homologene;

import com.epam.catgenome.component.MessageCode;
import com.epam.catgenome.entity.externaldb.homologene.EntryGenesXML;
import com.epam.catgenome.entity.externaldb.homologene.Gene;
import com.epam.catgenome.entity.externaldb.homologene.GeneXML;
import com.epam.catgenome.entity.externaldb.homologene.HomologeneEntry;
import com.epam.catgenome.entity.externaldb.homologene.HomologeneEntrySetXML;
import com.epam.catgenome.entity.externaldb.homologene.HomologeneEntryXML;
import com.epam.catgenome.manager.blast.BlastTaxonomyManager;
import com.epam.catgenome.manager.blast.dto.BlastTaxonomy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.catgenome.component.MessageHelper.getMessage;
import static com.epam.catgenome.util.Utils.DEFAULT_PAGE_SIZE;
import static org.apache.commons.lang3.StringUtils.join;

@Service
@Slf4j
public class HomologeneManager {

    private static final String TERM_SPLIT_TOKEN = " ";
    private static final String GENE_FIELDS_LINE_DELIMITER = "|";

    @Value("${homologene.index.directory}")
    private String indexDirectory;

    @Autowired
    BlastTaxonomyManager taxonomyManager;

    public HomologeneSearchResult<HomologeneEntry> searchHomologenes(final HomologeneSearchRequest query)
            throws IOException {
        final List<HomologeneEntry> entries = new ArrayList<>();
        final HomologeneSearchResult<HomologeneEntry> searchResult = new HomologeneSearchResult<>();
        try (Directory index = new SimpleFSDirectory(Paths.get(indexDirectory));
             IndexReader indexReader = DirectoryReader.open(index)) {

            final int page = (query.getPage() == null || query.getPage() <= 0) ? 1 : query.getPage();
            final int pageSize = (query.getPageSize() == null || query.getPage() <= 0) ? DEFAULT_PAGE_SIZE
                    : query.getPageSize();
            final int hits = page * pageSize;

            IndexSearcher searcher = new IndexSearcher(indexReader);
            TopDocs topDocs = searcher.search(buildSearchQuery(query.getQuery()), hits);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            final int from = (page - 1) * pageSize;
            final int to = Math.min(from + pageSize, scoreDocs.length);

            final Set<Long> taxIds = new HashSet<>();
            for (int i = from; i < to; i++) {
                Document doc = searcher.doc(scoreDocs[i].doc);
                List<Gene> genes = getEntryGenes(doc) == null ? Collections.emptyList()
                        : convertGenes(getEntryGenes(doc));
                List<Long> geneTaxIds = genes.stream().map(Gene::getTaxId).collect(Collectors.toList());
                taxIds.addAll(geneTaxIds);
            }
            final List<BlastTaxonomy> organisms = taxonomyManager.searchOrganismsByIds(taxIds);

            for (int i = from; i < to; i++) {
                Document doc = searcher.doc(scoreDocs[i].doc);
                List<Gene> genes = getEntryGenes(doc) == null ? Collections.emptyList()
                        : convertGenes(getEntryGenes(doc));
                setSpeciesName(genes, organisms);
                entries.add(
                    HomologeneEntry.builder()
                        .groupId(getGroupId(doc))
                        .taxId(getTaxId(doc))
                        .version(getVersion(doc))
                        .caption(getCaption(doc))
                        .genes(genes)
                        .build()
                );
            }
            searchResult.setItems(entries);
            searchResult.setTotalCount(topDocs.totalHits);
        }
        return searchResult;
    }

    private void setSpeciesName(final List<Gene> genes, List<BlastTaxonomy> organisms) {
        for (Gene gene: genes) {
            BlastTaxonomy organism = organisms
                    .stream()
                    .filter(o -> o.getTaxId().equals(gene.getTaxId()))
                    .findFirst()
                    .orElse(null);
            if (organism != null) {
                gene.setSpeciesCommonName(organism.getCommonName());
                gene.setSpeciesScientificName(organism.getScientificName());
            }
        }
    }

    public void importHomologeneDatabase(final String databasePath) throws IOException, ParseException {
        File file = new File(databasePath);
        Assert.isTrue(file.isFile() && file.canRead(), getMessage(MessageCode.RESOURCE_NOT_FOUND));
        try (Directory index = new SimpleFSDirectory(Paths.get(indexDirectory));
             IndexWriter writer = new IndexWriter(
                     index, new IndexWriterConfig(new StandardAnalyzer())
                     .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND))) {
            writer.deleteAll();
            for (HomologeneEntryXML entry: readHomologenes(databasePath)) {
                addDoc(writer, entry);
            }
        }
    }

    private Query buildSearchQuery(final String terms) {
        final BooleanQuery.Builder mainBuilder = new BooleanQuery.Builder();
        for (String term: terms.split(TERM_SPLIT_TOKEN)) {
            mainBuilder.add(
                new BooleanQuery.Builder()
                    .add(buildPrefixQuery(term, IndexFields.QUERY_FIELDS), BooleanClause.Occur.SHOULD)
                    .build(),
                BooleanClause.Occur.MUST
            );
        }
        return mainBuilder.build();
    }

    private PrefixQuery buildPrefixQuery(final String term, final IndexFields field) {
        return new PrefixQuery(new Term(field.getFieldName(), term.toLowerCase()));
    }

    private long getGroupId(final Document doc) {
        return Long.parseLong(doc.getField(IndexFields.GROUP_ID.getFieldName()).stringValue());
    }

    private long getVersion(final Document doc) {
        return Long.parseLong(doc.getField(IndexFields.VERSION.getFieldName()).stringValue());
    }

    private String getCaption(final Document doc) {
        return doc.getField(IndexFields.CAPTION.getFieldName()).stringValue();
    }

    private long getTaxId(final Document doc) {
        return Long.parseLong(doc.getField(IndexFields.TAX_ID.getFieldName()).stringValue());
    }

    private EntryGenesXML getEntryGenes(final Document doc) {
        return doc.getField(IndexFields.GENES.getFieldName()) == null ? null
                : deserializeGenes(doc.getField(IndexFields.GENES.getFieldName()).stringValue());
    }

    @Getter
    private enum IndexFields {
        GROUP_ID("groupId"),
        VERSION("version"),
        CAPTION("caption"),
        TAX_ID("taxId"),
        QUERY_FIELDS("queryFields"),
        GENES("genes");

        private final String fieldName;

        IndexFields(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    public List<HomologeneEntryXML> readHomologenes(final String path) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(HomologeneEntrySetXML.class);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            InputSource inputSource = new InputSource(path);
            SAXSource source = new SAXSource(xmlReader, inputSource);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            HomologeneEntrySetXML homologeneEntrySet = (HomologeneEntrySetXML) unmarshaller.unmarshal(source);
            return homologeneEntrySet.getHomologeneEntrySetEntries().getHomologeneEntries();
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private static void addDoc(final IndexWriter writer, final HomologeneEntryXML entry) throws IOException {
        final Document doc = new Document();

        doc.add(new StringField(IndexFields.GROUP_ID.getFieldName(),
                String.valueOf(entry.getGroupId()), Field.Store.YES));

        doc.add(new StringField(IndexFields.VERSION.getFieldName(),
                String.valueOf(entry.getVersion()), Field.Store.YES));

        doc.add(new StringField(IndexFields.CAPTION.getFieldName(),
                String.valueOf(entry.getCaption()), Field.Store.YES));

        doc.add(new StringField(IndexFields.TAX_ID.getFieldName(),
                String.valueOf(entry.getTaxId()), Field.Store.YES));

        if (entry.getEntryGenes() != null) {
            doc.add(new TextField(IndexFields.GENES.getFieldName(),
                    serializeGeneEntries(entry.getEntryGenes()), Field.Store.YES));
        }

        if (entry.getEntryGenes() != null) {
            doc.add(new TextField(IndexFields.QUERY_FIELDS.getFieldName(),
                    serializeQueryFields(entry.getEntryGenes()), Field.Store.YES));
        }
        writer.addDocument(doc);
    }

    @SneakyThrows
    private static EntryGenesXML deserializeGenes(final String encoded) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(encoded, EntryGenesXML.class);
    }

    private static List<Gene> convertGenes(final EntryGenesXML entryGenes) {
        List<Gene> genes = new ArrayList<>();
        for (GeneXML gene: entryGenes.getGenes()) {
            genes.add(Gene.builder()
                    .geneId(gene.getGeneId())
                    .symbol(gene.getSymbol())
                    .aliases(gene.getGeneAliases() == null ? null : gene.getGeneAliases().getAliases())
                    .title(gene.getTitle())
                    .taxId(gene.getTaxId())
                    .protGi(gene.getProtGi())
                    .protAcc(gene.getProtAcc())
                    .protLen(gene.getProtLen())
                    .nucGi(gene.getNucGi())
                    .nucAcc(gene.getNucAcc())
                    .domains(gene.getGeneDomains() == null ? null : gene.getGeneDomains().getDomains())
                    .locusTag(gene.getLocusTag())
                    .build()
            );
        }
        return genes;
    }

    private static String serializeGeneEntries(final EntryGenesXML entryGenes) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(entryGenes);
    }

    private static String serializeQueryFields(final EntryGenesXML entryGenes) {
        List<String> geneStrings = new ArrayList<>();
        for (GeneXML gene: entryGenes.getGenes()) {
            geneStrings.add(gene.getSymbol() + (gene.getGeneAliases() == null ? "" : GENE_FIELDS_LINE_DELIMITER +
                    join(gene.getGeneAliases().getAliases(), GENE_FIELDS_LINE_DELIMITER)));
        }
        return join(geneStrings, GENE_FIELDS_LINE_DELIMITER);
    }
}
