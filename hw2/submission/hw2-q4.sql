SELECT DISTINCT c.name AS name
  FROM Flights AS f,
       Carriers AS c
 WHERE f.carrier_id = c.cid
 GROUP BY f.year,
          f.month_id,
          f.day_of_month,
          f.carrier_id
HAVING count(*) > 1000;

-- Number of rows in the query result: 11

/*
name                  
----------------------
American Airlines Inc.
Delta Air Lines Inc.  
Envoy Air             
Northwest Airlines Inc
Comair Inc.           
SkyWest Airlines Inc. 
United Air Lines Inc. 
US Airways Inc.       
Southwest Airlines Co.
ExpressJet Airlines In
ExpressJet Airlines In
*/