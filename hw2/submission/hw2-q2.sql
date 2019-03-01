SELECT c.name AS name,
       f1.flight_num AS f1_flight_num,
       f1.origin_city AS f1_origin_city,
       f1.dest_city AS f1_dest_city,
       f1.actual_time AS f1_actual_time,
       f2.flight_num AS f2_flight_num,
       f2.origin_city AS f2_origin_city,
       f2.dest_city AS f2_dest_city,
       f2.actual_time AS f2_actual_time,
       f1.actual_time + f2.actual_time AS actual_time
  FROM Flights AS f1,
       Flights AS f2,
       Carriers AS c,
       Months AS m
 WHERE f1.origin_city = 'Seattle WA' AND 
       f1.dest_city = f2.origin_city AND 
       f2.dest_city = 'Boston MA' AND 
       f1.month_id = m.mid AND 
       f2.month_id = m.mid AND 
       m.month = 'July' AND 
       f1.day_of_month = 15 AND 
       f2.day_of_month = 15 AND 
       f1.year = 2015 AND 
       f2.year = 2015 AND 
       f1.carrier_id = f2.carrier_id AND 
       f1.carrier_id = c.cid AND 
       f1.actual_time + f2.actual_time < 420;
       
-- Number of Rows in the query result: 488

/*
 First 20 rows in hte query result: 
 name                    f1_flight_num  f1_origin_city  f1_dest_city  f1_actual_time  f2_flight_num  f2_origin_city  f2_dest_city  f2_actual_time  actual_time
----------------------  -------------  --------------  ------------  --------------  -------------  --------------  ------------  --------------  -----------
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           26             Chicago IL      Boston MA     150.0           378.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           186            Chicago IL      Boston MA     137.0           365.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           288            Chicago IL      Boston MA     137.0           365.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           366            Chicago IL      Boston MA     150.0           378.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           1205           Chicago IL      Boston MA     128.0           356.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           1240           Chicago IL      Boston MA     130.0           358.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           1299           Chicago IL      Boston MA     133.0           361.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           1435           Chicago IL      Boston MA     133.0           361.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           1557           Chicago IL      Boston MA     122.0           350.0      
American Airlines Inc.  42             Seattle WA      Chicago IL    228.0           2503           Chicago IL      Boston MA     127.0           355.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           84             New York NY     Boston MA     74.0            396.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           199            New York NY     Boston MA     80.0            402.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           235            New York NY     Boston MA     91.0            413.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           1443           New York NY     Boston MA     80.0            402.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2118           New York NY     Boston MA                     322.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2121           New York NY     Boston MA     74.0            396.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2122           New York NY     Boston MA     65.0            387.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2126           New York NY     Boston MA     60.0            382.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2128           New York NY     Boston MA     83.0            405.0      
American Airlines Inc.  44             Seattle WA      New York NY   322.0           2131           New York NY     Boston MA     70.0            392.0      
*/