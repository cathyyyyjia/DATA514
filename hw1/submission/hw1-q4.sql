/* DATA 514 HW1 Question 4 */
/* Cathy Jia */

SELECT * FROM MyRestaurants;


/* Format output with headers */
.headers on

/* Print the results in comma-separated form. */
.separator ,
SELECT * FROM MyRestaurants;

/* Print the results in list form, delimited by "|" */
.mode list
SELECT * FROM MyRestaurants;

/* Print the results in column form, and make each column have width 15 */
.mode column
.width 15 15 15 15 15
SELECT * FROM MyRestaurants;


/* Format output without headers */
.headers off

/* Print the results in comma-separated form. */
.separator ,
SELECT * FROM MyRestaurants;

/* Print the results in list form, delimited by "|" */
.mode list
SELECT * FROM MyRestaurants;

/* Print the results in column form, and make each column have width 15 */
.mode column
.width 15 15 15 15 15
SELECT * FROM MyRestaurants;