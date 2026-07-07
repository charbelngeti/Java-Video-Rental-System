import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Handles the graphical interface for customer rental transactions.
 * Connects to the central RMI registry to process checkouts and returns.
 */
public class CustomerDashboard extends Application {

    private IVidRentalService serverStub;

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        GridPane pane = new GridPane();
        pane.setPadding(new Insets(20));
        pane.setHgap(15); pane.setVgap(15);

        ComboBox<String> customerBox = new ComboBox<>();
        ComboBox<String> genreBox = new ComboBox<>();
        ComboBox<String> movieBox = new ComboBox<>();
        ComboBox<String> borrowedBox = new ComboBox<>();
        ComboBox<String> returnedBox = new ComboBox<>();

        Button saveRentalBtn = new Button("Save Rental");
        Button returnMovieBtn = new Button("Return Movie");
        Label statusAlert = new Label("Status: Ready");

        pane.add(new Label("1. Select Customer:"), 0, 0); pane.add(customerBox, 1, 0);
        pane.add(new Label("2. Select Genre:"), 0, 1); pane.add(genreBox, 1, 1);
        pane.add(new Label("3. Select Movie:"), 0, 2); pane.add(movieBox, 1, 2);
        pane.add(new Label("Currently Borrowed:"), 0, 3); pane.add(borrowedBox, 1, 3);
        pane.add(new Label("Historically Returned:"), 0, 4); pane.add(returnedBox, 1, 4);
        pane.add(saveRentalBtn, 0, 5); pane.add(returnMovieBtn, 1, 5);
        pane.add(statusAlert, 0, 6, 2, 1);

        // Logic 1: Genre filters Movies
        genreBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) {
                try {
                    int genreId = newVal.intValue() + 1;
                    movieBox.getItems().setAll(serverStub.getMoviesByGenre(genreId));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        // Logic 2: Customer selection refreshes their Borrowed and Returned lists
        customerBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) {
                try {
                    int customerId = newVal.intValue() + 1;
                    borrowedBox.getItems().setAll(serverStub.getBorrowedMovies(customerId));
                    returnedBox.getItems().setAll(serverStub.getReturnedMovies(customerId));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        // Logic 3: Save Rental updates the database and refreshes the view immediately
        saveRentalBtn.setOnAction(e -> {
            try {
                int cid = customerBox.getSelectionModel().getSelectedIndex() + 1;
                int mid = movieBox.getSelectionModel().getSelectedIndex() + 1;

                boolean success = serverStub.saveRental(cid, mid);
                if (success) {
                    borrowedBox.getItems().setAll(serverStub.getBorrowedMovies(cid));
                    statusAlert.setText("Status: Rental successful!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Scene scene = new Scene(pane, 450, 350);
        primaryStage.setTitle("Video Library System - Customer Rentals");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Establishes the remote connection to the central Video Library Server.
     * Must target the exact IP address defined by the server host.
     */
    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("192.168.1.100", 1099);
            serverStub = (IVidRentalService) registry.lookup("VideoLibraryService");
        } catch (Exception e) {
            System.err.println("RMI Connection Failed. Verify server is online.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { launch(args); }
}