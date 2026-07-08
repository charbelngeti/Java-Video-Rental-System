package src;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import common.IVidRentalService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * CustomerDashboard provides a distributed JavaFX user interface for Client 2
 * (Customer).
 * It connects via Java Remote Method Invocation (RMI) to a central Video
 * Library Service
 * to handle transactional operations such as renting videos, tracking borrowed
 * inventories,
 * and processing video returns.
 * * @author Abigael Wambui / Group Members
 * 
 * @version 1.0
 */
public class CustomerDashboard extends Application {

    /**
     * The remote stub interface used to call database operations hosted on the
     * Server.
     */
    private IVidRentalService serverStub;

    /**
     * * The static IP address targeting the remote Server machine hosting the RMI
     * registry.
     * Change this to match your Server's machine IP address when deploying on
     * separate computers.
     */
    private static final String SERVER_IP = "192.168.43.82";

    /** Dropdown list component containing all registered active customers. */
    private ComboBox<String> customerBox = new ComboBox<>();

    /** Dropdown list component displaying available genres. */
    private ComboBox<String> genreBox = new ComboBox<>();

    /**
     * Dropdown list component dynamically updated to show movies matching the
     * selected genre.
     */
    private ComboBox<String> movieBox = new ComboBox<>();

    /**
     * Dropdown list component containing movies currently borrowed by the selected
     * customer.
     */
    private ComboBox<String> borrowedBox = new ComboBox<>();

    /**
     * Dropdown list component displaying previously returned movies for the
     * selected customer.
     */
    private ComboBox<String> returnedBox = new ComboBox<>();

    /** Button to commit a new video rental assignment into the database. */
    private Button saveRentalBtn = new Button("Save rental");

    /** Button to update a selected rental record status to returned. */
    private Button returnMovieBtn = new Button("Return Movie");

    /**
     * Text object for displaying real-time success logs or network connectivity
     * issues.
     */
    private Text statusAlert = new Text();

    /**
     * The primary entry point for the JavaFX runtime application life cycle.
     * Sets up the RMI registry lookup to the Server, builds the GridPane layout
     * framework,
     * assigns element styling, and attaches event listeners to manage dynamic
     * behaviors.
     *
     * @param stage The primary Window Stage container configured by the platform.
     */
    @Override
    public void start(Stage stage) {
        try {
            // Target the remote server host registry port 1099
            Registry registry = LocateRegistry.getRegistry(SERVER_IP, 1099);
            serverStub = (IVidRentalService) registry.lookup("VideoLibraryService");

            // Initial lookup data pull
            genreBox.getItems().setAll(serverStub.getActiveGenres());
            customerBox.getItems().setAll(serverStub.getActiveCustomers());
        } catch (Exception e) {
            statusAlert.setText("Server connection offline. Verify registry status.");
            e.printStackTrace();
        }

        GridPane gridPane = new GridPane();
        gridPane.setMinSize(600, 500);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setVgap(15);
        gridPane.setHgap(15);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setStyle("-fx-background-color: BEIGE;"); // Required custom palette rule

        String primaryButtonStyle = "-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size:12pt; -fx-font-weight: bold;";
        saveRentalBtn.setStyle(primaryButtonStyle);
        returnMovieBtn.setStyle(primaryButtonStyle);
        statusAlert.setStyle("-fx-font: normal bold 14px 'serif';");

        gridPane.add(new Label("Customer:"), 0, 0);
        gridPane.add(customerBox, 1, 0);

        gridPane.add(new Label("Genre:"), 0, 1);
        gridPane.add(genreBox, 1, 1);

        gridPane.add(new Label("Movies:"), 0, 2);
        gridPane.add(movieBox, 1, 2);

        gridPane.add(saveRentalBtn, 1, 3);

        gridPane.add(new Label("Borrowed:"), 0, 4);
        gridPane.add(borrowedBox, 1, 4);

        gridPane.add(returnMovieBtn, 1, 5);

        gridPane.add(new Label("Returned:"), 0, 6);
        gridPane.add(returnedBox, 1, 6);

        gridPane.add(statusAlert, 1, 7);

        // Dynamic Filtering: Switch movie listings based on picked context category
        genreBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    List<String> associatedMovies = serverStub.getMoviesByGenre(newVal);
                    movieBox.getItems().setAll(associatedMovies);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        customerBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                refreshCustomerInventories(newVal);
            }
        });

        saveRentalBtn.setOnAction(event -> {
            try {
                String selectedCustomer = customerBox.getSelectionModel().getSelectedItem();
                String selectedMovie = movieBox.getSelectionModel().getSelectedItem();

                if (selectedCustomer != null && selectedMovie != null) {
                    boolean completed = serverStub.saveRental(selectedCustomer, selectedMovie);
                    if (completed) {
                        statusAlert.setText("Rental transacted successfully.");
                        refreshCustomerInventories(selectedCustomer);
                    }
                }
            } catch (Exception ex) {
                statusAlert.setText("Error executing transaction checkout.");
                ex.printStackTrace();
            }
        });

        returnMovieBtn.setOnAction(event -> {
            try {
                String selectedCustomer = customerBox.getSelectionModel().getSelectedItem();
                String selectedMovie = borrowedBox.getSelectionModel().getSelectedItem();

                if (selectedCustomer != null && selectedMovie != null) {
                    boolean completed = serverStub.returnMovie(selectedCustomer, selectedMovie);
                    if (completed) {
                        statusAlert.setText("Movie returned successfully.");
                        refreshCustomerInventories(selectedCustomer);
                    }
                }
            } catch (Exception ex) {
                statusAlert.setText("Error executing return transaction.");
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(gridPane);
        stage.setTitle("VLS Customer Rentals Module - Client 2");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Helper routine that hits remote interface endpoints to update both the
     * borrowed and
     * returned history collection boxes for a specific customer.
     *
     * @param customerName The Fullname designation identifier belonging to the
     *                     chosen customer.
     */
    private void refreshCustomerInventories(String customerName) {
        try {
            borrowedBox.getItems().setAll(serverStub.getBorrowedMovies(customerName));
            returnedBox.getItems().setAll(serverStub.getReturnedMovies(customerName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Execution container entry method that invokes launch procedures for JavaFX
     * applications.
     *
     * @param args Array collection containing passed invocation parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }
}