-- add all your SQL setup statements here. 

-- You can assume that the following base table has been created with data loaded for you when we test your submission 
-- (you still need to create and populate it in your instance however),
-- although you are free to insert extra ALTER COLUMN ... statements to change the column 
-- names / types if you like.

-- CREATE TABLE FLIGHTS
-- (
--  fid int NOT NULL PRIMARY KEY,
--  year int,
--  month_id int,
--  day_of_month int,
--  day_of_week_id int,
--  carrier_id varchar(3),
--  flight_num int,
--  origin_city varchar(34),
--  origin_state varchar(47),
--  dest_city varchar(34),
--  dest_state varchar(46),
--  departure_delay double precision,
--  taxi_out double precision,
--  arrival_delay double precision,
--  canceled int,
--  actual_time double precision,
--  distance double precision,
--  capacity int,
--  price double precision
--)

ALTER TABLE Flights ADD booked int NOT NULL DEFAULT(0);

--DROP TABLE IF EXISTS Users;
--DROP TABLE IF EXISTS Reservations;

CREATE TABLE Users (
    username VARCHAR(20) PRIMARY KEY,
    password VARCHAR(20),
    balance FLOAT
);

CREATE TABLE Reservations (
    rid INT PRIMARY KEY NONCLUSTERED,
    username VARCHAR(20),
    year INT,
    month INT,
    day INT,
    fid1 INT,
    fid2 INT,
    price FLOAT,
    direct INT,
    canceled INT NOT NULL DEFAULT(0),
    paid INT NOT NULL DEFAULT(0)
    CONSTRAINT SAME_DAY UNIQUE (username, day)
);

CREATE NONCLUSTERED INDEX nc_idx_res_username_day ON Reservations(username, day);

-- check Users table
--SELECT * FROM Users;
-- check Reservations table
--SELECT * FROM Reservations;