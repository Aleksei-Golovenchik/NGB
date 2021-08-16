CREATE SEQUENCE IF NOT EXISTS CATGENOME.S_HOMOLOG_GROUP START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS CATGENOME.HOMOLOG_GROUP (
    ID BIGINT NOT NULL PRIMARY KEY,
    PRIMARY_GENE_ID BIGINT NOT NULL,
    PRIMARY_GENE_TAX_ID BIGINT NOT NULL,
    PRIMARY_GENE_NAME VARCHAR(500) NOT NULL,
    PROTEIN_NAME VARCHAR(500) NOT NULL,
    DATABASE_ID BIGINT NOT NULL
);
