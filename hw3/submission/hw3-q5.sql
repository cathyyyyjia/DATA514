SELECT DISTINCT F3.dest_city AS city
FROM Flights AS F3
WHERE F3.dest_city != 'Seattle WA' AND -- Seattle ignored as a destination
      F3.dest_city NOT IN (
        -- direct flights from Seattle
        SELECT DISTINCT X.dest_city
        FROM Flights AS X
        WHERE X.origin_city = 'Seattle WA'
      ) AND
      F3.dest_city NOT IN (
        -- one-stop flights from Seattle
        SELECT DISTINCT F2.dest_city AS city
        FROM Flights AS F1, Flights AS F2
        WHERE F1.origin_city = 'Seattle WA' AND
              F1.dest_city = F2.origin_city AND
              F2.origin_city != 'Seattle WA' AND
              F2.dest_city != 'Seattle WA' AND
              F2.dest_city NOT IN (
                SELECT DISTINCT Y.dest_city
                FROM Flights AS Y
                WHERE Y.origin_city = 'Seattle WA'
              )
      );

-- Number of rows: 3
-- Query time: 00:05:29.949
/*
Devils Lake ND
Hattiesburg/Laurel MS
St. Augustine FL
*/