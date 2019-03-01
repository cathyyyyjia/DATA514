SELECT DISTINCT c.name AS name,
                AVG(f.canceled)*100 AS percent
  FROM Flights AS f,
       Carriers AS c
 WHERE f.carrier_id = c.cid AND 
       f.origin_city = 'Seattle WA'
 GROUP BY f.carrier_id
HAVING percent > 0.5
 ORDER BY percent ASC;

-- Number of rows in the query result: 6

/*
name                   percent          
---------------------  -----------------
SkyWest Airlines Inc.  0.728291316526611
Frontier Airlines Inc  0.840336134453782
United Air Lines Inc.  0.983767830791933
JetBlue Airways        1.00250626566416 
Northwest Airlines In  1.4336917562724  
ExpressJet Airlines I  3.2258064516129  
*/