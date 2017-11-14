--
-- Keyspace
--
CREATE KEYSPACE IF NOT EXISTS vessel
WITH REPLICATION = {
	'class' : 'NetworkTopologyStrategy',
	'dc1' : 1
};

--
-- UDT
--
CREATE TYPE IF NOT EXISTS vessel.vessel (
	name text,
	category text,
	visibility text,
	creation_center text,
	last_departure_port text,
	last_departure_time timestamp
);

--
-- Table
--
CREATE TABLE IF NOT EXISTS vessel.vessels (
	visibility text,
	uuid UUID,
	vessel FROZEN<vessel>,
	name text,
	category text,
	PRIMARY KEY ( visibility, uuid )
) WITH CLUSTERING ORDER BY ( uuid DESC );

CREATE TABLE IF NOT EXISTS vessel.vessels_by_uuid (
	uuid UUID,
	vessel FROZEN<vessel>,
	PRIMARY KEY ( uuid )
);

--
-- SSTable Attached Secondary Index
--
CREATE CUSTOM INDEX IF NOT EXISTS zone_name_index ON vessel.vessels (name)
USING 'org.apache.cassandra.index.sasi.SASIIndex'
WITH OPTIONS = {
	'mode' : 'CONTAINS',
	'analyzer_class' : 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer',
	'case_sensitive' : 'false'
};

--
-- Materialized view
--
CREATE MATERIALIZED VIEW IF NOT EXISTS vessel.vessels_by_category
AS SELECT visibility, uuid, vessel, name, category
	FROM vessels
	WHERE visibility IS NOT NULL AND category IS NOT NULL AND uuid IS NOT NULL
	PRIMARY KEY ( (visibility, category), uuid );