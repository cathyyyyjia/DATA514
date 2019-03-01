SELECT DISTINCT C.name AS carrier
FROM Flights AS F, Carriers AS C
WHERE F.carrier_id = C.cid AND
      F.origin_city = 'Seattle WA' AND
      F.dest_city = 'San Francisco CA';

-- Number of rows: 4
-- Execution time: 00:00:03.785
/*
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
*/