package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Main extends Application {
    private static final String api_key = "4TDC7FAL38CFGSAO";
    private static final String connectionString = "mongodb://localhost:27017";
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    static {
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase("stockDB");
        collection = database.getCollection("stocks");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Stocker");

        Label symbolLabel = new Label("Enter Stock Symbol:");
        TextField symbolField = new TextField();
        Button getStockButton = new Button("Get Stock Data");
        Label resultLabel = new Label();

        getStockButton.setOnAction(e -> {
            String symbol = symbolField.getText();
            String response = getData(symbol);
            String result = parseData(symbol, response);
            resultLabel.setText(result);
        });

        VBox vbox = new VBox(10, symbolLabel, symbolField, getStockButton, resultLabel);
        Scene scene = new Scene(vbox, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static String getData(String symbol) {
        String finalUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + symbol + "&interval=5min&apikey=" + api_key;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(finalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String content;
            while ((content = in.readLine()) != null) {
                result.append(content);
            }
            in.close();
            connection.disconnect();
        } catch (Exception e) {
            System.out.println("Error happens");
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String parseData(String symbol, String response) {
        JSONObject jsonn = new JSONObject(response);
        JSONObject timeseries = jsonn.getJSONObject("Time Series (5min)");
        String latestTime = timeseries.keys().next();
        JSONObject latestdata = timeseries.getJSONObject(latestTime);
        double openPrice = latestdata.getDouble("1. open");
        double highPrice = latestdata.getDouble("2. high");
        double lowPrice = latestdata.getDouble("3. low");
        double closePrice = latestdata.getDouble("4. close");
        double volume = latestdata.getDouble("5. volume");
        storeData(symbol, openPrice, highPrice, lowPrice, closePrice, volume, latestTime);

        // Construct result string for display in UI
        return String.format("Time: %s\nOpen price: %.2f\nHigh price: %.2f\nLow price: %.2f\nClose price: %.2f\nVolume: %.2f",
                latestTime, openPrice, highPrice, lowPrice, closePrice, volume);
    }

    public static void storeData(String symbol, double openPrice, double highPrice, double lowPrice, double closePrice, double volume, String latestTime) {
        Document document = new Document("symbol", symbol)
                .append("time", latestTime)
                .append("openPrice", openPrice)
                .append("highPrice", highPrice)
                .append("lowPrice", lowPrice)
                .append("closePrice", closePrice)
                .append("volume", volume);
        collection.insertOne(document);
        System.out.println("Data stored successfully");
    }
}
