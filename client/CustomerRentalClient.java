package client;

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
import common.IVidRentalService; // Imports your team's interface file smoothly now!

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class CustomerRentalClient extends Application {

    private IVidRentalService serverStub;

    // Five discrete dropdown collections matching specification rules
    private ComboBox<String> customerBox = new ComboBox<>();
    private ComboBox<String> genreBox = new ComboBox<>();
    private ComboBox<String> movieBox = new ComboBox<>();
    private ComboBox<String> borrowedBox = new ComboBox<>();
    private ComboBox<String> returnedBox = new ComboBox<>();

    private Button saveRentalBtn = new Button("Save rental");
    private Button returnMovieBtn = new Button("Return Movie");
    private Text statusAlert = new Text();

    @Override
    public void start(Stage stage) {
        try {
            // Pointing to Server Host registry port 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
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
            } catch(Exception ex) {
                statusAlert.setText("Error executing transaction checkout.");
                ex.printStackTrace();
            }
        });

        // 4. Return Movie Button: Processing returns using Customer Name and Movie Title Strings
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

    private void refreshCustomerInventories(String customerName) {
        try {
            borrowedBox.getItems().setAll(serverStub.getBorrowedMovies(customerName));
            returnedBox.getItems().setAll(serverStub.getReturnedMovies(customerName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}