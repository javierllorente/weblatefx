/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
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

import com.javierllorente.jgettext.TranslationParserFactory;
import com.javierllorente.wlfx.dialog.LoginDialog;
import com.javierllorente.wlfx.dialog.SettingsDialog;
import com.javierllorente.wlfx.alert.ExceptionAlert;
import com.javierllorente.wlfx.alert.UncommittedChangesAlert;
import com.javierllorente.wlfx.alert.SubmitAlert;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.javierllorente.jgettext.JsonParser;
import com.javierllorente.jgettext.ParserFactory;
import com.javierllorente.jgettext.TranslationFile;
import com.javierllorente.jgettext.TranslationParser;
import com.javierllorente.jgettext.exception.UnsupportedFileFormatException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/**
 * FXML Controller class
 *
 * @author javier
 */
public class BrowserController implements Initializable {

    private static final Logger logger = Logger.getLogger(BrowserController.class.getName());
    private Preferences preferences;
    private String selectedProject;
    private String selectedComponent;
    private String lastComponent;
    private String selectedLanguage;
    private String translation;
    private TranslationFile translationFile;
    private IntegerProperty entryIndexProperty;
    private boolean dataLoaded;
    private History history;
    private HistoryAdapter historyAdapter;
    private BooleanBinding menuItemNotSelected;

    @FXML
    private BorderPane borderPane;

    @FXML
    private TranslationTabController translationTabController;
    
    @FXML
    private QuickPanelController quickPanelController;

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
    private ComboBox<String> languagesComboBox;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preferences = Preferences.userRoot();
        entryIndexProperty = new SimpleIntegerProperty(-1);
        dataLoaded = false;
        history = new History();
        history.entryIndexProperty().bind(entryIndexProperty);
        historyAdapter = new HistoryAdapter();
        historyAdapter.setTranslationTabController(translationTabController);
        history.setAdapter(historyAdapter);
        translationTabController.setHistory(history);

        setupProjectListView();
        setupComponentsListView();
        setupLanguagesComboBox();
        setupBindings();

        entryIndexProperty.addListener((ObservableValue<? extends Number> ov,
                Number oldIndex, Number newIndex) -> {
            logger.log(Level.INFO, "Index value changed. Old: {0}, new: {1}",
                    new Object[]{oldIndex, newIndex});

            if (newIndex.equals(-1)) {
                return;
            }

            if (!oldIndex.equals(-1)
                    && translationTabController.translationChangedProperty().get()) {

                history.check(oldIndex.intValue());

                translationFile.updateEntry(oldIndex.intValue(),
                        translationTabController.getTranslations());

                quickPanelController.updateTableEntry(oldIndex.intValue(),
                        translationFile.getEntries().get(oldIndex.intValue()));

                if (translationFile.getEntries().get(oldIndex.intValue()).isFuzzy()) {
                    translationFile.getEntries().get(oldIndex.intValue()).removeFuzzyFlag();
                }
            }

            if (translationFile.getEntries().get(newIndex.intValue()).getMsgId() != null) {

                quickPanelController.selectAndScroll();
                quickPanelController.clearMetadata();
                if (translationFile.getEntries().get(newIndex.intValue()).getComments() != null) {
                    quickPanelController.addMetadata(translationFile.getEntries()
                            .get(newIndex.intValue()).getComments());
                }

                if (translationFile.getEntries().get(newIndex.intValue()).isPlural()) {
                    translationTabController.clearTranslationAreas();
                } else {
                    translationTabController.clearAllButFirst();
                }

                translationTabController.loadTranslations(translationFile.getEntries()
                        .get(newIndex.intValue()));
            }
        });

        quickPanelController.entryIndexProperty().addListener((ov, t, t1) -> {
            entryIndexProperty.set(t1.intValue());
        });

        autoLogin();
    }

    public void setOnCloseWindow(Scene scene) {
        scene.getWindow().setOnCloseRequest((e) -> {
            closeWindowEvent(e);
        });
        scene.setOnKeyPressed((e) -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                closeWindowEvent(e);
            }
        });
    }

    private void closeWindowEvent(Event event) {
        if (entryIndexProperty.get() != -1) {

            if (history.hasTranslationChanged()) {
                Alert alert = new UncommittedChangesAlert(borderPane.getScene().getWindow(), "close");
                alert.showAndWait().ifPresent((response) -> {
                    if (response == ButtonType.NO) {
                        event.consume();
                    }
                });
            }
        }
    }

    private void clearWorkArea() {
        translationTabController.clearTranslationAreas();
        history.clear();
        quickPanelController.clear();
        dataLoaded = false;
        entryIndexProperty.set(-1);
    }

    public void autoLogin() {
        if (preferences.getBoolean(App.AUTOLOGIN, false)) {
            handleSignIn();
        }
    }

    @FXML
    private void previousItem() {
        entryIndexProperty.set(quickPanelController.decrementTableIndex());
    }

    @FXML
    private void nextItem() {
        entryIndexProperty.set(quickPanelController.incrementTableIndex());
    }

    @FXML
    private void getProjects() {
        try {
            Platform.runLater(() -> {
                progressIndicator.setVisible(true);
            });
            ObservableList<String> items = FXCollections.observableArrayList(
                    App.getWeblate().getProjects());
            Collections.sort(items);
            Platform.runLater(() -> {
                projectsListView.setItems(items);
                progressIndicator.setVisible(false);
            });
        } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
            Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                showExceptionAlert(ex);
            });
        }
    }

    private void setupProjectListView() {
        ObservableList<String> components = FXCollections.observableArrayList();
        componentsListView.setItems(components);

        projectsListView.setCellFactory(new SelectionCellFactory(
                history, borderPane, "project"));

        projectsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    selectedProject = t1;
                    componentsListView.getItems().clear();
                    languagesComboBox.getItems().clear();

                    if (t1 == null) {
                        lastComponent = null;
                    } else {
                        logger.log(Level.INFO, "Selected project: {0}", selectedProject);

                        if (dataLoaded) {
                            clearWorkArea();
                        }

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
                            } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
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
        languagesComboBox.setItems(languages);

        componentsListView.setCellFactory(new SelectionCellFactory(
                history, borderPane, "component"));

        componentsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    languagesComboBox.getItems().clear();
                    
                    if (t1 != null) {
                        selectedComponent = t1;
                        lastComponent = selectedComponent;
                        logger.log(Level.INFO, "Selected component: {0}", selectedComponent);

                        if (dataLoaded) {
                            clearWorkArea();
                        }

                        new Thread(() -> {
                            try {
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(true);
                                });
                                List<String> items = App.getWeblate().getTranslations(
                                        selectedProject, selectedComponent.toLowerCase());
                                Collections.sort(items);
                                Platform.runLater(() -> {
                                    languages.setAll(items);

                                    String translatorLanguage = preferences
                                            .get(App.TRANSLATOR_LANGUAGE, "");

                                    if (!translatorLanguage.isEmpty()) {
                                        if (languages.contains(translatorLanguage)) {
                                            languagesComboBox.getSelectionModel()
                                                    .select(translatorLanguage);
                                        } else {
                                            // Clear selection
                                            languagesComboBox.setValue(null);
                                        }
                                    }

                                    progressIndicator.setVisible(false);
                                });

                            } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
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

    private void setupLanguagesComboBox() {
        languagesComboBox.setCellFactory(new SelectionCellFactory(
                history, borderPane, "language"));

        languagesComboBox.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    if (t1 != null) {
                        selectedLanguage = t1;
                        logger.log(Level.INFO, "Selected language: {0}", selectedLanguage);

                        if (dataLoaded) {
                            clearWorkArea();
                        }

                        if (!selectedLanguage.equals(preferences.get(App.TRANSLATOR_LANGUAGE, ""))) {
                            preferences.put(App.TRANSLATOR_LANGUAGE, selectedLanguage);
                        }

                        new Thread(() -> {
                            try {
                                Platform.runLater(() -> {
                                    progressIndicator.setVisible(true);
                                });

                                String fileFormat = App.getWeblate().getFileFormat(selectedProject,
                                        selectedComponent, selectedLanguage);

                                TranslationParserFactory parserFactory = new ParserFactory();
                                TranslationParser translationParser = parserFactory.getParser(fileFormat);

                                translation = App.getWeblate().getFile(selectedProject,
                                        selectedComponent, selectedLanguage);
                                translationFile = translationParser.parse(translation);
                                historyAdapter.setOldTranslations(translationFile.getEntries());

                                if (fileFormat.equalsIgnoreCase("json")) {
                                    String sourceLanguage = App.getWeblate().getFile(selectedProject,
                                            selectedComponent, "en");
                                    JsonParser jsonTranslationParser
                                            = (JsonParser) translationParser;
                                    jsonTranslationParser.setSourceLanguage(true);
                                    translationFile = translationParser.parse(sourceLanguage);
                                    jsonTranslationParser.setSourceLanguage(false);
                                }

                                quickPanelController.addTableData(translationFile.getEntries());
                                dataLoaded = !quickPanelController.isTableDataEmpty();

                                Platform.runLater(() -> {
                                    if (entryIndexProperty.equals(0)) {
                                        entryIndexProperty.set(-1);
                                    }
                                    entryIndexProperty.set(0);
                                    progressIndicator.setVisible(false);
                                });
                            } catch (IOException | UnsupportedFileFormatException 
                                    | NullPointerException ex) {
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
        menuItemNotSelected = projectsListView.getSelectionModel().selectedItemProperty().isNull()
                .or(componentsListView.getSelectionModel().selectedItemProperty().isNull())
                .or(languagesComboBox.getSelectionModel().selectedItemProperty().isNull());

        submitButton.disableProperty().bind(menuItemNotSelected
                .or(quickPanelController.tableSelectedIndexProperty().isEqualTo(-1)));

        previousButton.disableProperty().bind(menuItemNotSelected
                .or(quickPanelController.filterTextProperty().isEmpty()
                        .and(quickPanelController.tableSelectedIndexProperty().lessThanOrEqualTo(0)))
                .or(quickPanelController.filterTextProperty().isNotEmpty()
                        .and(quickPanelController.tableSelectedIndexProperty().lessThanOrEqualTo(0))));

        nextButton.disableProperty().bind(menuItemNotSelected
                .or(quickPanelController.tableSelectedIndexProperty().isEqualTo(-1)
                        .and(quickPanelController.filterTextProperty().isEmpty()))
                .or(Bindings.createBooleanBinding(() -> {
                    return quickPanelController.isFilterIndexAtEnd();
                }, quickPanelController.tableSelectedIndexProperty())));
    }

    @FXML
    private void handleSettings() {
        showSettingsDialog(false, false);
    }

    @FXML
    private void handleAbout() {
        Alert aboutAlert = new Alert(AlertType.INFORMATION);
        aboutAlert.initOwner(borderPane.getScene().getWindow());
        aboutAlert.getDialogPane().setPrefSize(480, 320);
        aboutAlert.setTitle("About " + App.NAME);
        aboutAlert.setGraphic(new ImageView(App.class.getResource("/wlfx.png").toString()));
        aboutAlert.setHeaderText(App.NAME + " " + App.VERSION + "\n"
                + "A JavaFX-based Weblate client");
        aboutAlert.setContentText("Java: "
                + System.getProperty("java.runtime.name") + " "
                + System.getProperty("java.runtime.version") + "\n"
                + "JavaFX: " + System.getProperty("javafx.runtime.version") + "\n"
                + "Libraries: jwl, jgettext, java-diff-utils, "
                + "ikonli-javafx, ikonli-icomoon-pack" + "\n\n"
                + "Copyright © 2020-2022 Javier Llorente" + "\n"
                + "This program is under the GPLv3");
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
            App.getWeblate().logout();
            projectsListView.getItems().clear();
            clearWorkArea();
            signInButton.setText("Sign in");
        } else {
            String authToken = preferences.get(App.AUTH_TOKEN, "");
            String apiUri = preferences.get(App.API_URI, "");

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

                showSettingsDialog(true, true);

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
    }

    private void authenticate(String authToken) {
        new Thread(() -> {
            try {
                App.getWeblate().setAuthToken(authToken);
                App.getWeblate().authenticate();
                Platform.runLater(() -> {
                    progressIndicator.setVisible(true);
                });
            } catch (ClientErrorException | ServerErrorException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);                
                
                switch (ex.getResponse().getStatusInfo().toEnum()) {
                    case UNAUTHORIZED:
                        preferences.put(App.AUTH_TOKEN, authToken);
                        Platform.runLater(() -> {
                            LoginDialog dialog = new LoginDialog(borderPane
                                    .getScene().getWindow(), preferences);
                            Optional<String> result = dialog.showAndWait();
                            result.ifPresent(authTokenEntered -> {
                                authenticate(authTokenEntered);
                            });
                        });       
                        break;
                    case NOT_FOUND:
                        Platform.runLater(() -> {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.initOwner(borderPane.getScene().getWindow());
                            alert.setResizable(true);
                            alert.setTitle("Error");
                            alert.setHeaderText("API URI not found");
                            alert.setContentText(preferences.get(App.API_URI, ""));
                            alert.showAndWait();
                            
                            showSettingsDialog(true, true);
                        });
                        break;                        
                    default:
                        Platform.runLater(() -> {
                            showExceptionAlert(ex);
                        });
                }

            } catch (ProcessingException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> {
                    showExceptionAlert(ex);
                });
            }

            if (App.getWeblate().isAuthenticated()) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    signInButton.setText("Sign out");
                });
                preferences.put(App.AUTH_TOKEN, authToken);
                getProjects();
            }

        }).start();
    }
    
    private void showSettingsDialog(boolean focusApiUriField, boolean callHandleSignIn) {
        SettingsDialog settingsDialog = new SettingsDialog(borderPane
                .getScene().getWindow(), preferences);
        if (focusApiUriField) {
            settingsDialog.focusApiUriField();
        }
        Optional<Map<String, String>> result = settingsDialog.showAndWait();
        result.ifPresent(data -> {
            updatePreferences(data);
            if (callHandleSignIn) {
                handleSignIn();
            }
        });        
    }
    
    private void updatePreferences(Map<String, String> data) {
        preferences.put(App.TRANSLATOR_NAME, data.get(App.TRANSLATOR_NAME));
        preferences.put(App.TRANSLATOR_EMAIL, data.get(App.TRANSLATOR_EMAIL));
        preferences.put(App.API_URI, data.get(App.API_URI));
        preferences.put(App.AUTH_TOKEN, data.get(App.AUTH_TOKEN));
        preferences.put(App.AUTOLOGIN, data.get(App.AUTOLOGIN));
    }

    private void showExceptionAlert(Throwable throwable) {
        ExceptionAlert exceptionAlert = new ExceptionAlert(borderPane.getScene().getWindow());
        exceptionAlert.setHeader(throwable.getMessage());        
        exceptionAlert.setThrowable(throwable);
        exceptionAlert.showAndWait();
    }
    
    @FXML
    private void submit() {
        translationFile.setTranslator(preferences.get(App.TRANSLATOR_NAME, ""),
                preferences.get(App.TRANSLATOR_EMAIL, ""));
        translationFile.setRevisionDate();
        translationFile.setGenerator(App.NAME + " " + App.VERSION);

        if (translationTabController.translationChangedProperty().get()) {
            translationFile.updateEntry(entryIndexProperty.get(), translationTabController.getTranslations());

            if (translationFile.getEntries().get(entryIndexProperty.get()).isFuzzy()) {
                translationFile.getEntries().get(entryIndexProperty.get()).removeFuzzyFlag();
            }
        }

        String translationFileStr = translationFile.toString();
        logger.log(Level.INFO, "lines old: {0} lines new: {1}", new Object[]{
            translation.split("\n", -1).length, translationFileStr.split("\n", -1).length});

        List<String> oldTranslation = Arrays.asList(translation.split("\n"));
        List<String> newTranslation = Arrays.asList(translationFileStr.split("\n"));

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
        submitAlert.setDiff(unifiedDiff);
        Optional<ButtonType> result = submitAlert.showAndWait();

        if (result.get() == ButtonType.OK) {
            new Thread(() -> {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(true);
                });
                try {
                    Map<String, String> submitResult = App.getWeblate().submit(selectedProject,
                            selectedComponent, selectedLanguage, translationFileStr);

                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);

                        String contextText = "";
                        int acceptedChanges = Integer.parseInt(submitResult.get("accepted"));
                                                
                        for (Map.Entry<String, String> entry : submitResult.entrySet()) {
                            contextText += entry.getKey() + ": " + entry.getValue() + "\n";
                        }

                        contextText = contextText.substring(0, contextText.length() - 1);

                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.initOwner(borderPane.getScene().getWindow());
                        alert.setTitle("Submit results");
                        alert.setHeaderText("Accepted changes: " + acceptedChanges);
                        alert.setContentText(contextText);
                        alert.setResizable(true); // FIXME: Workaround for JavaFX 11
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                        alert.showAndWait();

                        if (acceptedChanges > 0) {
                            translation = translationFileStr;
                            history.clear();
                        }
                    });

                } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
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
