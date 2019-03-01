SELECT DISTINCT F2.dest_city AS city
FROM Flights AS F1, Flights AS F2
WHERE F1.origin_city = 'Seattle WA' AND
      F1.dest_city = F2.origin_city AND
      F2.origin_city != 'Seattle WA' AND -- Seattle ignored as one of destinations
      F2.dest_city != 'Seattle WA' AND
      F2.dest_city NOT IN (
        -- direct flights from Seattle
        SELECT DISTINCT Y.dest_city
        FROM Flights AS Y
        WHERE Y.origin_city = 'Seattle WA'
      );

-- Number of rows: 256
-- Execution time: 00:00:18.869
/*
Dothan AL
Toledo OH
Peoria IL
Yuma AZ
Bakersfield CA
Daytona Beach FL
Laramie WY
North Bend/Coos Bay OR
Erie PA
Guam TT
Columbus GA
Wichita Falls TX
Hartford CT
Myrtle Beach SC
Arcata/Eureka CA
Kotzebue AK
Medford OR
Providence RI
Green Bay WI
Santa Maria CA
*/