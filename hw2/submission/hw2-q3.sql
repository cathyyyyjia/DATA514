SELECT w.day_of_week AS day_of_week,
       avg(arrival_delay) AS delay
  FROM Flights AS f,
       Weekdays AS w
 WHERE f.day_of_week_id = w.did
 GROUP BY f.day_of_week_id
 ORDER BY avg(arrival_delay) DESC
 LIMIT 1;

-- Number of rows in the query result: 1

/*
day_of_week  delay           
-----------  ----------------
Wednesday    14.0085323501848
*/