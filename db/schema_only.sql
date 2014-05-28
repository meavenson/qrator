DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
SET search_path TO public;

CREATE TABLE QUser (
    sid BIGSERIAL PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT 'true',
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    lastLogin TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE QSource (
    sid BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    uri VARCHAR(256) NOT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE
);

CREATE TABLE QTree (
    sid BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(512) DEFAULT NULL,
    spec TEXT,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE
);

CREATE TABLE QStructureType (
    sid BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(512) DEFAULT NULL,
    glycoName VARCHAR(128) NOT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    intreeqtree BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (intreeqtree) REFERENCES QTree (sid) ON DELETE CASCADE
);

CREATE TABLE QStructure (
    sid BIGSERIAL PRIMARY KEY,
    filename VARCHAR(128) NOT NULL,
    hash VARCHAR(128) NOT NULL UNIQUE,
    spec TEXT NOT NULL,
    contents TEXT NOT NULL,
    status VARCHAR(9) NOT NULL,
    version VARCHAR(32) DEFAULT NULL,
    uri VARCHAR(256) DEFAULT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    hastypeqstructuretype BIGINT NOT NULL,
    CHECK (status in ('pending', 'reviewed', 'deferred', 'approved', 'rejected', 'committed')),
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (hastypeqstructuretype) REFERENCES QStructureType (sid) ON DELETE CASCADE
);

CREATE TABLE QProvenance (
    sid BIGSERIAL PRIMARY KEY,
    action VARCHAR(10) NOT NULL,
    uri VARCHAR(256) DEFAULT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    qstructurehasprovenance BIGINT NOT NULL,
    CHECK (action in ('toPending', 'toReviewed', 'toDeferred', 'toApproved', 'toRejected', 'toOntology')),
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (qstructurehasprovenance) REFERENCES QStructure (sid) ON DELETE CASCADE
);

CREATE TABLE QAnnotation (
    sid BIGSERIAL PRIMARY KEY,
    comment VARCHAR(512) NOT NULL,
    uri VARCHAR(256) DEFAULT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    qstructurehasannotation BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (qstructurehasannotation) REFERENCES QStructure (sid) ON DELETE CASCADE
);

CREATE TABLE QRole (
    sid BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE
);

CREATE TABLE QReference (
    sid BIGSERIAL PRIMARY KEY,
    srcId VARCHAR(128) NOT NULL,
    uri VARCHAR(256) DEFAULT NULL UNIQUE,
    createdOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    modifiedOn TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    createdbyquser BIGINT NOT NULL,
    hassourceqsource BIGINT NOT NULL,
    qstructurehasreference BIGINT NOT NULL,
    FOREIGN KEY (createdbyquser) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (hassourceqsource) REFERENCES QSource (sid) ON DELETE CASCADE,
    FOREIGN KEY (qstructurehasreference) REFERENCES QStructure (sid) ON DELETE CASCADE
);

CREATE TABLE QUserTracksQStructure (
    tracker BIGINT NOT NULL,
    tracked BIGINT NOT NULL,
    UNIQUE(tracker, tracked),
    FOREIGN KEY (tracker) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (tracked) REFERENCES QStructure (sid) ON DELETE CASCADE
);

CREATE TABLE QUserHasRoleQRole (
    account BIGINT NOT NULL,
    role BIGINT NOT NULL,
    UNIQUE(account, role),
    FOREIGN KEY (account) REFERENCES QUser (sid) ON DELETE CASCADE,
    FOREIGN KEY (role) REFERENCES QRole (sid) ON DELETE CASCADE
);

INSERT INTO quser (username, password, name, email) VALUES ('admin','5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8','Administrator','');

INSERT INTO qrole (name, createdbyquser) VALUES ('admin', 1);
INSERT INTO qrole (name, createdbyquser) VALUES ('submit', 1);
INSERT INTO qrole (name, createdbyquser) VALUES ('review', 1);
INSERT INTO qrole (name, createdbyquser) VALUES ('curate', 1);

INSERT INTO quserhasroleqrole (account, role) VALUES (1, 1);

INSERT INTO qtree (name, createdbyquser) VALUES ('Unknown',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('N-glycan_lipid-linked_precursor',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('N-glycan',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('GalNAc-initiated_O-glycan',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('Man-initiated_O-glycan',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('Fuc-initiated_O-glycan',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('Gal-initiated_glycosphingolipid',1);
INSERT INTO qtree (name, createdbyquser) VALUES ('Glc-initiated_glycosphingolipid',1);

INSERT INTO qsource (name, uri, createdbyquser) VALUES ('GlycomeDB','http://www.glycome-db.org/database/showStructure.action?glycomeId=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('CarbBank','http://www.genome.jp/dbget-bin/www_bget?carbbank+<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('EurocarbDB (EBI)','http://www.ebi.ac.uk/eurocarb/show_glycan.action?glycanSequenceId=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('EurocarbDB (NIBRT)','http://glycobase.nibrt.ie:8080/internal/show_glycan.action?glycanSequenceId=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('Glycosciences.de','http://www.glycosciences.de/sweetdb/start.php?action=explore_linucsid&linucsid=<ID>&show=1#struct',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('BCSDB','http://www.glyco.ac.ru/bcsdb3/search_id.php?mode=record&id_list=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('CFG','http://www.functionalglycomics.org/glycomics/CarbohydrateServlet?pageType=view&view=view&operationType=view&carbId=<ID>&sideMenu=no',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('KEGG','http://www.genome.jp/dbget-bin/www_bget?gl:<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('Glycobase (Dublin)','http://glycobase.nibrt.ie:8080/database/show_glycanEntry.action?glycanId=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('Glycobase (Lille)','http://glycobase.univ-lille1.fr/base/view_mol.php?id=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('PDB','http://www.rcsb.org/pdb/explore/explore.do?structureId=<ID>',1);
INSERT INTO qsource (name, uri, createdbyquser) VALUES ('GlyAffinity','http://worm.mpi-cbg.de/affinity/structure.action?ID=<ID>',1);