/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javierllorente.weblatefx;

import com.javierllorente.jgettext.TranslationParserFactory;
import com.javierllorente.weblatefx.dialog.LoginDialog;
import com.javierllorente.weblatefx.dialog.SettingsDialog;
import com.javierllorente.weblatefx.alert.ExceptionAlert;
import com.javierllorente.weblatefx.alert.UncommittedChangesAlert;
import com.javierllorente.weblatefx.alert.SubmitAlert;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.javierllorente.jgettext.JsonParser;
import com.javierllorente.jgettext.ParserFactory;
import com.javierllorente.jgettext.TranslationFile;
import com.javierllorente.jgettext.TranslationParser;
import com.javierllorente.jgettext.exception.UnsupportedFileFormatException;
import com.javierllorente.weblatefx.dialog.GoToDialog;
import com.javierllorente.weblatefx.alert.ShortcutsAlert;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * FXML Controller class
 *
 * @author javier
 */
public class BrowserController implements Initializable {

    private static final Logger logger = Logger.getLogger(BrowserController.class.getName());
    private Preferences preferences;
    private String translation;
    private String fileFormat;
    private TranslationFile translationFile;
    private IntegerProperty entryIndexProperty;
    private History history;
    private HistoryAdapter historyAdapter;

    @FXML
    private BorderPane borderPane;
    
    @FXML
    private SelectionPanelController selectionPanelController;

    @FXML
    private TranslationTabController translationTabController;
    
    @FXML
    private QuickPanelController quickPanelController;

    @FXML
    private Button loginButton;

    @FXML
    private Button submitButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private ProgressIndicator progressIndicator;
    
    @FXML
    private MenuItem importMenuItem;
    
    @FXML
    private MenuItem exportMenuItem;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preferences = Preferences.userNodeForPackage(getClass());
        entryIndexProperty = new SimpleIntegerProperty(-1);
        history = new History();
        history.entryIndexProperty().bind(entryIndexProperty);
        historyAdapter = new HistoryAdapter();
        historyAdapter.setTranslationTabController(translationTabController);
        history.setAdapter(historyAdapter);
        translationTabController.setHistory(history);

        setupSelectionPanel();
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
        
        registerEventFilters();
        
        autoLogin();
    }

    private void registerEventFilters() {
        loginButton.addEventFilter(MouseEvent.MOUSE_RELEASED, (e) -> {
            if (entryIndexProperty.get() != -1 && history.hasTranslationChanged()) {
                showUncommittedChangesAlert(e, UncommittedChangesAlert.ActionType.LOGOUT);
            }
        });
    }

    private void showUncommittedChangesAlert(Event event, 
            UncommittedChangesAlert.ActionType actionType) {
        Alert alert = new UncommittedChangesAlert(borderPane.getScene().getWindow(), actionType);
        alert.showAndWait().ifPresent((response) -> {
            if (response == ButtonType.NO) {
                event.consume();
            }
        });
    }

    private void setOnCloseWindow(Scene scene) {
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
        if (entryIndexProperty.get() != -1
                && history.hasTranslationChanged()) {
            showUncommittedChangesAlert(event, UncommittedChangesAlert.ActionType.EXIT);
        }
    }
    
    public void setupScene(Scene scene) {
        setOnCloseWindow(scene);
        setupAccelerators(scene.getAccelerators());
        translationTabController.setupAccelerators(scene.getAccelerators());
        quickPanelController.setupAccelerators(scene.getAccelerators());
        selectionPanelController.setWindow(scene.getWindow());
    }
    
    private void setupAccelerators(ObservableMap<KeyCombination, Runnable> accelerators) {
        KeyCombination projectsShortcut = new KeyCodeCombination(KeyCode.DIGIT1,
                KeyCombination.CONTROL_DOWN);
        KeyCombination componentsShortcut = new KeyCodeCombination(KeyCode.DIGIT2,
                KeyCombination.CONTROL_DOWN);
        KeyCombination languagesShortcut = new KeyCodeCombination(KeyCode.DIGIT3,
                KeyCombination.CONTROL_DOWN);
        KeyCombination submitShortcut = new KeyCodeCombination(KeyCode.S,
                KeyCombination.CONTROL_DOWN);        
        KeyCombination previousShortcut = new KeyCodeCombination(KeyCode.COMMA,
                KeyCombination.CONTROL_DOWN);        
        KeyCombination nextShortcut = new KeyCodeCombination(KeyCode.PERIOD,
                KeyCombination.CONTROL_DOWN);
        accelerators.putAll(Map.of(
                projectsShortcut, () -> {
                    if (!selectionPanelController.getProjects().isEmpty()) {
                        showGoToDialog(GoToDialog.PROJECTS);
                    }
                },
                componentsShortcut, () -> {
                    if (!selectionPanelController.getComponents().isEmpty()) {
                        showGoToDialog(GoToDialog.COMPONENTS);
                    }
                },
                languagesShortcut, () -> {
                    if (!selectionPanelController.getLanguages().isEmpty()) {
                        showGoToDialog(GoToDialog.LANGUAGES);
                    }
                },
                submitShortcut, () -> {
                    if (!submitButton.isDisabled()) {
                        handleSubmit();
                    }
                },
                previousShortcut, () -> {
                    if (!previousButton.isDisabled()) {
                        previousItem();
                    }
                },
                nextShortcut, () -> {
                    if (!nextButton.isDisabled()) {
                        nextItem();
                    }
                }
        ));
    }

    private void showGoToDialog(String title) {
        ObservableList<String> data;        
        switch (title) {
            case GoToDialog.PROJECTS:
                data = selectionPanelController.getProjects();
                break;
            case GoToDialog.COMPONENTS:
                data = selectionPanelController.getComponents();
                break;
            case GoToDialog.LANGUAGES:
                data = selectionPanelController.getLanguages();
                break;
            default:
                throw new AssertionError();
        }
        
        GoToDialog dialog = new GoToDialog(borderPane.getScene().getWindow(),
                App.getBundle().getString(title));
        dialog.setData(data);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent((t) -> {
            switch (title) {
                case GoToDialog.PROJECTS:
                    selectionPanelController.selectProject(t);
                    break;
                case GoToDialog.COMPONENTS:
                    selectionPanelController.selectComponent(t);
                    break;
                case GoToDialog.LANGUAGES:
                    selectionPanelController.selectLanguage(t);
                    break;
                default:
                    throw new AssertionError();
            }
        });
    }

    private void clearWorkArea() {
        translationTabController.clearTranslationAreas();
        history.clear();
        quickPanelController.clear();
        entryIndexProperty.set(-1);
    }

    public void autoLogin() {
        if (preferences.getBoolean(App.AUTOLOGIN, false)) {
            handleLogin();
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
                    App.getTranslationProvider().getProjects());
            Collections.sort(items);
            Platform.runLater(() -> {
                selectionPanelController.setProjects(items);
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

    private void setupSelectionPanel() {
        selectionPanelController.setupCellFactories(history, borderPane);
        
        selectionPanelController.fetchingDataProperty().addListener((ov, t, t1) -> {
            progressIndicator.setVisible(t1);
            if (t1) {
                clearWorkArea();
            }
        });
     
        selectionPanelController.selectedLanguageProperty().addListener((ov, t, t1) -> {
            if (t1 != null) {
                String selectedLanguage = t1;
                logger.log(Level.INFO, "Selected language: {0}", selectedLanguage);

                clearWorkArea();

                if (!selectedLanguage.equals(preferences.get(App.TRANSLATOR_LANGUAGE, ""))) {
                    preferences.put(App.TRANSLATOR_LANGUAGE, selectedLanguage);
                }

                new Thread(() -> {
                    try {
                        Platform.runLater(() -> {
                            progressIndicator.setVisible(true);
                        });
                        
                        String selectedProject = selectionPanelController.selectedProjectProperty().get();
                        String selectedComponent = selectionPanelController.selectedComponentProperty().get();

                        fileFormat = App.getTranslationProvider()
                                .getFileFormat(selectedProject, selectedComponent,
                                        selectedLanguage);

                        TranslationParserFactory parserFactory = new ParserFactory();
                        TranslationParser translationParser = parserFactory.getParser(fileFormat);

                        translation = App.getTranslationProvider().getFile(selectedProject,
                                selectedComponent, selectedLanguage);
                        translationFile = translationParser.parse(translation);
                        historyAdapter.setOldTranslations(translationFile.getEntries());

                        if (fileFormat.equalsIgnoreCase("json")) {
                            String sourceLanguage = App.getTranslationProvider()
                                    .getFile(selectedProject, selectedComponent, "en");
                            JsonParser jsonTranslationParser
                                    = (JsonParser) translationParser;
                            jsonTranslationParser.setSourceLanguage(true);
                            translationFile = translationParser.parse(sourceLanguage);
                            jsonTranslationParser.setSourceLanguage(false);
                        }
                        
                        quickPanelController.addTableData(translationFile.getEntries());

                        Platform.runLater(() -> {
                            if (entryIndexProperty.equals(0)) {
                                entryIndexProperty.set(-1);
                            }                            
                            if (!translationFile.getEntries().isEmpty()) {
                                 entryIndexProperty.set(0);
                            }
                            progressIndicator.setVisible(false);
                        });
                    } catch (ClientErrorException | ServerErrorException | ProcessingException
                            | IOException | UnsupportedFileFormatException
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
        submitButton.disableProperty().bind(selectionPanelController.noSelectionProperty()
                .or(quickPanelController.tableSelectedIndexProperty().isEqualTo(-1)));

        previousButton.disableProperty().bind(selectionPanelController.noSelectionProperty()
                .or(quickPanelController.filterTextProperty().isEmpty()
                        .and(quickPanelController.tableSelectedIndexProperty().lessThanOrEqualTo(0)))
                .or(quickPanelController.filterTextProperty().isNotEmpty()
                        .and(quickPanelController.tableSelectedIndexProperty().lessThanOrEqualTo(0))));

        nextButton.disableProperty().bind(selectionPanelController.noSelectionProperty()
                .or(quickPanelController.tableSelectedIndexProperty().isEqualTo(-1)
                        .and(quickPanelController.filterTextProperty().isEmpty()))
                .or(Bindings.createBooleanBinding(() -> {
                    return quickPanelController.isFilterIndexAtEnd();
                }, quickPanelController.tableSelectedIndexProperty())));
        
        importMenuItem.disableProperty().bind(submitButton.disableProperty());
        exportMenuItem.disableProperty().bind(submitButton.disableProperty());
    }
    
    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("po", "*.po"), 
                new ExtensionFilter("json", "*.json"));
        
        File selectedFile = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String importedTranslation = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
                submit(importedTranslation);
            } catch (IOException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("po", "*.po"),
                new ExtensionFilter("json", "*.json"));
        fileChooser.setInitialFileName(selectionPanelController.selectedProjectProperty().get() 
                + "-" + selectionPanelController.selectedComponentProperty().get()
                + "-" + selectionPanelController.selectedLanguageProperty().get() 
                + "." + fileFormat);
        File file = fileChooser.showSaveDialog(borderPane.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), translationFile.toString(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void handleSettings() {
        showSettingsDialog(false, false);
    }
    
    @FXML
    private void handleShortcuts() {
        ShortcutsAlert alert = new ShortcutsAlert(borderPane.getScene().getWindow());
        alert.showAndWait();
    }
    
    @FXML
    private void handleAbout() {
        Alert aboutAlert = new Alert(AlertType.INFORMATION);
        aboutAlert.initOwner(borderPane.getScene().getWindow());
        aboutAlert.getDialogPane().setPrefSize(480, 320);
        aboutAlert.setTitle(MessageFormat.format(App.getBundle().getString("about.title {0}"), 
                new Object[] {App.NAME}));
        aboutAlert.setGraphic(new ImageView(App.class.getResource(App.ICON).toString()));
        aboutAlert.setHeaderText(App.NAME + " " + App.VERSION + "\n"
                + App.getBundle().getString("about.description"));
        aboutAlert.setContentText("Java: "
                + System.getProperty("java.runtime.name") + " "
                + System.getProperty("java.runtime.version") + "\n"
                + "JavaFX: " + System.getProperty("javafx.runtime.version") + "\n"
                + App.getBundle().getString("about.libraries")
                +  "jweblate, jgettext, java-diff-utils, "
                + "ikonli-javafx, ikonli-icomoon-pack" + "\n"
                + App.getBundle().getString("about.locale") + Locale.getDefault() + "\n\n"
                + App.getBundle().getString("about.copyright") + "\n"
                + App.getBundle().getString("about.license"));
        aboutAlert.setResizable(true);
        aboutAlert.showAndWait();
    }

    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    @FXML
    private void handleLogin() {
        
        if (App.getTranslationProvider().isAuthenticated()) {
            App.getTranslationProvider().logout();
            selectionPanelController.clearProjects();
            clearWorkArea();
            loginButton.setText(App.getBundle().getString("login"));
        } else {
            progressIndicator.setVisible(true);
            String authToken = App.getAuthTokenEncryptor().decrypt(preferences.get(App.AUTH_TOKEN, ""));
            String apiUri = preferences.get(App.API_URI, "");

            try {
                App.getTranslationProvider().setApiUrl(new URI(apiUri));
            } catch (URISyntaxException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (apiUri.isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(borderPane.getScene().getWindow());
                alert.setResizable(true);
                alert.setTitle(App.getBundle().getString("error.title"));
                alert.setHeaderText(App.getBundle().getString("error.api_uri.empty.header"));
                alert.setContentText(App.getBundle().getString("error.api_uri.empty.content"));
                alert.showAndWait();

                showSettingsDialog(true, true);

            } else if (authToken.isEmpty()) {
                LoginDialog dialog = new LoginDialog(borderPane.getScene().getWindow(),
                        preferences);
                Optional<String> result = dialog.showAndWait();
                result.ifPresentOrElse((authTokenEntered) -> {
                    authenticate(authTokenEntered);
                }, () -> progressIndicator.setVisible(false));
            } else {
                authenticate(authToken);
            }
        }
    }

    private void authenticate(String authToken) {
        new Thread(() -> {
            try {
                App.getTranslationProvider().setAuthToken(authToken);
                App.getTranslationProvider().authenticate();
            } catch (ClientErrorException | ServerErrorException ex) {
                Logger.getLogger(BrowserController.class.getName()).log(Level.SEVERE, null, ex);                
                
                switch (ex.getResponse().getStatusInfo().toEnum()) {
                    case UNAUTHORIZED:
                        preferences.put(App.AUTH_TOKEN, App.getAuthTokenEncryptor().encrypt(authToken));
                        Platform.runLater(() -> {
                            LoginDialog dialog = new LoginDialog(borderPane
                                    .getScene().getWindow(), preferences);
                            Optional<String> result = dialog.showAndWait();
                            result.ifPresentOrElse((authTokenEntered) -> {
                                authenticate(authTokenEntered);
                            }, () -> progressIndicator.setVisible(false));
                        });       
                        break;
                    case NOT_FOUND:
                        Platform.runLater(() -> {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.initOwner(borderPane.getScene().getWindow());
                            alert.setResizable(true);
                            alert.setTitle(App.getBundle().getString("error.title"));
                            alert.setHeaderText(App.getBundle().getString("error.api_uri.not_found.header"));
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

            if (App.getTranslationProvider().isAuthenticated()) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    loginButton.setText(App.getBundle().getString("logout"));
                });
                preferences.put(App.AUTH_TOKEN, App.getAuthTokenEncryptor().encrypt(authToken));
                getProjects();
            }

        }).start();
    }
    
    private void showSettingsDialog(boolean focusApiUriField, boolean callHandleLogin) {
        SettingsDialog settingsDialog = new SettingsDialog(borderPane
                .getScene().getWindow(), preferences);
        if (focusApiUriField) {
            settingsDialog.focusApiUriField();
        }
        Optional<Map<String, String>> result = settingsDialog.showAndWait();
        result.ifPresent(data -> {
            updatePreferences(data);
            if (callHandleLogin) {
                handleLogin();
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
    
    private List<String> diff(String translationFileStr) {
        List<String> oldTranslation = Arrays.asList(translation.split("\n"));
        List<String> newTranslation = Arrays.asList(translationFileStr.split("\n"));

//        System.out.println("New translation:");
//        newTranslation.forEach(System.out::println);
        logger.log(Level.INFO, "oldTranslation size: {0} newTranslation size: {1}",
                new Object[]{oldTranslation.size(), newTranslation.size()});

        Patch<String> patch = DiffUtils.diff(oldTranslation, newTranslation);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                App.getBundle().getString("diff.oldtranslation"),
                App.getBundle().getString("diff.newtranslation"), oldTranslation, patch, 0);
//            unifiedDiff.forEach(System.out::println);

        return unifiedDiff;
    }

    private void submit(String translationFileStr) {
        logger.log(Level.INFO, "lines old: {0} lines new: {1}", new Object[]{
            translation.split("\n", -1).length, translationFileStr.split("\n", -1).length});
        
        Task<Map<String, String>> submitTask = new Task() {
            @Override
            protected Object call() throws Exception {
                progressIndicator.setVisible(true);
                return App.getTranslationProvider().submit(
                        selectionPanelController.selectedProjectProperty().get(),
                        selectionPanelController.selectedComponentProperty().get(),
                        selectionPanelController.selectedLanguageProperty().get(),
                        translationFileStr);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                progressIndicator.setVisible(false);
                
                Map<String, String> submitResult = (Map<String, String>) getValue();

                String contextText = "";
                int acceptedChanges = Integer.parseInt(submitResult.get("accepted"));

                for (Map.Entry<String, String> entry : submitResult.entrySet()) {
                    contextText += entry.getKey() + ": " + entry.getValue() + "\n";
                }

                contextText = contextText.substring(0, contextText.length() - 1);

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.initOwner(borderPane.getScene().getWindow());
                alert.setTitle(App.getBundle().getString("submit.results"));
                alert.setHeaderText(MessageFormat.format(App.getBundle()
                        .getString("submit.accepted {0}"), new Object[]{acceptedChanges}));
                alert.setContentText(contextText);
                alert.setResizable(true); // FIXME: Workaround for JavaFX 11
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.showAndWait();

                if (acceptedChanges > 0) {
                    translation = translationFileStr;
                    history.clear();
                }
            }

            @Override
            protected void failed() {
                super.failed();
                progressIndicator.setVisible(false);
                logger.log(Level.SEVERE, null, getException());
                showExceptionAlert(getException());
            }
            
        };
        
        Task<List<String>> diffTask = new Task<List<String>>() {

            @Override
            protected List<String> call() throws Exception {
                progressIndicator.setVisible(true);
                return diff(translationFileStr);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                progressIndicator.setVisible(false);

                SubmitAlert submitAlert = new SubmitAlert(AlertType.CONFIRMATION,
                        borderPane.getScene().getWindow());
                submitAlert.setDiff(getValue());
                Optional<ButtonType> result = submitAlert.showAndWait();

                if (result.get() == ButtonType.OK) {
                    new Thread(submitTask).start();
                }
            }
            
            @Override
            protected void failed() {
                super.failed();
                progressIndicator.setVisible(false);
                logger.log(Level.SEVERE, null, getException());
                showExceptionAlert(getException());
            }
            
        };
        
        new Thread(diffTask).start();
    }
    
    @FXML
    private void handleSubmit() {
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
        submit(translationFile.toString());
    }
}
