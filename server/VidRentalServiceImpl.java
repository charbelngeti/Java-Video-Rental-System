package server;

import common.IVidRentalService;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI Service Implementation executing JDBC operations against the VLS
 * database.
 */
public class VidRentalServiceImpl extends UnicastRemoteObject implements IVidRentalService {

  // Database Connection URL (Using SQLite for ease of distribution)
  private static final String DB_URL = "jdbc:sqlite:vls_database.db";

  public VidRentalServiceImpl() throws RemoteException {
    super();
    // Initialize database file and connection verification upon boot
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      if (conn != null)
        System.out.println("JDBC connected successfully to Server space database.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // --- GENRE OPERATIONS ---
  @Override
  public boolean saveGenre(String name) throws RemoteException {
    String sql = "INSERT INTO Genres(genre, isactive) VALUES(?, 1)"; // 1 = listed [cite: 137]
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean removeGenre(String name) throws RemoteException {
    String sql = "UPDATE Genres SET isactive = 0 WHERE genre = ?"; // 0 = unlisted [cite: 137]
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public List<String> getActiveGenres() throws RemoteException {
    List<String> genres = new ArrayList<>();
    String sql = "SELECT genre FROM Genres WHERE isactive = 1"; // Fetch listed genres [cite: 137]
    try (Connection conn = DriverManager.getConnection(DB_URL);
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
  @Override
  public boolean saveMovie(String genreName, String title) throws RemoteException {
    String sql = "INSERT INTO Movies(genre_id, Title, isactive) SELECT id, ?, 1 FROM Genres WHERE genre = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public List<String> getMoviesByGenre(String genreName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Movies m JOIN Genres g ON m.genre_id = g.id WHERE g.genre = ? AND m.isactive = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public boolean removeMovie(String title) throws RemoteException {
    String sql = "UPDATE Movies SET isactive = 0 WHERE Title = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
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
  @Override
  public boolean saveCustomer(String name, String phone, String email) throws RemoteException {
    String sql = "INSERT INTO Clients(Fullname, phone, email, isactive) VALUES(?, ?, ?, 1)";
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public boolean removeCustomer(String name) throws RemoteException {
    String sql = "UPDATE Clients SET isactive = 0 WHERE Fullname = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public List<String> getActiveCustomers() throws RemoteException {
    List<String> clients = new ArrayList<>();
    String sql = "SELECT Fullname FROM Clients WHERE isactive = 1";
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  // --- TRANSACTIONAL RENTAL MANAGEMENT (Member 3 Targets) ---
  @Override
  public boolean saveRental(String customerName, String movieTitle) throws RemoteException {
    String sql = "INSERT INTO Rentals(client_id, movie_id, Returned) " +
        "VALUES ((SELECT id FROM Clients WHERE Fullname = ?), (SELECT id FROM Movies WHERE Title = ?), 0)"; // 0 =
                                                                                                            // borrowed
                                                                                                            // [cite:
                                                                                                            // 141]
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public boolean returnMovie(String customerName, String movieTitle) throws RemoteException {
    String sql = "UPDATE Rentals SET Returned = 1 WHERE client_id = (SELECT id FROM Clients WHERE Fullname = ?) " +
        "AND movie_id = (SELECT id FROM Movies WHERE Title = ?) AND Returned = 0"; // 1 = returned [cite: 141]
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public List<String> getBorrowedMovies(String customerName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Rentals r JOIN Movies m ON r.movie_id = m.id " +
        "JOIN Clients c ON r.client_id = c.id WHERE c.Fullname = ? AND r.Returned = 0"; // 0 = borrowed [cite: 141]
    try (Connection conn = DriverManager.getConnection(DB_URL);
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

  @Override
  public List<String> getReturnedMovies(String customerName) throws RemoteException {
    List<String> movies = new ArrayList<>();
    String sql = "SELECT m.Title FROM Rentals r JOIN Movies m ON r.movie_id = m.id " +
        "JOIN Clients c ON r.client_id = c.id WHERE c.Fullname = ? AND r.Returned = 1"; // 1 = returned [cite: 141]
    try (Connection conn = DriverManager.getConnection(DB_URL);
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