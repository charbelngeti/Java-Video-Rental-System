import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AdminDashboard extends Application {

    private IVidRentalService serverStub;

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createGenresTab(),
                createMoviesTab(),
                createCustomersTab()
        );

        Scene scene = new Scene(new VBox(tabPane), 500, 400);
        primaryStage.setTitle("Video Library System - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    /**
     * Establishes the remote connection to the central Video Library Server.
     * Must target the exact IP address defined by the server host.
     */
    private void connectToServer() {
        try {
            // Must point to the server's exact IP to avoid connection refused errors
            Registry registry = LocateRegistry.getRegistry("192.168.1.100", 1099);
            serverStub = (IVidRentalService) registry.lookup("VideoLibraryService");
            System.out.println("Connected to RMI Server.");
        } catch (Exception e) {
            System.err.println("RMI Connection Failed. Is the server running?");
            e.printStackTrace();
        }
    }
    /**
     * Constructs the administrative interface for managing library genres.
     * Provides text inputs to register new categories and buttons to alter their status.
     * Synchronizes the registered genres list with the central database via RMI.
     *
     * @return A constructed Tab object containing the Genres GUI layout.
     */
    private Tab createGenresTab() {
        Tab tab = new Tab("1. Genres");
        tab.setClosable(false);
        GridPane pane = createBaseGrid();

        TextField nameField = new TextField();
        ComboBox<String> registeredGenres = new ComboBox<>();
        Button saveBtn = new Button("Save");
        Button removeBtn = new Button("Remove");

        pane.add(new Label("Genre Name:"), 0, 0);
        pane.add(nameField, 1, 0);
        pane.add(new Label("Registered:"), 0, 1);
        pane.add(registeredGenres, 1, 1);
        pane.add(saveBtn, 0, 2);
        pane.add(removeBtn, 1, 2);

        saveBtn.setOnAction(e -> {
            try {
                if (serverStub != null) {
                    serverStub.saveGenre(nameField.getText());
                    registeredGenres.getItems().setAll(serverStub.getActiveGenres());
                    nameField.clear();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        tab.setContent(pane);
        return tab;
    }
    /**
     * Constructs the administrative interface for managing the movie inventory.
     * Features dynamic cascading event listeners: selecting a specific genre
     * automatically queries the server to filter and display only the movies
     * associated with that ID.
     *
     * @return A constructed Tab object containing the Movies GUI layout.
     */
    private Tab createMoviesTab() {
        Tab tab = new Tab("2. Movies");
        tab.setClosable(false);
        GridPane pane = createBaseGrid();

        ComboBox<String> genreBox = new ComboBox<>();
        TextField movieNameField = new TextField();
        ComboBox<String> registeredMovies = new ComboBox<>();
        Button saveBtn = new Button("Save");
        Button removeBtn = new Button("Remove");

        pane.add(new Label("Select Genre:"), 0, 0);
        pane.add(genreBox, 1, 0);
        pane.add(new Label("Movie Name:"), 0, 1);
        pane.add(movieNameField, 1, 1);
        pane.add(new Label("Registered:"), 0, 2);
        pane.add(registeredMovies, 1, 2);
        pane.add(saveBtn, 0, 3);
        pane.add(removeBtn, 1, 3);

        // Cascading Logic: Selecting genre updates registered movies
        genreBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() >= 0) {
                try {
                    // Assuming the index aligns with ID for simplicity. Update with object IDs if applicable.
                    int genreId = newVal.intValue() + 1;
                    registeredMovies.getItems().setAll(serverStub.getMoviesByGenre(genreId));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        tab.setContent(pane);
        return tab;
    }
    /**
     * Constructs the administrative interface for managing library patrons.
     * Captures essential demographics (Name, Phone, Email) and commits them
     * to the database. Refreshes the active customer registry upon successful saves.
     *
     * @return A constructed Tab object containing the Customers GUI layout.
     */
    private Tab createCustomersTab() {
        Tab tab = new Tab("3. Customers");
        tab.setClosable(false);
        GridPane pane = createBaseGrid();

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        ComboBox<String> registeredCustomers = new ComboBox<>();
        Button saveBtn = new Button("Save");
        Button removeBtn = new Button("Remove");

        pane.add(new Label("Name:"), 0, 0); pane.add(nameField, 1, 0);
        pane.add(new Label("Phone:"), 0, 1); pane.add(phoneField, 1, 1);
        pane.add(new Label("Email:"), 0, 2); pane.add(emailField, 1, 2);
        pane.add(new Label("Registered:"), 0, 3); pane.add(registeredCustomers, 1, 3);
        pane.add(saveBtn, 0, 4); pane.add(removeBtn, 1, 4);

        saveBtn.setOnAction(e -> {
            try {
                serverStub.saveCustomer(nameField.getText(), phoneField.getText(), emailField.getText());
                registeredCustomers.getItems().setAll(serverStub.getActiveCustomers());
                nameField.clear(); phoneField.clear(); emailField.clear();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        tab.setContent(pane);
        return tab;
    }

    private GridPane createBaseGrid() {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(20));
        pane.setHgap(10);
        pane.setVgap(10);
        return pane;
    }

    public static void main(String[] args) { launch(args); }
}