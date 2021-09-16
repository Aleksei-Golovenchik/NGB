package com.epam.catgenome.manager.reference;

import com.epam.catgenome.controller.vo.registration.ReferenceRegistrationRequest;
import com.epam.catgenome.entity.reference.Chromosome;
import com.epam.catgenome.entity.reference.Reference;
import com.epam.catgenome.entity.reference.motif.MotifSearchRequest;
import com.epam.catgenome.entity.reference.motif.MotifSearchResult;
import com.epam.catgenome.entity.reference.motif.MotifSearchType;
import com.epam.catgenome.manager.gene.parser.StrandSerializable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-test.xml"})
public class TemporaryTest {

    public static final int PAGE_SIZE = 1000000;
    private static final int[] TIME = new int[3];
    private static final int[] SIZE = new int[3];
    private static final int MILLIS_TO_SECOND = 1;
//    private static final String TEST_MOTIF = "ttttatttcttttCttac";
  //  private static final String TEST_MOTIF = "atatatat";
    private static final String TEST_MOTIF = "acaagt";
                                            //"acwagt"
//    private static final String TEST_MOTIF = "ttttatttctt";

    @Autowired
    ApplicationContext context;

    @Autowired
    private ReferenceManager referenceManager;

    @Autowired
    private MotifSearchManager motifSearchManager;

    private Reference testReference;
    private List<Chromosome> chromosomeList;

    @Before
    public void setup() throws IOException {
//        File fastaFile = new File("C:\\Alexey_Golovenchik\\LAB-Projects\\NGB\\FASTO\\hg38.fa");
        File fastaFile = context.getResource("classpath:templates/A3.fa").getFile();
        ReferenceRegistrationRequest request = new ReferenceRegistrationRequest();
        request.setPath(fastaFile.getPath());
        testReference = referenceManager.registerGenome(request);
        chromosomeList = testReference.getChromosomes();
    }

    @AfterClass
    public static void printResults() {
        System.out.println("test 1: size="+ SIZE[0] + " duration " + TIME[0] + "second (MotifSearchIterator)");
        System.out.println("test 2: size="+ SIZE[1] + " duration " + TIME[1] + "second (AdvancedMotifSearchIterator)");
        System.out.println("test 3: size="+ SIZE[2] + " duration " + TIME[2] + "second (AlterDuoMotifSearchIterator)");
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void search1() {
        final String testMotif1 = "test1" + TEST_MOTIF;
        final int slidingWindow = 30;
        final long l = System.currentTimeMillis();

        MotifSearchRequest testRequest = MotifSearchRequest.builder()
                .referenceId(testReference.getId())
                .chromosomeId(chromosomeList.get(0).getId())
                .startPosition(0)
                .pageSize(PAGE_SIZE)
                .includeSequence(true)
                .searchType(MotifSearchType.WHOLE_GENOME)
                .slidingWindow(slidingWindow)
                .motif(testMotif1)
                .strand(StrandSerializable.POSITIVE)
                .build();
        MotifSearchResult search = motifSearchManager.search(testRequest);

        TIME[0] = (int)((System.currentTimeMillis() - l) / MILLIS_TO_SECOND);
        SIZE[0] = search.getResult().size();
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void search2() {

        final String testMotif1 = "test2" + TEST_MOTIF;
        final int slidingWindow = 30;
        final long l = System.currentTimeMillis();

        MotifSearchRequest testRequest = MotifSearchRequest.builder()
                .referenceId(testReference.getId())
                .chromosomeId(chromosomeList.get(0).getId())
                .startPosition(0)
                .pageSize(PAGE_SIZE)
                .includeSequence(true)
                .searchType(MotifSearchType.WHOLE_GENOME)
                .slidingWindow(slidingWindow)
                .motif(testMotif1)
                .build();
        MotifSearchResult search = motifSearchManager.search(testRequest);

        TIME[1] = (int)((System.currentTimeMillis() - l) / MILLIS_TO_SECOND);
        SIZE[1] = search.getResult().size();
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void search3() {

        final String testMotif1 = "test3" + TEST_MOTIF;
        final int slidingWindow = 30;
        final long l = System.currentTimeMillis();

        MotifSearchRequest testRequest = MotifSearchRequest.builder()
                .referenceId(testReference.getId())
                .chromosomeId(chromosomeList.get(0).getId())
                .startPosition(0)
                .pageSize(PAGE_SIZE)
                .includeSequence(true)
                .searchType(MotifSearchType.WHOLE_GENOME)
                .slidingWindow(slidingWindow)
                .motif(testMotif1)
                .strand(StrandSerializable.POSITIVE)
                .build();
        MotifSearchResult search = motifSearchManager.search(testRequest);

        TIME[2] = (int)((System.currentTimeMillis() - l) / MILLIS_TO_SECOND);
        SIZE[2] = search.getResult().size();
    }
}
