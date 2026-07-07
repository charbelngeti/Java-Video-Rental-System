import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IVidRentalService extends Remote {

    // Administrative Operations
    boolean saveGenre(String name) throws RemoteException;
    boolean removeGenre(int id) throws RemoteException;
    List<String> getActiveGenres() throws RemoteException;

    boolean saveMovie(int genreId, String title) throws RemoteException;
    boolean removeMovie(int id) throws RemoteException;
    List<String> getMoviesByGenre(int genreId) throws RemoteException;

    boolean saveCustomer(String name, String phone, String email) throws RemoteException;
    boolean removeCustomer(int id) throws RemoteException;
    List<String> getActiveCustomers() throws RemoteException;

    // Transactional Customer Operations
    boolean saveRental(int clientId, int newMovieId) throws RemoteException;
    boolean returnMovie(int rentalId) throws RemoteException;
    List<String> getBorrowedMovies(int clientId) throws RemoteException;
    List<String> getReturnedMovies(int clientId) throws RemoteException;
}