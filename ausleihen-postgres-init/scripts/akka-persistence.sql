CREATE EXTENSION IF NOT EXISTS HSTORE;

CREATE TABLE IF NOT EXISTS journal (
   "id" BIGSERIAL NOT NULL PRIMARY KEY,
   "persistenceid" VARCHAR(254) NOT NULL,
   "sequencenr" INT NOT NULL,
   "rowid" BIGINT DEFAULT NULL,
   "deleted" BOOLEAN DEFAULT false,
   "payload" BYTEA,
   "manifest" VARCHAR(512),
   "uuid" VARCHAR(36) NOT NULL,
   "writeruuid" VARCHAR(36) NOT NULL,
   "created" timestamptz NOT NULL,
   "tags" HSTORE,
   "event" JSON,
   constraint "cc_journal_payload_event" check (payload IS NOT NULL OR event IS NOT NULL)
);

CREATE UNIQUE INDEX IF NOT EXISTS journal_pidseq_idx ON journal (persistenceid, sequencenr);
CREATE UNIQUE INDEX IF NOT EXISTS journal_rowid_idx ON journal (rowid);

CREATE TABLE IF NOT EXISTS snapshot (
   "persistenceid" VARCHAR(254) NOT NULL,
   "sequencenr" INT NOT NULL,
   "timestamp" bigint NOT NULL,
   "snapshot" BYTEA,
   "manifest" VARCHAR(512),
   "json" JSON,
   CONSTRAINT "cc_snapshot_payload_jsoin" check (snapshot IS NOT NULL OR (json IS NOT NULL AND manifest IS NOT NULL)),
   PRIMARY KEY (persistenceid, sequencenr)
);