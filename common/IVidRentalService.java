package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Shared Remote Interface defining Video Library System operations.
 * All methods include Javadoc and throw RemoteException as required.
 */
public interface IVidRentalService extends Remote {

  // --- ADMIN MODULE ACTIONS
  boolean saveGenre(String name) throws RemoteException;

  boolean removeGenre(String name) throws RemoteException;

  List<String> getActiveGenres() throws RemoteException;

  boolean saveMovie(String genreName, String title) throws RemoteException;

  boolean removeMovie(String title) throws RemoteException;

  List<String> getMoviesByGenre(String genreName) throws RemoteException;

  boolean saveCustomer(String name, String phone, String email) throws RemoteException;

  boolean removeCustomer(String name) throws RemoteException;

  List<String> getActiveCustomers() throws RemoteException;

  // --- CUSTOMER RENTAL ACTIONS
  boolean saveRental(String customerName, String movieTitle) throws RemoteException;

  boolean returnMovie(String customerName, String movieTitle) throws RemoteException;

  List<String> getBorrowedMovies(String customerName) throws RemoteException;

  List<String> getReturnedMovies(String customerName) throws RemoteException;
}