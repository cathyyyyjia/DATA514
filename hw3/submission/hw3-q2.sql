SELECT DISTINCT F.origin_city AS city
FROM Flights as F
GROUP BY F.origin_city
-- assume NULL actual_time are not 3 hours or more
HAVING MAX(ISNULL(F.actual_time,0)) < 180
ORDER BY F.origin_city;

-- Number of rows: 109
-- Execution time: 00:00:07.459
/*
Aberdeen SD
Abilene TX
Alpena MI
Ashland WV
Augusta GA
Barrow AK
Beaumont/Port Arthur TX
Bemidji MN
Bethel AK
Binghamton NY
Brainerd MN
Bristol/Johnson City/Kingsport TN
Butte MT
Carlsbad CA
Casper WY
Cedar City UT
Chico CA
College Station/Bryan TX
Columbia MO
Columbus GA
*/