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
package com.epam.catgenome.manager.externaldb.homolog;

import com.epam.catgenome.manager.blast.dto.BlastTaxonomy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

@Service
@Slf4j
public class HomologManager {

    private static final String AUTHORITY = "authority";
    private static final String COMMON_NAME = "common name";
    private static final String SCIENTIFIC_NAME = "scientific name";
    private static final String TAXONOMY_LINE_DELIMITER = "|";
    private static final String TAXONOMY_TOKEN_DELIMITER_PATTERN = "\\|";
    private static final List<String> RECORDS_TO_BE_EXCLUDED = Arrays.asList("type material", "in-part");
    private static final String TAXONOMY_TERM_SPLIT_TOKEN = " ";

    @Value("${taxonomy.index.directory}")
    private String taxonomyIndexDirectory;

    @Value("${taxonomy.top.hits:10}")
    private int taxonomyTopHits;

//    public List<BlastTaxonomy> searchOrganisms(final String terms) throws IOException, ParseException {
//        final List<BlastTaxonomy> organisms = new ArrayList<>();
//        try (Directory index = new SimpleFSDirectory(Paths.get(taxonomyIndexDirectory));
//            IndexReader indexReader = DirectoryReader.open(index)) {
//            IndexSearcher searcher = new IndexSearcher(indexReader);
//            TopDocs topDocs = searcher.search(buildTaxonomySearchQuery(terms), taxonomyTopHits);
//            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//                Document doc = searcher.doc(scoreDoc.doc);
//                organisms.add(
//                    BlastTaxonomy.builder()
//                        .taxId(getTaxId(doc))
//                        .scientificName(getScientificName(doc))
//                        .commonName(getCommonName(doc))
//                        .synonyms(getSynonyms(doc))
//                        .build()
//                );
//            }
//        }
//        return organisms;
//    }
//
//    @SneakyThrows
//    public BlastTaxonomy searchHomolog(final HomologSearchRequest searchRequest) {
//        final StandardAnalyzer analyzer = new StandardAnalyzer();
//        final Query query = new QueryParser(TaxonomyIndexFields.TAX_ID.getFieldName(), analyzer)
//                .parse(String.valueOf(searchRequest));
//        try (Directory index = new SimpleFSDirectory(Paths.get(taxonomyIndexDirectory));
//             IndexReader indexReader = DirectoryReader.open(index)) {
//            IndexSearcher searcher = new IndexSearcher(indexReader);
//            TopDocs topDocs = searcher.search(query, 1);
//            ScoreDoc scoreDoc = topDocs.scoreDocs.length > 0 ? topDocs.scoreDocs[0] : null;
//            Document doc = scoreDoc != null ? searcher.doc(scoreDoc.doc) : null;
//            return doc == null ? null
//                    :  BlastTaxonomy.builder().taxId(getTaxId(doc))
//                            .scientificName(getScientificName(doc))
//                            .commonName(getCommonName(doc))
//                            .synonyms(getSynonyms(doc))
//                            .build();
//        }
//    }
//
//    public void importHomologData(final String databasePath) throws IOException, ParseException {
//        try (Directory index = new SimpleFSDirectory(Paths.get(taxonomyIndexDirectory));
//             IndexWriter writer = new IndexWriter(
//                     index, new IndexWriterConfig(new StandardAnalyzer())
//                     .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND))) {
//            writer.deleteAll();
//            for (BlastTaxonomy taxonomyEntry: readTaxonomy(databasePath)) {
//                addDoc(writer, taxonomyEntry);
//            }
//        }
//    }
//
//    List<BlastTaxonomy> readTaxonomy(final String path) {
//        return readTaxonomyLines(path).stream().collect(Collectors.groupingBy(TaxonomyRecord::getTaxId))
//                .entrySet().stream()
//                .map(entry -> processTaxonomyId(entry.getKey(), entry.getValue()))
//                .filter(Objects::nonNull)
//                .filter(this::taxonomyIdIsComplete)
//                .collect(Collectors.toList());
//    }
//
//    private Query buildTaxonomySearchQuery(final String terms) {
//        final BooleanQuery.Builder mainBuilder = new BooleanQuery.Builder();
//        for (String term : terms.split(TAXONOMY_TERM_SPLIT_TOKEN)) {
//            mainBuilder.add(
//                new BooleanQuery.Builder()
//                    .add(buildPrefixQuery(term, TaxonomyIndexFields.COMMON_NAME), BooleanClause.Occur.SHOULD)
//                    .add(buildPrefixQuery(term, TaxonomyIndexFields.SCIENTIFIC_NAME), BooleanClause.Occur.SHOULD)
//                    .add(buildPrefixQuery(term, TaxonomyIndexFields.SYNONYMS), BooleanClause.Occur.SHOULD)
//                    .build(),
//                BooleanClause.Occur.MUST
//            );
//        }
//        return mainBuilder.build();
//    }
//
//
//
//    private List<TaxonomyRecord> readTaxonomyLines(final String path) {
//        try {
//            return Files.lines(Paths.get(path))
//                    .map(tl -> new TaxonomyRecord(
//                            Long.parseLong(tl.split(TAXONOMY_TOKEN_DELIMITER_PATTERN)[0].trim()), tl))
//                    .collect(Collectors.toList());
//        } catch (IOException | NumberFormatException e) {
//            log.error(e.getMessage());
//            return Collections.emptyList();
//        }
//    }
}
