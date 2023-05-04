package cs1302.api;

import java.net.http.HttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import java.util.Optional;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

        /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    public static final String API_KEY = "dhlbapd0h4nv";

    Stage stage;
    Scene scene;
    VBox root;

    HBox urlLayer;
    TextField url;
    Text searchMessage;
    MenuButton dropdown;
    MenuItem zip;
    MenuItem address;
    Button searchButton;
    HBox instLayer;
    Text instructions;

    String query;
    String uri;
    ZipResponse zipResponse;
    BirdResponse birdResponse;
    BirdResult[] birdResults;
    Alert alert = new Alert(AlertType.NONE);

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        this.stage = stage;
        urlLayer = new HBox();
        url = new TextField("30609");
        HBox.setHgrow(url, Priority.ALWAYS);

        searchMessage = new Text("Search: ");
        searchButton = new Button("Search");

        instLayer = new HBox();
        instructions = new Text("Enter zip code and press enter to see birds in your area!");

        zip = new MenuItem("Zipcode");
        address = new MenuItem("Address");
        dropdown = new MenuButton("Zipcode", null, zip, address);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        urlLayer.getChildren().addAll(searchMessage, url, dropdown, searchButton);
        instLayer.getChildren().addAll(instructions);
        System.out.println("init called");

        // address.setOnAction(e -> encodeMedia("address"));
        searchButton.setOnAction(e -> locationRequest());

    } // init


    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);

        // some labels to display information
        Label notice = new Label("Modify the starter code to suit your needs.");

        // setup scene
        root.getChildren().addAll(urlLayer,instLayer, banner, notice);
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**
     * Sends http request to Zippopotam.us API for
     * latitude and longitude.
     */
    private void locationRequest() {
        try {
            query = "http://api.zippopotam.us/us/" + url.getText();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            String message = "Response failed";
            if (response.statusCode() != 200) {
                instructions.setText("Last attempt to get images failed...");
                throw new IOException(response.toString());
            } // if

            String jsonString = response.body();
            zipResponse = GSON.fromJson(jsonString, ZipResponse.class);

            System.out.println("LAtitude: " + zipResponse.getLatitude());
            System.out.println("Longitude: " + zipResponse.getLongitude());
            eBirdRequest();

        } catch (IOException | InterruptedException |
            IllegalArgumentException | IllegalStateException e) {
            alertError(e);
        } // try

    } // locationRequest



    /**
     * Sends http request to eBird API for
     * latitude and longitude.
     */
    private void eBirdRequest() {
        double lat = zipResponse.getLatitude();
        double lng = zipResponse.getLongitude();

        try {
            uri = "https://api.ebird.org/v2/data/obs/geo/recent?lat="
                + lat + "&lng=" + lng + "&includeMedia=true";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("X-eBirdApiToken", API_KEY)
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            String message = "Response failed";
            if (response.statusCode() != 200) {
                instructions.setText("Last attempt to get images failed...");
                throw new IOException(response.toString());
            } // if

            String jsonString = response.body();
            birdResults = GSON.fromJson(jsonString, BirdResult[].class);
            System.out.println(birdResults);
            for (int i = 0; i < birdResults.length; i ++) {
                String bird = birdResults[i].comName;
                System.out.println(i + " : " + bird);
            } // for

        } catch (IOException | InterruptedException |
            IllegalArgumentException | IllegalStateException e) {
            alertError(e);
        } // try

    } // locationRequest

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop

    /**
     * Show a modal error alert based on {@code cause}.
     * @param cause a {@link java.lang.Throwable Throwable} that caused the alert
     */
    public void alertError(Throwable cause) {

        TextArea text = new TextArea("URI: " + query + "\n\nException: " + cause.toString());
        text.setEditable(false);
        Platform.runLater(() -> alert.setAlertType(AlertType.ERROR));
        alert.getDialogPane().setContent(text);
        alert.setResizable(true);
        Platform.runLater(() -> alert.showAndWait());
    } // alertError

} // ApiApp
