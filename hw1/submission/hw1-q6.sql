/* DATA 514 HW1 Question 6 */
/* Cathy Jia */

SELECT * FROM MyRestaurants WHERE likeOrNot=1 AND lastVisit<date('now','-3 month');