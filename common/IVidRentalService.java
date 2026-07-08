package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Shared Remote Interface defining Video Library System operations.
 * Unifies all components cleanly using robust String lookups.
 */
public interface IVidRentalService extends Remote {

  // --- ADMINISTRATIVE ACTIONS

  /**
   * Saves a new movie genre to the system.
   *
   * @param name the unique name of the genre to create
   * @return true if the genre was successfully saved, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean saveGenre(String name) throws RemoteException;

  /**
   * Removes a movie genre from the system by setting its state to inactive.
   *
   * @param name the name of the genre to remove
   * @return true if the genre was successfully deactivated, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean removeGenre(String name) throws RemoteException;

  /**
   * Retrieves a list of all currently active genres in the system.
   *
   * @return a List of Strings representing the names of active genres
   * @throws RemoteException if an RMI network communication error occurs
   */
  List<String> getActiveGenres() throws RemoteException;

  /**
   * Saves a new movie and associates it with a specific genre.
   *
   * @param genreName the name of the genre this movie belongs to
   * @param title     the title of the new movie
   * @return true if the movie was successfully saved, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean saveMovie(String genreName, String title) throws RemoteException;

  /**
   * Removes a movie from the system by setting its state to inactive.
   *
   * @param title the title of the movie to remove
   * @return true if the movie was successfully deactivated, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean removeMovie(String title) throws RemoteException;

  /**
   * Retrieves all active movies belonging to a specified genre.
   *
   * @param genreName the name of the genre to filter by
   * @return a List of Strings containing the titles of movies in the specified
   *         genre
   * @throws RemoteException if an RMI network communication error occurs
   */
  List<String> getMoviesByGenre(String genreName) throws RemoteException;

  /**
   * Registers a new customer profile in the system.
   *
   * @param name  the full name of the customer
   * @param phone the contact phone number of the customer
   * @param email the contact email address of the customer
   * @return true if the customer was successfully registered, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean saveCustomer(String name, String phone, String email) throws RemoteException;

  /**
   * Removes a customer profile from the system by setting their state to
   * inactive.
   *
   * @param name the full name of the customer to remove
   * @return true if the customer profile was successfully deactivated, false
   *         otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean removeCustomer(String name) throws RemoteException;

  /**
   * Retrieves a list of all currently active registered customers.
   *
   * @return a List of Strings containing the full names of active customers
   * @throws RemoteException if an RMI network communication error occurs
   */
  List<String> getActiveCustomers() throws RemoteException;

  // --- TRANSACTIONAL RENTAL ACTIONS

  /**
   * Records a new movie rental transaction for a specific customer.
   *
   * @param customerName the full name of the borrowing customer
   * @param movieTitle   the title of the movie being rented
   * @return true if the rental log was successfully recorded, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean saveRental(String customerName, String movieTitle) throws RemoteException;

  /**
   * Updates an outstanding rental record to mark a movie as returned.
   *
   * @param customerName the full name of the returning customer
   * @param movieTitle   the title of the movie being returned
   * @return true if the rental record was successfully updated, false otherwise
   * @throws RemoteException if an RMI network communication error occurs
   */
  boolean returnMovie(String customerName, String movieTitle) throws RemoteException;

  /**
   * Retrieves all movies currently borrowed (not yet returned) by a customer.
   *
   * @param customerName the full name of the customer
   * @return a List of Strings containing the titles of currently borrowed movies
   * @throws RemoteException if an RMI network communication error occurs
   */
  List<String> getBorrowedMovies(String customerName) throws RemoteException;

  /**
   * Retrieves a historical list of all movies successfully returned by a
   * customer.
   *
   * @param customerName the full name of the customer
   * @return a List of Strings containing the titles of historically returned
   *         movies
   * @throws RemoteException if an RMI network communication error occurs
   */
  List<String> getReturnedMovies(String customerName) throws RemoteException;
}