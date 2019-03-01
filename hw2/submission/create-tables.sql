CREATE TABLE Flights (
    fid             INT          PRIMARY KEY,
    year            INT,
    month_id        INT          REFERENCES Months,
    day_of_month    INT,
    day_of_week_id  INT          REFERENCES Weekdays,
    carrier_id      VARCHAR (3)  REFERENCES Carriers,
    flight_num      INT,
    origin_city     VARCHAR (34),
    origin_state    VARCHAR (47),
    dest_city       VARCHAR (34),
    dest_state      VARCHAR (46),
    departure_delay DOUBLE,
    taxi_out        DOUBLE,
    arrival_delay   DOUBLE,
    canceled        INT,
    actual_time     DOUBLE,
    distance        DOUBLE,
    capacity        INT,
    price           DOUBLE
);

CREATE TABLE Carriers (
    cid  VARCHAR (7)  PRIMARY KEY,
    name VARCHAR (83) 
);

CREATE TABLE Months (
    mid   INT         PRIMARY KEY,
    month VARCHAR (9) 
);

CREATE TABLE Weekdays (
    did         INT         PRIMARY KEY,
    day_of_week VARCHAR (9) 
);


PRAGMA foreign_keys=ON;
.mode csv
.import ./carriers.csv Carriers
.import ./months.csv Months
.import ./weekdays.csv Weekdays
.import ./flights-small.csv Flights