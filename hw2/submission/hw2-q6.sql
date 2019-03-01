SELECT   c.name AS carrier,
         AVG(f.price) AS average_price
FROM     Flights AS f,
         Carriers AS c
WHERE    f.carrier_id = c.cid AND
         (
         (f.origin_city = 'Seattle WA' AND
         f.dest_city = 'New York NY')
         OR
         (f.origin_city = 'New York NY' AND
         f.dest_city = 'Seattle WA')
         )
GROUP BY f.carrier_id;

-- Number of rows in the query result: 3

/*
carrier                 average_price   
----------------------  ----------------
American Airlines Inc.  553.588870967742
JetBlue Airways         538.2045        
Delta Air Lines Inc.    569.29          
*/