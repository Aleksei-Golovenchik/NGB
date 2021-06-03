# BLASTn search from full transcript's sequence of Gene 

Test verifies
 - BLASTn search from full transcript's sequence of Gene (All transcript info)

**Prerequisites**:

 - dataset = **Felis_catus**, .gtf = **[Felis_catus.Felis_catus_9.0.94.sorted.gtf](https://ngb-oss-builds.s3.amazonaws.com/public/data/demo/ngb_demo_data/Felis_catus.Felis_catus_9.0.94.sorted.gtf)**
.bam = **[SRR5373742-10m.bam](https://ngb-oss-builds.s3.amazonaws.com/public/data/demo/ngb_demo_data/agnts3.09-28.trim.SRR5373742-10m.bam)**
 - [Sequence of BRCA2-201 transcript for BRCA2 gene](Sequence_data/Sequence_of_BRCA-201_transcript.md)

 - Go to NGB
 - Close **Blast** panel if it is opened

| Steps | Actions | Expected results |
| :---: | --- | --- |
| 1 | Login to NGB | |
| 2 | Go to **Datasets** tab  | |
| 3 | Select dataset, bam file from **Prerequisites** | <li> Bam file (**SRR5373742-10m.bam**) is selected in dataset (**Felis_catus**)|
| 4 | Go to **Browser header**| 
| 5 | Set checkbox **Felis_catus.Felis_catus_9.0.94.sorted.gtf** in the **FELIS_CATUS** dropdown list (if it is not set)  | 
| 6 | Go to Coordinates and search input control at right of tab's header|  | 
| 7 | Enter **A1: 11534536 - 11650701** in the **TYPE COORDINATES** and click **Enter**| **BRCA2** gene displays in the Browser|
| 8 | At the gene-track, select **'Expanded'** view| | 
| 9 | Click on **BRCA2-201** transcript of **BRCA2** gene | <li> Context menu is displayed <li> **BLASTn Search** and **BLASTp Search** display between **BLAT Search** and **Copy info to clipboard** |
| 10 | Click on **BLASTn Search** in the context menu | 2 sub-items appear to the right of the context menu: <li> **Exon only** <li> **All transcript info** |
| 11 | Select **All transcript info** | <li> **BLAST** panel is opened first at the right side in the additional panels <li> **blastn** search tool is selected by default <li> All the corresponding sequence of **BRCA2-201** transcript displays in the **Query Sequence** field as in **Sequence of BRCA2-201 transcript for BRCA2 gene** file from **Prerequisites**|
| 12 | Fill **Task title** field by any value (e.g. "Search from full **BRCA2-201** transcript") | | 
| 13| Select **Homo_sapiens.GRCh38** database from the dropdown in the  **Database** field | | 
| 14 | Type and select **Homo sapiens** in **Organism** field| | 
| 15| Look at **Algoritm** field | **megablast *(highly similar sequence)*** value displays in the **Algoritm** field |
| 16| Look at **Additional Parameters** section |  Collapsed **Additional Parameters** section is displayed|
| 17|  Click **Search** button|  <li> Search is started <li> **History** sub-tab is opened automatically <li> A new search task created with auto-generated ID <li> Title specified at step 12 is displayed in the **Task title** column <li> Current state is **Searching...** <li>  Date and time when the certain search was started is displayed in the **Submitted at** column <li> Duration of the certain search task displays in the **Duration** column <li> Button to **cancel search** (conventionally shown by a **cross-button**) displays to the right of the task <li>  Button to **open the search again**  (conventionally shown by a **reverse arrow-button**) displays to the right of the cross-button|
| 18|Wait until state changed to **Done**| <li> **Current state** changed to **Done** <li> Button **to open the search again** in the **Search** sub-tab (reverse arrow-button) displays **only**|
| 19| Click on **Id** of the last searched sequence in the **Task ID** column | <li> Collapsed **Blast parameters** section is displayed with details (parameters/options) of the opened search above **Sequences table** in the same **History** sub-tab <li> The corresponding search results for the desired sequence is displayed in the **Sequences table** <li> **Homo sapiens** organism only displays in the **Organism** column|
| 20| Click on the first **Sequence ID** link in the **Sequence ID** column | <li> Form with details about all matches (alignments) of the search query to the certain sequence is opened <li> **Alignments** form is opened in the same History sub-tab <li> Strands of each sequence (query and subject) - plus or minus displays on the Alignments form <li> Symbols that "link" the corresponding letters in both sequences displays on the Alignments form: straight line if letters are equal, nothing (empty) if letters are not equal (mismatch), minus symbol ("-") in any sequence - for gaps|
| 21| Click on **View at track** link for the first sequence| An alignment of the nucleotide sequence displays at a track (graphic visualization) in the **Browser** panel |
| 22| Go to **Sequences table** on the NGB| 
| 23| Click on **Sequence ID** link |  Corresponding sequence page on NCBI is opened (if exists)|