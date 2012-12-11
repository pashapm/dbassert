DROP TABLE customers IF EXISTS;
DROP TABLE IF EXISTS test_table;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS domains;

CREATE TABLE customers
(
  id int NOT NULL,
  name character varying,
  date_cr date,
  number integer
);


CREATE TABLE events
(
  id int NOT NULL,
  name character varying,
  login_name character varying
);

CREATE TABLE test_table
(
  name character varying,
  id int NOT NULL,
  CONSTRAINT test_table_pkey PRIMARY KEY (id)
);

CREATE TABLE domains
(
    id int NOT NULL,
    name character varying,
    parent_id integer not null,
    PRIMARY KEY(id),
    FOREIGN KEY (parent_id) REFERENCES domains(id)
);
