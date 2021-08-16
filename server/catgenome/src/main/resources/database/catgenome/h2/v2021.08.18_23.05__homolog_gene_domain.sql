CREATE SEQUENCE IF NOT EXISTS CATGENOME.S_HOMOLOG_GENE_DOMAIN START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS CATGENOME.HOMOLOG_GENE_DOMAIN (
    ID BIGINT NOT NULL PRIMARY KEY,
    GENE_ID BIGINT NOT NULL,
    BEGIN BIGINT NOT NULL,
    END BIGINT NOT NULL,
    PSSMID BIGINT NOT NULL,
    CDDID VARCHAR(500) NOT NULL,
    CDDNAME VARCHAR(500) NOT NULL,
    CONSTRAINT domain_gene_id_fkey FOREIGN KEY (GENE_ID) REFERENCES CATGENOME.HOMOLOG_GENE_DESCRIPTION(ID)
);
