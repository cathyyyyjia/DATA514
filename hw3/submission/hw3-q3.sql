SELECT F1.origin_city AS origin_city,
       CAST(F2.num AS FLOAT)/F1.num AS percentage
FROM (
    -- total number (NULL included)
    SELECT Y.origin_city AS origin_city,
           ISNULL(COUNT(*),1) AS num
    FROM Flights AS Y
    GROUP BY Y.origin_city
) AS F1 LEFT JOIN (
    -- number of city with time < 180
    SELECT X.origin_city AS origin_city,
           COUNT(*) AS num
    FROM Flights AS X
    -- NULL actual_time not counted as < 180
    WHERE ISNULL(X.actual_time,200) < 180
    GROUP BY X.origin_city
) AS F2
ON F1.origin_city = F2.origin_city
ORDER BY CAST(F2.num AS FLOAT)/F1.num;

-- Number of rows: 327
-- Execution time: 00:00:14.569
/*
Guam TT	NULL
Pago Pago TT	NULL
Aguadilla PR	0.286792452830189
Anchorage AK	0.316562778272484
San Juan PR	0.335439168534746
Charlotte Amalie VI	0.392700729927007
Ponce PR	0.403225806451613
Fairbanks AK	0.495391705069124
Kahului HI	0.533411833971151
Honolulu HI	0.545336955115768
San Francisco CA	0.552237084870849
Los Angeles CA	0.554127883447997
Seattle WA	0.574109328256731
New York NY	0.605324373223055
Long Beach CA	0.61719979024646
Kona HI	0.629527991218441
Newark NJ	0.633675652545999
Plattsburgh NY	0.64
Las Vegas NV	0.644710061799208
Christiansted VI	0.646666666666667
*/