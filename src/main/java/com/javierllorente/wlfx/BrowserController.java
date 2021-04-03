/*
 * Copyright (C) 2020, 2021 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.javierllorente.wlfx;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.javierllorente.jgettext.POFile;
import com.javierllorente.jgettext.POParser;
import com.javierllorente.jgettext.TranslationParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javax.naming.AuthenticationException;

/**
 * FXML Controller class
 *
 * @author javier
 */
public class BrowserController implements Initializable {
    private static final Logger logger = Logger.getLogger(BrowserController.class.getName());
    private TranslationTabController translationTabController;
    private Preferences preferences;
    private String selectedProject;
    private String selectedComponent;
    private String lastComponent;
    private String selectedLanguage;
    private String lastLanguage;
    private String translation;
    private POFile poFile;
    private int entryIndex;
    private IntegerProperty entryIndexProperty;
    
    @FXML
    private BorderPane borderPane;
    
    @FXML
    private VBox workArea;
    
    @FXML
    private Button signInButton;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button previousButton;
    
    @FXML
    private Button nextButton;
    
    @FXML
    private ProgressIndicator progressIndicator;    
    
    @FXML
    private ListView<String> projectsListView;
    
    @FXML
    private ListView<String> componentsListView;
    
    @FXML
    private ListView<String> languagesListView;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preferences = Preferences.userRoot();
        entryIndex = -1;
        entryIndexProperty = new SimpleIntegerProperty(entryIndex);
        translationTabController = new TranslationTabController(workArea);        
        
        setupProjectListView();
        setupComponentsListView();
        setupLanguagesListView();
        setupBindings();
        
        entryIndexProperty.addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            logger.log(Level.INFO, "Index changed: {0}", entryIndexProperty.get());
            if ((entryIndex != -1) && (poFile.getEntries().get(entryIndex).getMsgId() != null)) {
                translationTabController.loadTranslations(poFile.getEntries().get(entryIndex));
            }
        });       
        
        Platform.runLater(() -> {
            autoLogin();
        });
    }    
    
    public void autoLogin() {
        if (preferences.getBoolean(App.AUTOLOGIN, false)) {
            handleSignIn();
        }
    }    
    
    @FXML
    private void previousItem() {        
        if (entryIndex != 0) {                       
            if (translationTabController.translationChangedProperty().get()) {
                poFile.updateEntry(entryIndex, translationTabController.getTranslations());
            }            
            
            if (poFile.getEntries().get(--entryIndex).isPlural()) {
                translationTabController.clearTranslationAreas();
            } else {
                translationTabController.clearAllButFirst();
            }
            
            entryIndexProperty.set(entryIndex);            
        }
    }
    
    @FXML
    private void nextItem() {
        if (entryIndex != poFile.getEntries().size() - 1) {
            if (translationTabController.translationChangedProperty().get()) {
                poFile.updateEntry(entryIndex, translationTabController.getTranslations());
            }

            if (poFile.getEntries().get(++entryIndex).isPlural()) {
                translationTabController.clearTranslationAreas();
            } else {
                translationTabController.clearAllButFirst();
            }
            
            entryIndexProperty.set(entryIndex);            
        }
    }

    @FXML
    private void getProjects() {
        
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(true);
                });
                ObservableList<String> items = FXCollections.observableArrayList(
                        App.getWeblate().getProjects());

                Platform.runLater(() -> {
                    projectsListView.setItems(items);
                    progressIndicator.setVisible(false);
                });
            } catch (URISyntaxException | InterruptedException | IOException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    showExceptionAlert(ex);                    
                });
            }
        }).start();
    }

    private void setupProjectListView() {
        ObservableList<String> components = FXCollections.observableArrayList();
        componentsListView.setItems(components);

        projectsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    selectedProject = t1;
                    if (t1 == null) {
                        componentsListView.getItems().clear();
                        languagesListView.getItems().clear();
                        lastComponent = null;
                        lastLanguage = null;
                    } else {
                        selectedProject = t1.toLowerCase().replace(".", "-");
                        logger.log(Level.INFO, "Selected project: {0}", selectedProject);

                        new Thread(() -> {
                            try {
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(true);
                                });
                                List<String> items = App.getWeblate().getComponents(selectedProject);
                                Platform.runLater(() -> {
                                    components.setAll(items);
                                    if (lastComponent != null && components.contains(lastComponent)) {
                                        componentsListView.getSelectionModel().select(lastComponent);
                                    }
                                    progressIndicator.setVisible(false);
                                });
                            } catch (URISyntaxException | InterruptedException | IOException ex) {
                                Logger.getLogger(BrowserController.class.getName())
                                        .log(Level.SEVERE, null, ex);
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(false);
                                    showExceptionAlert(ex);
                                });
                            }
                        }).start();
                    }
                });
    }
    
    private void setupComponentsListView() {
        ObservableList<String> languages = FXCollections.observableArrayList();
        languagesListView.setItems(languages);
        
        componentsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    if (t1 == null) {
                        languagesListView.getItems().clear();
                    } else {
                        selectedComponent = t1.toLowerCase().replace(".", "_").replace(" ", "-");
                        lastComponent = selectedComponent;
                        logger.log(Level.INFO, "Selected component: {0}", selectedComponent);

                        new Thread(() -> {
                            try {
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(true);
                                });
                                List<String> items = App.getWeblate().getTranslations(
                                        selectedProject, selectedComponent.toLowerCase(), 1);
                                Collections.sort(items);
                                Platform.runLater(() -> {
                                    languages.setAll(items);                                    
                                    if (lastLanguage != null && languages.contains(lastLanguage)) {
                                        languagesListView.getSelectionModel().select(lastLanguage);
                                    }
                                    progressIndicator.setVisible(false);
                                });

                            } catch (URISyntaxException | InterruptedException | IOException ex) {
                                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(false);
                                    showExceptionAlert(ex);
                                });
                            }
                        }).start();

                    }
                });
    }
    
    private void setupLanguagesListView() {        
        languagesListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    if (t1 == null) {
                    } else {
                        selectedLanguage = t1.toLowerCase().replace(" ", "-");
                        lastLanguage = t1;
                        logger.log(Level.INFO, "Selected language: {0}", selectedLanguage);

                        new Thread(() -> {
                            try {
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(true);
                                });
                                translation = App.getWeblate().getFile(selectedProject, selectedComponent, t1);
                                TranslationParser poParser = new POParser();
                                poFile = (POFile) poParser.parse(translation);

                                Platform.runLater(() -> {
                                    if (entryIndex == 0) {
                                        entryIndex = -1;
                                        entryIndexProperty.set(entryIndex);
                                    }
                                    entryIndex = 0;
                                    entryIndexProperty.set(entryIndex);
                                    progressIndicator.setVisible(false);
                                });
                            } catch (URISyntaxException | InterruptedException | IOException ex) {
                                Logger.getLogger(BrowserController.class.getName())
                                        .log(Level.SEVERE, null, ex);
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(false);
                                    showExceptionAlert(ex);
                                });
                            }
                        }).start();
                    }
                });
    }
    
    private void setupBindings() {
        submitButton.disableProperty().bind(Bindings.or(
                projectsListView.getSelectionModel().selectedItemProperty().isNull(),
                componentsListView.getSelectionModel().selectedItemProperty().isNull()).or(
                languagesListView.getSelectionModel().selectedItemProperty().isNull()
        ));

        previousButton.disableProperty().bind(entryIndexProperty.lessThanOrEqualTo(0));

        nextButton.disableProperty().bind(Bindings.or(
                componentsListView.getSelectionModel().selectedItemProperty().isNull(),
                languagesListView.getSelectionModel().selectedItemProperty().isNull()
        ).or(Bindings.createBooleanBinding(() -> {
            return poFile != null ? entryIndex + 1 > poFile.getEntries().size() - 1 : false;
        }, entryIndexProperty)
        ));
        
    }

    @FXML
    private void handleSettings() {        
        SettingsDialog dialog = new SettingsDialog(borderPane.getScene().getWindow(), 
                preferences);
        
        Optional<Map<String, String>> result = dialog.showAndWait();        
        result.ifPresent(data -> {
            preferences.put(App.TRANSLATOR_NAME, data.get(App.TRANSLATOR_NAME));
            preferences.put(App.TRANSLATOR_EMAIL, data.get(App.TRANSLATOR_EMAIL));
            preferences.put(App.API_URI, data.get(App.API_URI));
            preferences.put(App.AUTH_TOKEN, data.get(App.AUTH_TOKEN));
            preferences.put(App.AUTOLOGIN, data.get(App.AUTOLOGIN));
        });
        
    }
    
    @FXML
    private void handleAbout() {
        Alert aboutAlert = new Alert(AlertType.INFORMATION);
        aboutAlert.initOwner(borderPane.getScene().getWindow());
        aboutAlert.setTitle("About " + App.NAME);
        aboutAlert.setGraphic(new ImageView(App.class.getResource("/wlfx.png").toString()));
        aboutAlert.setHeaderText(App.NAME + " " + App.VERSION + "\n" 
                + "A JavaFX-based Weblate client");
        aboutAlert.setContentText("© 2020, 2021 Javier Llorente" + "\n" +
                "This program is under the GPLv3");
        aboutAlert.setResizable(true);
        aboutAlert.showAndWait();
    }
    
    @FXML
    private void handleQuit() {
       Platform.exit();
    }
    
    @FXML
    private void handleSignIn() {
        
        if (App.getWeblate().isAuthenticated()) {
            signInButton.setText("Sign in");
            
            LoginDialog dialog = new LoginDialog(borderPane.getScene().getWindow(), 
                    preferences);
            Optional<String> result = dialog.showAndWait();
            result.ifPresentOrElse((authTokenEntered) -> {
                projectsListView.getItems().clear();
                authenticate(authTokenEntered);
            }, () -> {
                signInButton.setText("Sign out");
            });
            
            return;
        }

        String authToken = preferences.get(App.AUTH_TOKEN, "");
        String apiUri = preferences.get(App.API_URI, "");
        if (apiUri.endsWith("/")) {
            apiUri = apiUri.substring(0, apiUri.lastIndexOf("/"));
        }
        
        try {
            App.getWeblate().setApiUrl(new URI(apiUri));
        } catch (URISyntaxException ex) {
            Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (apiUri.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(borderPane.getScene().getWindow());
            alert.setResizable(true);
            alert.setTitle("Error");
            alert.setHeaderText("Empty API URI");
            alert.setContentText("API URI is empty. Please add an API URI in settings");
            alert.showAndWait();    
        } else if (authToken.isEmpty()) {
            LoginDialog dialog = new LoginDialog(borderPane.getScene().getWindow(), 
                    preferences);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(authTokenEntered -> {
                authenticate(authTokenEntered);
            });
        } else {
            authenticate(authToken);
        }
    }

    private void authenticate(String authToken) {
        try {
            App.getWeblate().setAuthToken(authToken);
            App.getWeblate().authenticate();
            progressIndicator.setVisible(true);
        } catch (IOException | InterruptedException | AuthenticationException ex) {            
            
            if ((ex.getMessage() != null) && (ex.getMessage().equals("401"))) {
                Logger.getLogger(BrowserController.class.getName())
                        .log(Level.WARNING, "Error 401. Wrong auth token.");
                preferences.put(App.AUTH_TOKEN, authToken);
                Platform.runLater(() -> {
                    LoginDialog dialog = new LoginDialog(borderPane.getScene().getWindow(),
                            preferences);
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(authTokenEntered -> {
                        authenticate(authTokenEntered);
                    });
                });
            } else {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);

                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.initOwner(borderPane.getScene().getWindow());
                    alert.setResizable(true);
                    alert.setTitle("Error");
                    alert.setHeaderText(ex.getClass().toString());
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }        

        if (App.getWeblate().isAuthenticated()) {
            progressIndicator.setVisible(false);
            preferences.put(App.AUTH_TOKEN, authToken);
            signInButton.setText("Sign out");
            getProjects();
        }

    }
    
    private void showExceptionAlert(Throwable throwable) {
        ExceptionAlert exceptionAlert = new ExceptionAlert(borderPane.getScene().getWindow());
        exceptionAlert.setThrowable(throwable);
        exceptionAlert.showAndWait();
    }
    
    @FXML
    private void submit() {
        poFile.setTranslator(preferences.get(App.TRANSLATOR_NAME, ""),
                preferences.get(App.TRANSLATOR_EMAIL, ""));
        poFile.setRevisionDate();
        poFile.setGenerator(App.NAME + " " + App.VERSION);

        if (translationTabController.translationChangedProperty().get()) {
            poFile.updateEntry(entryIndex, translationTabController.getTranslations());
        }

        String poFileStr = poFile.toString();
        logger.log(Level.INFO, "lines old: {0} lines new: {1}", new Object[]{
            translation.split("\n", -1).length, poFileStr.split("\n", -1).length});

        List<String> oldTranslation = Arrays.asList(translation.split("\n"));
        List<String> newTranslation = Arrays.asList(poFileStr.split("\n"));
        
//        System.out.println("New translation:");
//        newTranslation.forEach(System.out::println);
        
        logger.log(Level.INFO, "oldTranslation size: {0} newTranslation size: {1}", 
                new Object[]{oldTranslation.size(), newTranslation.size()});

        Patch<String> patch = DiffUtils.diff(oldTranslation, newTranslation);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "Old translation", "New translation", oldTranslation, patch, 0);
//            unifiedDiff.forEach(System.out::println);

        SubmitAlert submitAlert = new SubmitAlert(AlertType.CONFIRMATION,
                borderPane.getScene().getWindow());
        submitAlert.setDiff(String.join("\n", unifiedDiff));
        Optional<ButtonType> result = submitAlert.showAndWait();
        
        if (result.get() == ButtonType.OK) {
            new Thread(() -> {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(true);
                });
                try {
                    String submitResult = App.getWeblate().submit(selectedProject,
                            selectedComponent, selectedLanguage, poFileStr);
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                    });
                    logger.log(Level.INFO, "Submit result: {0}", submitResult);
                } catch (IOException | URISyntaxException | InterruptedException ex) {
                    Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        showExceptionAlert(ex);
                    });
                }
            }).start();
        }
    }
}
