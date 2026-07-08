package src;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import common.IVidRentalService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Serves as the primary Administrative Client for the Distributed Video Library
 * System.
 * Provides a JavaFX graphical interface for managing library genres, movie
 * inventories,
 * and customer demographics via Remote Method Invocation (RMI).
 */
public class AdminDashboard extends Application {

    private IVidRentalService serverStub;

    // Change this to match your Server's machine IP address when deploying on
    // separate computers
    private static final String SERVER_IP = "10.75.126.75";

    /**
     * Initializes the JavaFX application lifecycle and constructs the main
     * dashboard layout.
     *
     * @param primaryStage The primary window provided by the JavaFX runtime
     *                     environment.
     */
    @Override
    public void start(Stage primaryStage) {
        connectToServer();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createGenresTab(),
                createMoviesTab(),
                createCustomersTab());

        Scene scene = new Scene(new VBox(tabPane), 500, 400);
        primaryStage.setTitle("Video Library System - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Establishes the remote connection to the central Video Library Server.
     * Binds the local serverStub to the remote registry instance.
     */
    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_IP, 1099);
            serverStub = (IVidRentalService) registry.lookup("VideoLibraryService");
            System.out.println("Admin System successfully linked to RMI Registry.");
        } catch (Exception e) {
            System.err.println("RMI Connection Failed. Ensure Server is running on: " + SERVER_IP);
            e.printStackTrace();
        }
    }

    /**
     * Constructs the administrative interface for managing library genres.
     * Provides text inputs to register new categories and buttons to alter their
     * status.
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

        // Populate initial dropdown items
        refreshGenres(registeredGenres);

        saveBtn.setOnAction(e -> {
            try {
                String input = nameField.getText().trim();
                if (serverStub != null && !input.isEmpty()) {
                    if (serverStub.saveGenre(input)) {
                        refreshGenres(registeredGenres);
                        nameField.clear();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        removeBtn.setOnAction(e -> {
            try {
                String selected = registeredGenres.getValue();
                if (serverStub != null && selected != null) {
                    if (serverStub.removeGenre(selected)) {
                        refreshGenres(registeredGenres);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tab.setContent(pane);
        return tab;
    }

    /**
     * Constructs the administrative interface for managing the movie inventory.
     * Features dynamic cascading event listeners to filter movies by their
     * associated genre.
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

        // Context reload when tab shifts focus
        tab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal)
                refreshGenres(genreBox);
        });
        refreshGenres(genreBox);

        // Cascading Logic: Selecting genre updates registered movies safely via text
        genreBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                refreshMovies(newVal, registeredMovies);
            } else {
                registeredMovies.getItems().clear();
            }
        });

        saveBtn.setOnAction(e -> {
            try {
                String selectedGenre = genreBox.getValue();
                String movieTitle = movieNameField.getText().trim();
                if (serverStub != null && selectedGenre != null && !movieTitle.isEmpty()) {
                    if (serverStub.saveMovie(selectedGenre, movieTitle)) {
                        refreshMovies(selectedGenre, registeredMovies);
                        movieNameField.clear();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        removeBtn.setOnAction(e -> {
            try {
                String selectedGenre = genreBox.getValue();
                String selectedMovie = registeredMovies.getValue();
                if (serverStub != null && selectedMovie != null) {
                    if (serverStub.removeMovie(selectedMovie)) {
                        refreshMovies(selectedGenre, registeredMovies);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tab.setContent(pane);
        return tab;
    }

    /**
     * Constructs the administrative interface for managing library patrons.
     * Captures essential demographics and commits them to the database.
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

        pane.add(new Label("Name:"), 0, 0);
        pane.add(nameField, 1, 0);
        pane.add(new Label("Phone:"), 0, 1);
        pane.add(phoneField, 1, 1);
        pane.add(new Label("Email:"), 0, 2);
        pane.add(emailField, 1, 2);
        pane.add(new Label("Registered:"), 0, 3);
        pane.add(registeredCustomers, 1, 3);
        pane.add(saveBtn, 0, 4);
        pane.add(removeBtn, 1, 4);

        refreshCustomers(registeredCustomers);

        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                if (serverStub != null && !name.isEmpty()) {
                    if (serverStub.saveCustomer(name, phone, email)) {
                        refreshCustomers(registeredCustomers);
                        nameField.clear();
                        phoneField.clear();
                        emailField.clear();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        removeBtn.setOnAction(e -> {
            try {
                String selected = registeredCustomers.getValue();
                if (serverStub != null && selected != null) {
                    if (serverStub.removeCustomer(selected)) {
                        refreshCustomers(registeredCustomers);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        tab.setContent(pane);
        return tab;
    }

    /**
     * Queries the RMI server for active genres and populates the provided ComboBox.
     *
     * @param box The ComboBox element to be refreshed.
     */
    private void refreshGenres(ComboBox<String> box) {
        try {
            if (serverStub != null)
                box.getItems().setAll(serverStub.getActiveGenres());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Queries the RMI server for movies associated with a specific genre.
     *
     * @param genre The String representation of the genre to filter by.
     * @param box   The ComboBox element to be refreshed with the filtered results.
     */
    private void refreshMovies(String genre, ComboBox<String> box) {
        try {
            if (serverStub != null && genre != null)
                box.getItems().setAll(serverStub.getMoviesByGenre(genre));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Queries the RMI server for active customers and populates the provided
     * ComboBox.
     *
     * @param box The ComboBox element to be refreshed.
     */
    private void refreshCustomers(ComboBox<String> box) {
        try {
            if (serverStub != null)
                box.getItems().setAll(serverStub.getActiveCustomers());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Generates a standardized GridPane layout to ensure consistent padding
     * and gap spacing across all administrative tabs.
     *
     * @return A configured GridPane instance.
     */
    private GridPane createBaseGrid() {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(20));
        pane.setHgap(10);
        pane.setVgap(10);
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}