package io.github.mfirash;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.MenuItem;
import javafx.scene.Node;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PrimaryController {

    @FXML
    private WebView webView;

    @FXML
    private TextField url;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab initialTab;

    @FXML
    private ToolBar bookmarks;

    @FXML
    private MenuItem savebook;

    @FXML
    private MenuItem book;

    @FXML
    private MenuItem gethtml;

    @FXML
    private MenuItem savehtml;

    private final String BOOKMARKS_FILE_NAME = "bookmarks.json";

    @FXML
    public void initialize() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        WebEngine webEngine = webView.getEngine();        
        webEngine.load("https://google.com/");
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            url.setText(newValue);
        });

        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                initialTab.setText(newTitle);
            } else {
                initialTab.setText("Welcome");
            }
        });
        loadBookmarks();
    }

    @FXML
    private void behind(ActionEvent event) {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            if (webEngine.getHistory().getCurrentIndex() > 0) {
                webEngine.getHistory().go(-1);
            }
        }
    }

    @FXML
    private void foward(ActionEvent event) {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            if (webEngine.getHistory().getCurrentIndex() < webEngine.getHistory().getEntries().size() - 1) {
                webEngine.getHistory().go(1);
            }
        }
    }

    @FXML
    private void reload(ActionEvent event) {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            currentWebView.getEngine().reload();
        }
    }

    @FXML
    private void gtg() {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            webEngine.load("https://google.com/");
            System.out.println("Going to Google");
        }
    }

    @FXML
    private void enterurl(ActionEvent event) {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            String urlText = url.getText().trim();
            if (!urlText.isEmpty()) {
                if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
                    urlText = "https://" + urlText;
                }
                webEngine.load(urlText);
            }
        }
    }

    @FXML
    private void about(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About FXWeb");
        alert.setHeaderText("FXWeb Browser");
        alert.setContentText("This is an attempted QtWeb clone web browser built using JavaFX.");
        alert.showAndWait();
    }

    @FXML
    private void close(ActionEvent event) {

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            tabPane.getTabs().remove(selectedTab);
            if (tabPane.getTabs().isEmpty()) {
                Platform.exit();
                System.exit(0);
            }
        }
    }

    @FXML
    private void newtab(ActionEvent event) {
        WebView newWebView = new WebView();
        AnchorPane anchorPane = new AnchorPane(newWebView);
        AnchorPane.setTopAnchor(newWebView, 0.0);
        AnchorPane.setBottomAnchor(newWebView, 0.0);
        AnchorPane.setLeftAnchor(newWebView, 0.0);
        AnchorPane.setRightAnchor(newWebView, 0.0);

        Button closeButton = new Button("X");
        closeButton.setOnAction(e -> {
            Tab tabToClose = null;
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getGraphic() == e.getSource()) {
                    tabToClose = tab;
                    break;
                }
            }
            if (tabToClose != null) {
                tabPane.getTabs().remove(tabToClose);
            }
        });

        Tab newTab = new Tab("New Tab", anchorPane);
        newTab.setGraphic(closeButton);

        WebEngine webEngine = newWebView.getEngine();
        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                newTab.setText(newTitle);
            } else {
                newTab.setText("New Tab");
            }
        });
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newTab.isSelected()) {
                url.setText(newValue);
            }
        });

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    @FXML
    private void markpage(ActionEvent event) {
        addBookmark();
    }

    @FXML
    private void book(ActionEvent event) {
        addBookmark();
    }

    private void addBookmark() {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            String pageUrl = webEngine.getLocation();
            String pageTitle = webEngine.getTitle();

            if (pageUrl == null || pageUrl.isEmpty() || "about:blank".equals(pageUrl)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Bookmark Error");
                alert.setHeaderText("Cannot Add Bookmark");
                alert.setContentText("This website has no URL or the URL is about:blank.");
                alert.showAndWait();
                return;
            }

            Button bookmarkButton = new Button(pageTitle != null ? pageTitle : pageUrl);
            bookmarkButton.setUserData(pageUrl); // Store the actual URL here
            bookmarkButton.setOnAction(e -> {
                webEngine.load(pageUrl);
            });
            bookmarks.getItems().add(bookmarkButton);
        }
    }

    @FXML
    private void savebook(ActionEvent event) {
        try (FileWriter file = new FileWriter(BOOKMARKS_FILE_NAME)) {
            JSONArray bookmarkList = new JSONArray();
            for (Object item : bookmarks.getItems()) {
                if (item instanceof Button) {
                    Button btn = (Button) item;
                    JSONObject bookmarkObject = new JSONObject();
                    bookmarkObject.put("title", btn.getText());
                    bookmarkObject.put("url", btn.getUserData());
                    bookmarkList.add(bookmarkObject);
                }
            }
            file.write(bookmarkList.toJSONString());
            file.flush();
            System.out.println("Bookmarks saved to " + BOOKMARKS_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving bookmarks: " + e.getMessage());
        }
    }

    private void loadBookmarks() {
        File bookmarksFile = new File(BOOKMARKS_FILE_NAME);
        if (bookmarksFile.exists()) {
            JSONParser parser = new JSONParser();
            try (FileReader reader = new FileReader(bookmarksFile)) {
                Object obj = parser.parse(reader);
                JSONArray bookmarkList = (JSONArray) obj;
                bookmarks.getItems().clear();

                for (Object item : bookmarkList) {
                    JSONObject bookmarkObject = (JSONObject) item;
                    String title = (String) bookmarkObject.get("title");
                    String url = (String) bookmarkObject.get("url");

                    Button bookmarkButton = new Button(title);
                    bookmarkButton.setOnAction(e -> {
                        WebView currentWebView = getCurrentWebView();
                        if (currentWebView != null) {
                            currentWebView.getEngine().load(url);
                        }
                    });
                    bookmarks.getItems().add(bookmarkButton);
                }
                System.out.println("Bookmarks loaded from " + BOOKMARKS_FILE_NAME);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                System.err.println("Error loading bookmarks: " + e.getMessage());
            }
        }
    }

    @FXML
    private void gethtml(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open HTML File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("HTML Files", "*.html", "*.htm"),
                new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            WebView currentWebView = getCurrentWebView();
            if (currentWebView != null) {
                currentWebView.getEngine().load(selectedFile.toURI().toString());
            }
        }
    }

    @FXML
    private void savehtml(ActionEvent event) {
        WebView currentWebView = getCurrentWebView();
        if (currentWebView != null) {
            WebEngine webEngine = currentWebView.getEngine();
            String htmlContent = (String) webEngine.executeScript("document.documentElement.outerHTML");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save HTML Content");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("HTML Files", "*.html"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(htmlContent);
                    System.out.println("HTML content saved to: " + file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private WebView getCurrentWebView() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            AnchorPane anchorPane = (AnchorPane) selectedTab.getContent();
            return (WebView) anchorPane.getChildren().get(0);
        }
        return null;
    }
}
