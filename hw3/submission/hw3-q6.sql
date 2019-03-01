SELECT DISTINCT C.name AS carrier
FROM Carriers AS C
WHERE C.cid IN (
    SELECT F.carrier_id
    FROM Flights AS F
    WHERE F.origin_city = 'Seattle WA' AND
          F.dest_city = 'San Francisco CA'
);

-- Number of rows: 4
-- Execution time: 00:00:03.895
/*
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
*/