package server;

import common.IVidRentalService;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI Service Implementation executing JDBC operations against the MySQL VLS
 * database.
 * Includes automated schema creation checks at initialization.
 */
public class VidRentalServiceImpl extends UnicastRemoteObject implements IVidRentalService {

  /** JDBC URL targeting the local MySQL schema space. */
  private static final String DB_URL = "jdbc:mysql://localhost:3306/videorentalssystem";

  /** Database access username credential. */
  private static final String DB_USER = "root";

  /** Database access password credential. */
  private static final String DB_PASS = "Delliakavinya123";

  /**
   * Constructs a new VidRentalServiceImpl remote instance.
   * Exports the object to the RMI runtime environments and triggers internal
   * relational database validation/schema setup.
   *
   * @throws RemoteException if the remote object export process fails
   */
  public VidRentalServiceImpl() throws RemoteException {
    super();
    initializeDatabase();
  }

  /**
   * Runtime check: Verification and structural deployment of the MySQL schema.
   * Generates necessary application tables if they do not exist within the local
   * target space.
   */
  private void initializeDatabase() {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement()) {

      if (conn != null) {
        System.out.println("JDBC connected successfully to Server space database.");

        // 1. Genres Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Genres (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "genre VARCHAR(100) NOT NULL," +
                "isactive BOOLEAN DEFAULT TRUE" +
                ")");

        // 2. Movies Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Movies (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "genre_id INT NOT NULL," +
                "Title VARCHAR(255) NOT NULL," +
                "isactive BOOLEAN DEFAULT TRUE," +
                "FOREIGN KEY (genre_id) REFERENCES Genres(id)" +
                ")");

        // 3. Clients Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Clients (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "Fullname VARCHAR(255) NOT NULL," +
                "phone VARCHAR(20)," +
                "email VARCHAR(255)," +
                "isactive BOOLEAN DEFAULT TRUE" +
                ")");

        // 4. Rentals Table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Rentals (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "client_id INT NOT NULL," +
                "movie_id INT NOT NULL," +
                "Returned BOOLEAN DEFAULT FALSE," +
                "FOREIGN KEY (client_id) REFERENCES Clients(id)," +
                "FOREIGN KEY (movie_id) REFERENCES Movies(id)" +
                ")");

        System.out.println("Database tables validated/created successfully.");
      }
    } catch (SQLException e) {
      System.err.println("Critical runtime database initialization error:");
      e.printStackTrace();
    }
  }

  // --- GENRE OPERATIONS ---

  /**
   * {@inheritDoc}
   * Executes an INSERT SQL query to persist a new genre row with default active
   * state.
   */
  @Override
  public boolean saveGenre(String name) throws RemoteException {
    String sql = "INSERT INTO Genres(genre, isactive) VALUES(?, 1)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Executes a soft-delete UPDATE statement to flag the specified genre as
   * inactive.
   */
  @Override
  public boolean removeGenre(String name) throws RemoteException {
    String sql = "UPDATE Genres SET isactive = 0 WHERE genre = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Collects all records from the Genres table where the active flag equals 1.
   */
  @Override
  public List<String> getActiveGenres() throws RemoteException {
    List<String> genres = new ArrayList<>();
    String sql = "SELECT genre FROM Genres WHERE isactive = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        genres.add(rs.getString("genre"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return genres;
  }

  // --- MOVIE OPERATIONS ---

  /**
   * {@inheritDoc}
   * Inserts a new Movie row utilizing an internal sub-query lookup to obtain the
   * matching genre ID.
   */
  @Override
  public boolean saveMovie(String genreName, String title) throws RemoteException {
    String sql = "INSERT INTO Movies(genre_id, Title, isactive) SELECT id, ?, 1 FROM Genres WHERE genre = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, title);
      pstmt.setString(2, genreName);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Executes an inner join query across Movies and Genres to find active titles
   * by name criteria.
   */
  @Override
  public List<String> getMoviesByGenre(String genreName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Movies m JOIN Genres g ON m.genre_id = g.id WHERE g.genre = ? AND m.isactive = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, genreName);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          movies.add(rs.getString("Title"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return movies;
  }

  /**
   * {@inheritDoc}
   * Soft-deletes a movie row by mutating the active flag field to 0.
   */
  @Override
  public boolean removeMovie(String title) throws RemoteException {
    String sql = "UPDATE Movies SET isactive = 0 WHERE Title = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, title);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // --- CUSTOMER OPERATIONS ---

  /**
   * {@inheritDoc}
   * Inserts customer attributes directly into the Clients database table data
   * space.
   */
  @Override
  public boolean saveCustomer(String name, String phone, String email) throws RemoteException {
    String sql = "INSERT INTO Clients(Fullname, phone, email, isactive) VALUES(?, ?, ?, 1)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.setString(2, phone);
      pstmt.setString(3, email);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Updates an existing Client profile to disable active status flag.
   */
  @Override
  public boolean removeCustomer(String name) throws RemoteException {
    String sql = "UPDATE Clients SET isactive = 0 WHERE Fullname = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Selects all profile names from Clients table filter structured where state
   * equals 1.
   */
  @Override
  public List<String> getActiveCustomers() throws RemoteException {
    List<String> clients = new ArrayList<>();
    String sql = "SELECT Fullname FROM Clients WHERE isactive = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        clients.add(rs.getString("Fullname"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return clients;
  }

  // --- TRANSACTIONAL RENTAL MANAGEMENT ---

  /**
   * {@inheritDoc}
   * Maps client name and movie title strings into IDs using sub-queries to insert
   * a transaction record.
   */
  @Override
  public boolean saveRental(String customerName, String movieTitle) throws RemoteException {
    String sql = "INSERT INTO Rentals(client_id, movie_id, Returned) " +
        "VALUES ((SELECT id FROM Clients WHERE Fullname = ?), (SELECT id FROM Movies WHERE Title = ?), 0)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, customerName);
      pstmt.setString(2, movieTitle);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Matches IDs via nested sub-queries to close an unreturned (0) rental row with
   * a returned status (1).
   */
  @Override
  public boolean returnMovie(String customerName, String movieTitle) throws RemoteException {
    String sql = "UPDATE Rentals SET Returned = 1 WHERE client_id = (SELECT id FROM Clients WHERE Fullname = ?) " +
        "AND movie_id = (SELECT id FROM Movies WHERE Title = ?) AND Returned = 0";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, customerName);
      pstmt.setString(2, movieTitle);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@inheritDoc}
   * Performs an explicit multi-table join operation to query active unreturned
   * checkouts.
   */
  @Override
  public List<String> getBorrowedMovies(String customerName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Rentals r JOIN Movies m ON r.movie_id = m.id " +
        "JOIN Clients c ON r.client_id = c.id WHERE c.Fullname = ? AND r.Returned = 0";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, customerName);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          movies.add(rs.getString("Title"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return movies;
  }

  /**
   * {@inheritDoc}
   * Multi-table join identifying historical rental rows where returned flag
   * equals 1.
   */
  @Override
  public List<String> getReturnedMovies(String customerName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Rentals r JOIN Movies m ON r.movie_id = m.id " +
        "JOIN Clients c ON r.client_id = c.id WHERE c.Fullname = ? AND r.Returned = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, customerName);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          movies.add(rs.getString("Title"));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return movies;
  }
}