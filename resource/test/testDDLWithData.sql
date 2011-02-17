DROP SCHEMA IF EXISTS source CASCADE;
CREATE SCHEMA source;

CREATE TABLE source.person(
name TEXT,
address INT8,
PRIMARY KEY (name)
) WITH OIDS;

CREATE TABLE source.address(
id INT8,
city TEXT,
PRIMARY KEY (id)
) WITH OIDS;


DROP SCHEMA IF EXISTS target CASCADE;
CREATE SCHEMA target;

CREATE VIEW target.employee AS (
SELECT p.name AS name, a.city AS city
FROM
	source.person ANNOT('M1','M2') p LEFT OUTER JOIN
	source.address ANNOT('M2') a ON (p.address = a.id)
);

INSERT INTO source.person VALUES ('Peter','1');
INSERT INTO source.person VALUES ('Heinz','2');
INSERT INTO source.person VALUES ('Gert',NULL);

INSERT INTO source.address VALUES ('1','Toronto');
INSERT INTO source.address VALUES ('2','Montreal');
INSERT INTO source.address VALUES ('3','Quebec');


ALTER TABLE source.person ADD FOREIGN KEY (address) REFERENCES source.address (id);
