/* DATA 514 HW1 Question 1 */
/* Cathy Jia */

CREATE TABLE Edges (Source INTEGER, Destination INTEGER);

INSERT INTO Edges VALUES (10,5);
INSERT INTO Edges VALUES (6,25);
INSERT INTO Edges VALUES (1,3);
INSERT INTO Edges VALUES (4,4);

SELECT * FROM Edges;

SELECT Source FROM Edges;

SELECT * FROM Edges WHERE Source>Destination;

INSERT INTO Edges VALUES ('-1','2000');
/* The tuple can be successfully inserted without an error. SQLite supports the concept of "type affinity" on columns. It automatically convert values to the appropriate datatype. In this statement, columns Source and Destination both have datatype INTEGER. When a string is inserted into a INTEGER column, the storage class of the text is converted to INTEGER if such conversion is lossless and reversible. Therefore, the string '-1' is converted to an integer -1 and the string '2000' is converted to an integer 2000 before being stored. */