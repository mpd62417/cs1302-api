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
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

import java.util.Optional;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * App that converts city name and state to latitude and longitude, finds the first 45 birds
 * within 50 kilometers of that location, and creates buttons with a link to a Wikipedia article
 * for each of those birds.
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

    private static final String API_KEY = "dhlbapd0h4nv";

    private static final String[] STATES = {
        "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL",
        "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
        "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI",
        "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        };

    Stage stage;
    Scene scene;
    VBox root;
    ImageView banner;
    HBox urlLayer;
    TextField url;
    Text searchMessage;
    MenuButton dropdown;

    Button searchButton;
    HBox instLayer;
    Text instructions;
    Label notice;
    String stateName = "GA";
    String query;
    String uri;
    ZipResponse zipResponse;
    BirdResponse birdResponse;
    BirdResult[] birdResults;
    Alert alert = new Alert(AlertType.NONE);
    HBox row1;
    HBox row2;
    HBox row3;
    StackPane stackPane;
    Button forward;
    Button backward;
    Button backToBirds;
    HBox bottomLayer;
    WebView webView;
    int count = 0;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
        this.stage = stage;
        urlLayer = new HBox();
        urlLayer.setPrefHeight(20);
        url = new TextField("Athens");
        HBox.setHgrow(url, Priority.ALWAYS);

        searchMessage = new Text("Search: ");
        searchButton = new Button("Search");

        instLayer = new HBox();
        instLayer.setPrefHeight(40);
        instructions = new Text("Enter your U.S. city name, pick the state of the city, "
            + "and press enter to find birds in your area! The default location is Athens, GA.");
        instructions.setWrappingWidth(500);
        dropdown = new MenuButton("GA");
        stackPane = new StackPane();
        forward = new Button("Next Page");
        backward = new Button("Previous Page");
        backToBirds = new Button("Back to Birds");
        bottomLayer = new HBox();
        bottomLayer.setPrefHeight(40);
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        urlLayer.getChildren().addAll(searchMessage, url, dropdown, searchButton);
        instLayer.getChildren().addAll(instructions);

        searchButton.setOnAction(e -> runNow(() -> {
            count = 0;
            searchButton.setDisable(true);
            instructions.setText("Finding birds in your area...");
            locationRequest();
            setLinks();
        }));

        for (int i = 0; i < 50; i ++) {
            MenuItem state = new MenuItem(STATES[i]);
            state.setOnAction(e -> {
                stateName = state.getText();
                dropdown.setText(stateName);
            });
            dropdown.getItems().add(state);
        } // for

        forward.setOnAction(e -> {
            backward.setDisable(false);
            count += 9;
            setLinks();
            if (count == 36) {
                forward.setDisable(true);
            } // if
        });


        backward.setOnAction(e -> {
            count -= 9;
            setLinks();
            if (count == 0) {
                backward.setDisable(true);
            } // if
        });


        System.out.println("init called");
    } // init


    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        // demonstrate how to load local asset using "file:resources/"
        Image defaultImage = new Image("file:resources/czNmcy1wcml2YXRlL3Jhd3BpeGV" +
            "sX2ltYWdlcy93ZWJzaXRlX2NvbnRlbnQvdHAyNTAtYmFja2dyb3VuZC0xMS14LWt6cnYzNGh4LmpwZw.jpg");
        banner = new ImageView(defaultImage);
        banner.setFitHeight(450);
        banner.setFitWidth(600);

        stackPane.getChildren().add(banner);
        stackPane.setPrefHeight(450);

        bottomLayer.setSpacing(112.5);
        backToBirds.setPrefWidth(125);
        backward.setPrefWidth(125);
        forward.setPrefWidth(125);

        backToBirds.setDisable(true);
        backward.setDisable(true);
        forward.setDisable(true);
        bottomLayer.getChildren().addAll(backward, backToBirds, forward);
        // setup scene
        root.getChildren().addAll(urlLayer,instLayer, stackPane, bottomLayer);
        scene = new Scene(root, 600, 550);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start


    /**
     * Creates new thread.
     *
     * @param target the Runnable
     */
    private static void runNow(Runnable target) {
        Thread loadThread = new Thread(target);
        loadThread.setDaemon(true);
        loadThread.start();
    } // runNow

    /**
     * Sends http request to Zippopotam.us API for
     * latitude and longitude from selected city name and state.
     */
    private void locationRequest() {
        try {
            query = "http://api.zippopotam.us/us/" + stateName + "/" + url.getText();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(query))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                instructions.setText("Last attempt to get location failed..."
                    + "\nMake sure your city is spelled right and the correct state is chosen!");
                throw new IOException(response.toString());
            } // if

            String jsonString = response.body();
            zipResponse = GSON.fromJson(jsonString, ZipResponse.class);
            eBirdRequest();

        } catch (IOException | InterruptedException |
            IllegalArgumentException | IllegalStateException e) {
            instructions.setText("Last attempt to get birds failed. Try a different city.");
            alertError(e);
        } // try

    } // locationRequest



    /**
     * Sends http request to eBird API for
     * the list of birds within a 50 kilometer radius of the coordinates.
     */
    private void eBirdRequest() {
        double lat = zipResponse.getLatitude();
        double lng = zipResponse.getLongitude();

        try {
            uri = "https://api.ebird.org/v2/data/obs/geo/recent?lat="
                + lat + "&lng=" + lng + "&limit=45";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("X-eBirdApiToken", API_KEY)
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                instructions.setText("Last attempt to get birds failed..."
                    + "Your city might not have enough bird sightings.");
                throw new IOException(response.toString());
            } // if

            String jsonString = response.body();
            birdResults = GSON.fromJson(jsonString, BirdResult[].class);
            for (int i = 0; i < birdResults.length; i ++) {
                String bird = birdResults[i].comName;
            } // for

        } catch (IOException | InterruptedException |
            IllegalArgumentException | IllegalStateException e) {
            instructions.setText("Last attempt to get birds failed. Try a different city.");
            alertError(e);
        } // try

    } // locationRequest

    /**
     * Sends a request to retrieve a link to a wikipedia article
     * for the bird called and opens that article if the bird button
     * is pressed.
     *
     * @param bird the String to find a wikipedia article for
     */
    private void wikiRequest(String bird) {
        try {
            String uri = String.format("%s?action=%s&format=%s&titles=%s&prop=%s&inprop=%s&limit=",
                "https://en.wikipedia.org/w/api.php", "query", "json",
                URLEncoder.encode(bird, "UTF-8"), "info", "url", "1");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                instructions.setText("Last attempt to get articles failed...");
                throw new IOException(response.toString());
            } // if

            String jsonString = response.body();
            WikiResponse wikiResponse = GSON.fromJson(jsonString, WikiResponse.class);
            String wikiUrl = wikiResponse.getUrl();
            webView = new WebView();
            webView.getEngine().load(wikiUrl);
            stackPane.getChildren().add(webView);
            instructions.setText("Article for " + bird);
            forward.setDisable(true);
            backward.setDisable(true);
            backToBirds.setDisable(false);
            count = 0;
            backToBirds.setOnAction(e -> setLinks());
        } catch (IOException | InterruptedException |
            IllegalArgumentException | IllegalStateException e) {
            instructions.setText("Last attempt to get birds failed. Try a different city.");
            alertError(e);
        } // try
    } // wikiRequest

    /**
     * Sets the screen with nine buttons with links to the birds in the
     * specified area.
     */
    private void setLinks() {
        backToBirds.setDisable(true);
        forward.setDisable(false);
        row1 = new HBox();
        row2 = new HBox();
        row3 = new HBox();
        if (birdResults != null) {
            try {
                for (int i = 0; i < 3; i ++) {
                    Button button = new Button(birdResults[i + count].comName);
                    button.setPrefSize(200, 150);
                    button.setWrapText(true);
                    button.setOnAction(e -> wikiRequest(button.getText()));
                    row1.getChildren().add(button);
                } // for

                for (int i = 3; i < 6; i ++) {
                    Button button = new Button(birdResults[i + count].comName);
                    button.setPrefSize(200, 150);
                    button.setWrapText(true);
                    button.setOnAction(e -> wikiRequest(button.getText()));
                    row2.getChildren().add(button);
                } // for

                for (int i = 6; i < 9; i ++) {
                    Button button = new Button(birdResults[i + count].comName);
                    button.setPrefSize(200, 150);
                    button.setWrapText(true);
                    button.setOnAction(e -> wikiRequest(button.getText()));
                    row3.getChildren().add(button);
                } // for
                row2.setTranslateY(150);
                row3.setTranslateY(300);
                Platform.runLater(() -> stackPane.getChildren().remove(banner));
                Platform.runLater(() -> stackPane.getChildren().addAll(row1, row2, row3));
                searchButton.setDisable(false);
                instructions.setText("Birds seen within 50 kilometers of "
                    + zipResponse.getPlaceName() + ", " + stateName);

            } catch (ArrayIndexOutOfBoundsException e) {
                searchButton.setDisable(false);
                instructions.setText("Not enough birds in " + zipResponse.getPlaceName()
                    + ", " + stateName + ". Try a different city.");
                alertError(e);
            } // try
        } // if
    } // setLinks

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
        instructions.setText("Last attempt to get birds failed. Try a different city.");
        searchButton.setDisable(false);
        TextArea text = new TextArea("URI: " + query + "\n\nException: " + cause.toString());
        text.setEditable(false);
        Platform.runLater(() -> alert.setAlertType(AlertType.ERROR));
        alert.getDialogPane().setContent(text);
        alert.setResizable(true);
        Platform.runLater(() -> alert.showAndWait());
    } // alertError

} // ApiApp
