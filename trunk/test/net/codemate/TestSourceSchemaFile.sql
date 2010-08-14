DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS test_table;
DROP TABLE IF EXISTS events;

CREATE TABLE customers
(
  id serial NOT NULL,
  "name" character varying,
  date_cr date,
  "number" integer
)
WITH (OIDS=FALSE);
ALTER TABLE customers OWNER TO postgres;


CREATE TABLE events
(
  id serial NOT NULL,
  "name" character varying,
  login_name character varying
)
WITH (OIDS=FALSE);
ALTER TABLE events OWNER TO postgres;

CREATE TABLE test_table
(
  "name" character varying,
  id serial NOT NULL,
  CONSTRAINT test_table_pkey PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE test_table OWNER TO postgres;