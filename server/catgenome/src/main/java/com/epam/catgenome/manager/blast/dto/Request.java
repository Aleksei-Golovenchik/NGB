package com.epam.catgenome.manager.blast.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Request {
    private String blastTool;
    private String algorithm;
    private String dbName;
    private List<Long> taxIds;
    private List<Long> excludedTaxIds;
    private String query;
    private long maxTargetSequence;
    private long expectedThreshold;
    private String options;
}
