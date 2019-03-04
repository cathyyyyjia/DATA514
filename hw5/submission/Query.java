import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.omg.CORBA.SystemException;

/**
 * Runs queries against a back-end database
 */
public class Query
{
  private String configFilename;
  private Properties configProps = new Properties();

  private String jSQLDriver;
  private String jSQLUrl;
  private String jSQLUser;
  private String jSQLPassword;

  // DB Connection
  private Connection conn;

  // Logged In User
  private String username; // customer username is unique

  // Search
  private Itinerary[] searchResult;
  private int searchCount;
  
  // Canned queries

  // flights
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT (capacity-booked) AS capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;
  
  private static final String RESET_FLIGHT_CAPACITY = "UPDATE Flights SET booked = booked - 1 WHERE fid = ?";
  private PreparedStatement resetFlightCapacityStatement;
  
  private static final String UPDATE_FLIGHT_CAPACITY = "UPDATE Flights SET booked = booked + 1 WHERE fid = ?";
  private PreparedStatement updateFlightCapacityStatement;

  // transactions
  private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
  private PreparedStatement beginTransactionStatement;

  private static final String COMMIT_SQL = "COMMIT TRANSACTION";
  private PreparedStatement commitTransactionStatement;

  private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
  private PreparedStatement rollbackTransactionStatement;
  
  // clear
  private static final String CLEAR_USERS = "DELETE FROM Users";
  private PreparedStatement clearUsersStatement;

  private static final String CLEAR_BOOKED = "UPDATE Flights SET booked = 0 WHERE booked <> 0";
  private PreparedStatement clearBookedStatement;
  
  private static final String CLEAR_RESERVATIONS = "DELETE FROM Reservations";
  private PreparedStatement clearReservationsStatement;
  
  // login
  private static final String CHECK_LOGIN = "SELECT COUNT(*) as count FROM Users WHERE username = ? AND password = ?";
  private PreparedStatement checkLoginStatement;
  
  // create
  private static final String CREATE_LOGIN = "INSERT INTO Users VALUES (?, ?, ?)";
  private PreparedStatement createLoginStatement;
  
  // search direct/non-direct flight
  private static final String ONE_HOP_FLIGHT = "SELECT TOP (?) " +
          "fid,year,month_id,day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price " +
          "FROM Flights " +
          "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 " +
          "ORDER BY actual_time ASC, fid ASC";
  private PreparedStatement oneHopFlightStatement;

  private static final String ONE_STOP_FLIGHT = "SELECT TOP (?) " +
          "a.fid AS fid1,a.year AS year1,a.month_id AS month_id1,a.day_of_month AS day_of_month1,a.carrier_id AS carrier_id1,a.flight_num AS flight_num1,a.origin_city AS origin_city1,a.dest_city AS dest_city1,a.actual_time AS actual_time1,a.capacity AS capacity1,a.price AS price1," +
		  "b.fid AS fid2,b.year AS year2,b.month_id AS month_id2,b.day_of_month AS day_of_month2,b.carrier_id AS carrier_id2,b.flight_num AS flight_num2,b.origin_city AS origin_city2,b.dest_city AS dest_city2,b.actual_time AS actual_time2,b.capacity AS capacity2,b.price AS price2 " +
          "FROM Flights AS a, Flights AS b " +
          "WHERE a.origin_city = ? AND b.dest_city = ? AND a.dest_city = b.origin_city AND a.day_of_month = ? AND a.day_of_month = b.day_of_month AND a.canceled = 0 AND b.canceled = 0 " +
          "ORDER BY a.actual_time + b.actual_time ASC, a.fid ASC, b.fid ASC";
  private PreparedStatement oneStopFlightStatement;
  
  // book reservation
  private static final String SEARCH_RESERVATIONS = "SELECT * FROM Reservations WHERE username = ? AND day = ? AND canceled = 0";
  private PreparedStatement searchReservationsStatement;
  
  private static final String COUNT_RESERVATION_ID = "SELECT COUNT(*) AS count FROM Reservations";
  private PreparedStatement countReservationIDStatement;
  
  private static final String MAKE_RESERVATION = "INSERT INTO Reservations " +
		  "(rid,username,year,month,day,fid1,fid2,price,direct) VALUES " + 
		  "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private PreparedStatement makeReservationStatement;
  
  // retrieve reservations
  private static final String GET_RESERVATIONS = "SELECT " +
		  "r.rid AS rid,r.year AS year,r.month AS month,r.day AS day,r.price AS price,r.direct AS direct,r.paid AS paid," +
          "a.fid AS fid1,a.year AS year1,a.month_id AS month_id1,a.day_of_month AS day_of_month1,a.carrier_id AS carrier_id1,a.flight_num AS flight_num1,a.origin_city AS origin_city1,a.dest_city AS dest_city1,a.actual_time AS actual_time1,a.capacity AS capacity1,a.price AS price1," +
		  "b.fid AS fid2,b.year AS year2,b.month_id AS month_id2,b.day_of_month AS day_of_month2,b.carrier_id AS carrier_id2,b.flight_num AS flight_num2,b.origin_city AS origin_city2,b.dest_city AS dest_city2,b.actual_time AS actual_time2,b.capacity AS capacity2,b.price AS price2 " +
          "FROM Reservations AS r INNER JOIN Flights AS a ON r.fid1 = a.fid LEFT JOIN Flights AS b ON r.fid2 = b.fid " +
		  "WHERE r.username = ? AND r.canceled = 0";
  private PreparedStatement getReservationsStatement;
  
  // cancel reservation
  private static final String GET_RESERVATION = "SELECT * FROM Reservations WHERE username = ? AND rid = ? AND canceled = 0";
  private PreparedStatement getReservationStatement;
  
  private static final String CANCEL_RESERVATION = "UPDATE Reservations SET canceled = 1 WHERE rid = ?";
  private PreparedStatement cancelReservationStatement;
  
  private static final String REFUND_RESERVATION = "UPDATE Users SET balance = balance + ? WHERE username = ?";
  private PreparedStatement refundReservationStatement;
  
  // pay reservation
  private static final String CHECK_RESERVATION_ID = "SELECT * FROM Reservations WHERE rid = ?  AND canceled = 0 AND paid = 0";
  private PreparedStatement checkReservationIDStatement;
  
  private static final String GET_USER = "SELECT * FROM Users WHERE username = ?";
  private PreparedStatement getUserStatement;
  
  private static final String PAY_RESERVATION = "UPDATE Users SET balance = balance - ? WHERE username = ?;" + 
  												"UPDATE Reservations SET paid = 1 WHERE rid = ?;";
  private PreparedStatement payReservationStatement;
  
  class Flight {
    public int fid;
    public int year;
    public int monthId;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public double time;
    public int capacity;
    public double price;
    
    public Flight(ResultSet res, String which) throws SQLException { 
		this.fid = res.getInt("fid" + which);
		this.year = res.getInt("year" + which);
		this.monthId = res.getInt("month_id" + which);
	    this.dayOfMonth = res.getInt("day_of_month" + which);
	    this.carrierId = res.getString("carrier_id" + which);
	    this.flightNum = res.getString("flight_num" + which);
	    this.originCity = res.getString("origin_city" + which);
	    this.destCity = res.getString("dest_city" + which);
	    this.time = res.getDouble("actual_time" + which);
	    this.capacity = res.getInt("capacity" + which);
	    this.price = res.getDouble("price" + which);
	}

	@Override
    public String toString() {
      return "ID: " + fid + " Date: " + year + "-" + monthId + "-" + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }
  
  class Itinerary {
	  public int num;
	  public Flight f1;
	  public Flight f2;
	  
	  // direct flight
	  public Itinerary(Flight f) {
		  this.num = 1;
		  this.f1 = f;
		  this.f2 = null;
	  }
	  
	  // non-direct flight
	  public Itinerary(Flight f, Flight g) {
		  this.num = 2;
		  this.f1 = f;
		  this.f2 = g;
	  }
	  
	  @Override
	  public String toString() {
		  double totalTime = f1.time;
		  String str = num + " flight(s), " + totalTime + " minutes\n" + f1.toString() + "\n";
		  if (num == 2) {
			  totalTime += f2.time;
			  str =  num + " flight(s), " + totalTime + " minutes\n" + f1.toString() + "\n" + f2.toString() + "\n";
		  }
	      return str;
	  }
  }

  public Query(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /* Connection code to SQL Azure.  */
  public void openConnection() throws Exception
  {
    configProps.load(new FileInputStream(configFilename));

    jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    jSQLUrl = configProps.getProperty("flightservice.url");
    jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

		/* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

		/* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement

		/* You will also want to appropriately set the transaction's isolation level through:
		   conn.setTransactionIsolation(...)
		   See Connection class' JavaDoc for details.
		 */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * Clear the data in any custom tables created. Do not drop any tables and do not
   * clear the flights table. You should clear any tables you use to store reservations
   * and reset the next reservation ID to be 1.
   */
  public void clearTables () throws Exception
  {
	  // clear user table
	  clearUsersStatement.executeUpdate();
	  // reset flight booked capacity
	  clearBookedStatement.executeUpdate();
	  // clear reservation table
	  clearReservationsStatement.executeUpdate();
	  
	  // No need to reset the next reservation ID because new reservation IDs are
	  // assigned by counting the total record in the whole Reservations table
	  
	  return;
  }

	/**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
    commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
    rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    resetFlightCapacityStatement = conn.prepareStatement(RESET_FLIGHT_CAPACITY);
    updateFlightCapacityStatement = conn.prepareStatement(UPDATE_FLIGHT_CAPACITY);

    /* add here more prepare statements for all the other queries you need */
		/* . . . . . . */
    clearUsersStatement = conn.prepareStatement(CLEAR_USERS);
    clearBookedStatement = conn.prepareStatement(CLEAR_BOOKED);
    clearReservationsStatement = conn.prepareStatement(CLEAR_RESERVATIONS);
    checkLoginStatement = conn.prepareStatement(CHECK_LOGIN);
    createLoginStatement = conn.prepareStatement(CREATE_LOGIN);
    oneHopFlightStatement = conn.prepareStatement(ONE_HOP_FLIGHT);
    oneStopFlightStatement = conn.prepareStatement(ONE_STOP_FLIGHT);
    searchReservationsStatement = conn.prepareStatement(SEARCH_RESERVATIONS);
    countReservationIDStatement = conn.prepareStatement(COUNT_RESERVATION_ID);
    makeReservationStatement = conn.prepareStatement(MAKE_RESERVATION);
    getReservationsStatement = conn.prepareStatement(GET_RESERVATIONS);
    getReservationStatement = conn.prepareStatement(GET_RESERVATION);
    cancelReservationStatement = conn.prepareStatement(CANCEL_RESERVATION);
    refundReservationStatement = conn.prepareStatement(REFUND_RESERVATION);
    checkReservationIDStatement = conn.prepareStatement(CHECK_RESERVATION_ID);
    getUserStatement = conn.prepareStatement(GET_USER);
    payReservationStatement = conn.prepareStatement(PAY_RESERVATION);
  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username
   * @param password
   *
   * @return If someone has already logged in, then return "User already logged in\n"
   * For all other errors, return "Login failed\n".
   *
   * Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password)
  {
	  try {
		  if (this.username != null)
			  return "User already logged in\n";
		  checkLoginStatement.clearParameters();
		  checkLoginStatement.setString(1, username);
		  checkLoginStatement.setString(2, password);
		  ResultSet res = checkLoginStatement.executeQuery();
		  res.next();
		  int count = res.getInt("count");
		  res.close();
		  if (count == 1) {
			  this.username = username;
			  searchResult = null; // initialize search result
			  searchCount = 0;
			  return "Logged in as " + username + "\n";
		  }
		  else
			  return "Login failed\n";
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  return "Login failed\n";
  }

  /**
   * Implement the create user function.
   *
   * @param username new user's username. User names are unique the system.
   * @param password new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer (String username, String password, double initAmount)
  {
	  if (initAmount < 0)
		  return "Failed to create user\n";
	  
	  try {
		  createLoginStatement.clearParameters();
		  createLoginStatement.setString(1, username);
		  createLoginStatement.setString(2, password);
		  createLoginStatement.setDouble(3, initAmount);
		  createLoginStatement.executeUpdate();
		  return "Created user " + username + "\n";
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  return "Failed to create user\n";
  }

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise is searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {	  
	  try {
		  int count = 0;
		  searchResult = new Itinerary[numberOfItineraries];
		  
		  // direct flight
		  oneHopFlightStatement.clearParameters();
		  oneHopFlightStatement.setInt(1, numberOfItineraries);
		  oneHopFlightStatement.setString(2,originCity);
		  oneHopFlightStatement.setString(3, destinationCity);
		  oneHopFlightStatement.setInt(4, dayOfMonth);
		  ResultSet res = oneHopFlightStatement.executeQuery();
		  while (res.next()) {
			  searchResult[count] = new Itinerary(new Flight(res,""));
			  count++;
		  }
		  res.close();
		  
		  if (!directFlight) {
			  // non-direct flight
			  oneStopFlightStatement.clearParameters();
			  oneStopFlightStatement.setInt(1, numberOfItineraries-count);
			  oneStopFlightStatement.setString(2, originCity);
			  oneStopFlightStatement.setString(3, destinationCity);
			  oneStopFlightStatement.setInt(4, dayOfMonth);
			  res = oneStopFlightStatement.executeQuery();
			  while (res.next()) {
				  searchResult[count] = new Itinerary(new Flight(res,"1"), new Flight(res,"2"));
				  count++;
			  }
			  res.close();
		  }
		  
		  if (count == 0)
			  return "No flights match your selection\n";
		  
		  searchCount = count;
		  String str = "";
		  for (int i = 0; i < count; i++) {
			  str += "Itinerary " + i + ": " + searchResult[i].toString();
		  }
		  return str;
	  } catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  return "Failed to search\n";
  }

  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
   * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
   * If the user already has a reservation on the same day as the one that they are trying to book now, then return
   * "You cannot book two flights in the same day\n".
   * For all other errors, return "Booking failed\n".
   *
   * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
   * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
   * successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId)
  {
	  // check username
	  if (username == null)
		  return "Cannot book reservations, not logged in\n";
	  
	  // check itineraryId
	  if (itineraryId < 0 || itineraryId >= searchCount)
		  return "No such itinerary " + itineraryId + "\n";
	  
	  String err = "Booking failed\n";
	  
	  Itinerary currItinerary = searchResult[itineraryId];
	  try {
		  beginTransaction();
		  
		  // search past reservations
		  searchReservationsStatement.clearParameters();
		  searchReservationsStatement.setString(1, username);
		  searchReservationsStatement.setInt(2, currItinerary.f1.dayOfMonth);
		  ResultSet resS = searchReservationsStatement.executeQuery();
		  // check duplicated flight on the same day
		  if (resS.next()) {
			  resS.close();
			  rollbackTransaction();
			  return "You cannot book two flights in the same day\n";
		  }
		  resS.close();
		  
		  // check flight capacity
		  int openSeat = 0;
		  openSeat = checkFlightCapacity(currItinerary.f1.fid);
		  if (openSeat > 0 && currItinerary.num == 2)
			  openSeat = checkFlightCapacity(currItinerary.f2.fid);
		  if (openSeat > 0) {
			  // assign new reservation id
			  int reservationId = 1;
			  ResultSet resCountID= countReservationIDStatement.executeQuery();
			  resCountID.next();
			  reservationId += resCountID.getInt("count");
			  resCountID.close();
			  
			  // make reservation (rid,username,year,month,day,fid1,fid2,price,direct)
			  makeReservationStatement.clearParameters();
			  makeReservationStatement.setInt(1, reservationId);
			  makeReservationStatement.setString(2, username);
			  makeReservationStatement.setInt(3, currItinerary.f1.year);
			  makeReservationStatement.setInt(4, currItinerary.f1.monthId);
			  makeReservationStatement.setInt(5, currItinerary.f1.dayOfMonth);
			  makeReservationStatement.setInt(6, currItinerary.f1.fid);
			  makeReservationStatement.setInt(9, 1); // direct
			  updateFlightCapacity(currItinerary.f1.fid);
			  double price = currItinerary.f1.price;
			  if (currItinerary.num == 2) {
				  makeReservationStatement.setInt(7, currItinerary.f2.fid);
				  price += currItinerary.f2.price;
				  makeReservationStatement.setInt(9, 0); // non-direct
				  updateFlightCapacity(currItinerary.f2.fid);
			  }
			  else
				  makeReservationStatement.setNull(7, Types.INTEGER);
			  makeReservationStatement.setDouble(8,price);
			  makeReservationStatement.executeUpdate();
			  
			  commitTransaction();
			  // successful booking
			  return "Booked flight(s), reservation ID: " + reservationId + "\n";
		  }
		  //else {
			  //System.out.println("No seats available"); // for debugging
		  //}
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
		  // avoid same login and same booking on different terminals
		  //err = ex.getMessage();
		  if (ex.getMessage().startsWith("Violation of UNIQUE KEY constraint 'SAME_DAY'."))
			  err = "You cannot book two flights in the same day\n";
	  }
	  
	  try {
		  rollbackTransaction();
	  } catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  return err;
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
   * If the user has no reservations, then return "No reservations found\n"
   * For all other errors, return "Failed to retrieve reservations\n"
   *
   * Otherwise return the reservations in the following format:
   *
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * ...
   *
   * Each flight should be printed using the same format as in the {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations()
  {
	  // check username
	  if (username == null)
		  return "Cannot view reservations, not logged in\n";
	  
	  try {
		  // get all reservations
		  getReservationsStatement.clearParameters();
		  getReservationsStatement.setString(1, username);
		  ResultSet res = getReservationsStatement.executeQuery();
		  int count = 0;
		  String str = "";
		  while(res.next()) {
			  str += "Reservation " + res.getInt("rid");
			  str += " paid: " + (res.getInt("paid") == 1 ? "true" : "false") + ":\n";
			  // flight 1
			  str += (new Flight(res,"1")).toString() + "\n";
			  // flight 2 if exists
			  if (res.getInt("direct") == 0)
				  str += (new Flight(res,"2")).toString() + "\n";
			  count += 1;
		  }
		  res.close();
		  
		  // no reservations
		  if (count == 0)
			  return "No reservations found\n";
		  
		  return str;
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  return "Failed to retrieve reservations\n";
  }

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   *
   * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
   * For all other errors, return "Failed to cancel reservation [reservationId]"
   *
   * If successful, return "Canceled reservation [reservationId]"
   *
   * Even though a reservation has been canceled, its ID should not be reused by the system.
   */
  public String transaction_cancel(int reservationId)
  {
	  // check username
	  if (username == null)
		  return "Cannot cancel reservations, not logged in\n";
	  
	  try {
		  beginTransaction();
		  
		  // get reservation price
		  getReservationStatement.clearParameters();
		  getReservationStatement.setString(1, username);
		  getReservationStatement.setInt(2, reservationId);
		  ResultSet resR = getReservationStatement.executeQuery();
		  if (!resR.next()) {
			  resR.close();
			  rollbackTransaction();
			  return "Failed to cancel reservation " + reservationId + "\n";
		  }
		  // refund price
		  double price = 0;
		  if (resR.getInt("paid")==1)
			  price = resR.getDouble("price");
		  // flight id(s)
		  int fid1 = resR.getInt("fid1");
		  int fid2 = resR.getInt("fid2");
		  int direct = resR.getInt("direct");
		  resR.close();

		  // cancel reservation
		  cancelReservationStatement.clearParameters();
		  cancelReservationStatement.setInt(1, reservationId);
		  cancelReservationStatement.executeUpdate();
		  
		  // refund
		  refundReservationStatement.clearParameters();
		  refundReservationStatement.setString(2,username);
		  refundReservationStatement.setDouble(1,price);
		  refundReservationStatement.executeUpdate();
		  
		  // update flight capacity
		  resetFlightCapacity(fid1);
		  if (direct == 0)
			  resetFlightCapacity(fid2);
		  
		  commitTransaction();
		  return "Canceled reservation " + reservationId + "\n";
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  try {
		  rollbackTransaction();
	  } catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  return "Failed to cancel reservation " + reservationId + "\n";	  
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n"
   * If the reservation is not found / not under the logged in user's name, then return
   * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
   * If the user does not have enough money in their account, then return
   * "User has only [balance] in account but itinerary costs [cost]\n"
   * For all other errors, return "Failed to pay for reservation [reservationId]\n"
   *
   * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
   * where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay (int reservationId)
  {
	  if (username == null)
		  return "Cannot pay, not logged in\n";
	  
	  try {
		  beginTransaction();
		  
		  // check reservation id in whole system
		  checkReservationIDStatement.clearParameters();
		  checkReservationIDStatement.setInt(1,reservationId);
		  ResultSet resID = checkReservationIDStatement.executeQuery();
		  if (!resID.next()) {
			  resID.close();
			  rollbackTransaction();
			  return "Cannot find unpaid reservation " + reservationId + " under user: " + username + "\n";
		  }
		  
		  // get reservation
		  getReservationStatement.clearParameters();
		  getReservationStatement.setString(1, username);
		  getReservationStatement.setInt(2, reservationId);
		  ResultSet resR = getReservationStatement.executeQuery();
		  if (!resR.next() || resR.getInt("paid")==1) {
			  resR.close();
			  rollbackTransaction();
			  return "Cannot find unpaid reservation " + reservationId + " under user: " + username + "\n";
		  }
		  // get price
		  double price = resR.getDouble("price");
		  resR.close();
		  
		  // get user info
		  getUserStatement.clearParameters();
		  getUserStatement.setString(1, username);
		  ResultSet resU = getUserStatement.executeQuery();
		  resU.next();
		  double balance = resU.getDouble("balance");
		  resU.close();
		  if (balance < price) {
			  rollbackTransaction();
			  return "User has only " + balance + " in account but itinerary costs " + price + "\n";
		  }
		  
		  // update user balance and reservation paid status
		  payReservationStatement.clearParameters();
		  payReservationStatement.setDouble(1,price);
		  payReservationStatement.setString(2, username);
		  payReservationStatement.setInt(3, reservationId);
		  payReservationStatement.executeUpdate();
		  
		  commitTransaction();
		  return "Paid reservation: " + reservationId + " remaining balance: " + (balance - price) + "\n";
	  }
	  catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  try {
		  rollbackTransaction();
	  } catch (Exception ex) {
		  //System.out.println(ex.getMessage()); // for debugging
	  }
	  
	  return "Failed to cancel reservation " + reservationId + "\n";
  }

  /* some utility functions below */

  public void beginTransaction() throws SQLException
  {
    conn.setAutoCommit(false);
    beginTransactionStatement.executeUpdate();
  }

  public void commitTransaction() throws SQLException
  {
    commitTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  public void rollbackTransaction() throws SQLException
  {
    rollbackTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments. You don't need to
   * use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();
    return capacity;
  }
  
  private void resetFlightCapacity(int fid) throws SQLException
  {
    resetFlightCapacityStatement.clearParameters();
    resetFlightCapacityStatement.setInt(1, fid);
    resetFlightCapacityStatement.executeUpdate();
    return;
  }
  
  private void updateFlightCapacity(int fid) throws SQLException
  {
    updateFlightCapacityStatement.clearParameters();
    updateFlightCapacityStatement.setInt(1, fid);
    updateFlightCapacityStatement.executeUpdate();
    return;
  }
}