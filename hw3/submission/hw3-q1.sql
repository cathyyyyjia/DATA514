SELECT DISTINCT F1.origin_city AS origin_city,
       F2.dest_city AS dest_city,
       F1.max_time AS time
FROM (
    -- longest direct flight
    SELECT F.origin_city AS origin_city,
           max(F.actual_time) AS max_time
    FROM Flights AS F
    GROUP BY F.origin_city
) AS F1, Flights AS F2
WHERE F1.origin_city = F2.origin_city AND
      F1.max_time = F2.actual_time
ORDER BY F1.origin_city,
         F2.dest_city;

-- Number of rows: 329
-- Execution time: 00:00:19.121
/*
Aberdeen SD	Minneapolis MN	106
Abilene TX	Dallas/Fort Worth TX	111
Adak Island AK	Anchorage AK	471.37
Aguadilla PR	New York NY	368.76
Akron OH	Atlanta GA	408.69
Albany GA	Atlanta GA	243.45
Albany NY	Atlanta GA	390.31
Albuquerque NM	Houston TX	492.81
Alexandria LA	Atlanta GA	391.05
Allentown/Bethlehem/Easton PA	Atlanta GA	456.95
Alpena MI	Detroit MI	80
Amarillo TX	Houston TX	390.73
Anchorage AK	Barrow AK	490.01
Appleton WI	Atlanta GA	405.07
Arcata/Eureka CA	San Francisco CA	476.89
Asheville NC	Chicago IL	279.81
Ashland WV	Cincinnati OH	84
Aspen CO	Los Angeles CA	304.59
Atlanta GA	Honolulu HI	649
Atlantic City NJ	Fort Lauderdale FL	212
*/