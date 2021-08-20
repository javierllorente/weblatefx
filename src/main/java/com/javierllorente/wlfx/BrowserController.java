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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.javierllorente.jgettext.POFile;
import com.javierllorente.jgettext.POParser;
import com.javierllorente.jgettext.TranslationElement;
import com.javierllorente.jgettext.TranslationEntry;
import com.javierllorente.jgettext.TranslationParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
    private String translation;
    private POFile poFile;
    private int quickTableIndex;
    private IntegerProperty entryIndexProperty;
    private ObservableList<TranslationEntry> quickTableData;
    private FilteredList<TranslationEntry> quickTableFilteredData;
    private boolean dataLoaded;
    private BooleanBinding menuItemNotSelected;
    
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
    private ComboBox<String> languagesComboBox;
    
    @FXML
    private TableView<TranslationEntry> quickTable;
    
    @FXML
    private TableColumn<Integer, String> entryColumn;
    
    @FXML
    private TableColumn<TranslationEntry, String> sourceColumn;
    
    @FXML
    private TableColumn<TranslationEntry, String> targetColumn;
    
    @FXML
    private TextField quickFilter;
    
    @FXML
    private ChoiceBox quickChoice;
    
    @FXML
    private TextArea metadataTextArea;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        preferences = Preferences.userRoot();
        quickTableIndex = 0;
        entryIndexProperty = new SimpleIntegerProperty(-1);
        translationTabController = new TranslationTabController(workArea);
        dataLoaded = false;
        
        setupProjectListView();
        setupComponentsListView();
        setupLanguagesComboBox();
        setupTable();
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
                poFile.updateEntry(oldIndex.intValue(), 
                        translationTabController.getTranslations());
                quickTableData.set(oldIndex.intValue(), 
                        poFile.getEntries().get(oldIndex.intValue()));
            }
            
            if (poFile.getEntries().get(newIndex.intValue()).getMsgId() != null) {    

                if (!quickTable.getSelectionModel().isSelected(quickTableIndex)) {
                    quickTable.getSelectionModel().select(quickTableIndex);
                    quickTable.scrollTo(quickTableIndex);
                }
                
                metadataTextArea.clear();
                poFile.getEntries().get(newIndex.intValue()).getComments().forEach((t) -> {
                    metadataTextArea.appendText(t
                            .replaceAll("^#(\\.|\\:|\\,)\\s", "") + "\n");
                });

                if (poFile.getEntries().get(newIndex.intValue()).isPlural()) {
                    translationTabController.clearTranslationAreas();
                } else {
                    translationTabController.clearAllButFirst();
                }

                translationTabController.loadTranslations(poFile.getEntries()
                        .get(newIndex.intValue()));
            }
        });
        
        Platform.runLater(() -> {
            autoLogin();
        });
    }    

    private void setupTable() {        
        quickTableData = FXCollections.observableArrayList();
        quickTableFilteredData = new FilteredList<>(quickTableData, f -> true);
        
        SortedList<TranslationEntry> quickTableSortedData = new SortedList<>(quickTableFilteredData);
        quickTableSortedData.comparatorProperty().bind(quickTable.comparatorProperty());
        quickTable.setItems(quickTableSortedData);
        
        quickTable.setOnSort((t) -> {
            quickTableIndex = quickTable.getSelectionModel().getSelectedIndex();
            quickTable.scrollTo(quickTableIndex);
        });
        
        quickTable.setRowFactory((p) -> {
            TableRow<TranslationEntry> row = new TableRow<>();
            row.setOnMouseClicked((t) -> {
                if (t.getButton() == MouseButton.PRIMARY) {
                    quickTableIndex = row.getIndex();
                    entryIndexProperty.set(Integer.parseInt((String) p.getColumns()
                            .get(0).getCellData(row.getIndex())));
                }
            });
            return row;
        });
        
        entryColumn.setCellValueFactory((p) -> {
           return new ReadOnlyObjectWrapper(quickTableData.indexOf(p.getValue()) + "");
        });

        sourceColumn.setCellValueFactory((p) -> {
            return new ReadOnlyObjectWrapper(
                    p.getValue().isPlural()
                    ? getMessageDisplayString(p.getValue().getMsgId())
                    + ", "
                    + getMessageDisplayString(p.getValue().getMsgIdPluralElement().get())
                    : getMessageDisplayString(p.getValue().getMsgId()));
        });

        targetColumn.setCellValueFactory((p) -> {
            TranslationEntry entry = p.getValue();

            return new SimpleStringProperty(
                    entry.isPlural()
                    ? getElementsDisplayString(entry.getMsgStrElements())
                    : getMessageDisplayString(entry.getMsgStr()));
        });
        
        ChangeListener<String> quickFilterListener = ((ov, oldValue, newValue) -> {
            
            if (!oldValue.equals(newValue)) {
                quickTableIndex = -1;
            }
            
            quickTableFilteredData.setPredicate((f) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String entry;
                
                // Filter by source/target
                if (quickChoice.getSelectionModel()
                        .selectedIndexProperty().get() == 0) {
                    entry = f.isPlural()
                            ? getMessageDisplayString(f.getMsgId())
                            + ", " + getMessageDisplayString(f
                                    .getMsgIdPluralElement().get())
                            : getMessageDisplayString(f.getMsgId());
                } else {                    
                    entry = f.isPlural() 
                            ? getElementsDisplayString(f.getMsgStrElements()) 
                            : getMessageDisplayString(f.getMsgStr());  
                }                

                String lowerCaseFilter = newValue.toLowerCase();
                return entry.toLowerCase().contains(lowerCaseFilter);
           });
        });
        
        quickFilter.textProperty().addListener(quickFilterListener);
        
        quickChoice.setItems(FXCollections.observableArrayList("Source", "Target"));
        quickChoice.getSelectionModel().select(1);
        quickChoice.setOnAction((t) -> {
            quickFilterListener.changed(quickFilter.textProperty(), "",
                    quickFilter.textProperty().get());
        });
    }

    private String getMessageDisplayString(List<String> msg) {
        return String.join(", ", msg).replace("\n", "").replaceFirst(", ", "");
    }
    
    private String getElementsDisplayString(List<TranslationElement> elements) {
        return elements
                .stream().map(Object::toString)
                .collect(Collectors.joining(", "))
                .replace("\n", "").replaceAll("msgstr\\[\\d\\]", "");
    }
    
    private void clearWorkArea() {
        translationTabController.clearTranslationAreas();
        quickTableData.clear();
        metadataTextArea.clear();
        dataLoaded = false;
        quickTableIndex = 0;
        entryIndexProperty.set(-1);
    }
    
    public void autoLogin() {
        if (preferences.getBoolean(App.AUTOLOGIN, false)) {
            handleSignIn();
        }
    }    
    
    @FXML
    private void previousItem() {
        entryIndexProperty.set(Integer.parseInt((String) quickTable.getColumns()
                .get(0).getCellData(--quickTableIndex)));
    }
    
    @FXML
    private void nextItem() {
        entryIndexProperty.set(Integer.parseInt((String) quickTable.getColumns()
                .get(0).getCellData(++quickTableIndex)));
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
                Collections.sort(items);
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
                        languagesComboBox.getItems().clear();
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
                                    // FIXME: Glossaries are currently not supported
                                    components.remove("glossary");
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
        languagesComboBox.setItems(languages);
        
        componentsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    if (t1 == null) {
                        languagesComboBox.getItems().clear();
                    } else {
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
                                        selectedProject, selectedComponent.toLowerCase(), 1);
                                Collections.sort(items);
                                Platform.runLater(() -> {
                                    languages.setAll(items);

                                    String translatorLanguage = preferences
                                            .get(App.TRANSLATOR_LANGUAGE, "");

                                    if (!translatorLanguage.isEmpty()
                                            && languages.contains(translatorLanguage)) {
                                        languagesComboBox.getSelectionModel()
                                                .select(translatorLanguage);
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

    private void setupLanguagesComboBox() {        
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
                                translation = App.getWeblate().getFile(selectedProject, selectedComponent, t1);
                                TranslationParser poParser = new POParser();
                                poFile = (POFile) poParser.parse(translation);
                                quickTableData.addAll(poFile.getEntries());
                                dataLoaded = !quickTableData.isEmpty();

                                Platform.runLater(() -> {
                                    if (entryIndexProperty.equals(0)) {
                                        entryIndexProperty.set(-1);
                                    }
                                    entryIndexProperty.set(0);
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
        menuItemNotSelected = projectsListView.getSelectionModel().selectedItemProperty().isNull()
                .or(componentsListView.getSelectionModel().selectedItemProperty().isNull())
                .or(languagesComboBox.getSelectionModel().selectedItemProperty().isNull());

        submitButton.disableProperty().bind(menuItemNotSelected
                .or(quickTable.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));

        previousButton.disableProperty().bind(menuItemNotSelected
                .or(quickFilter.textProperty().isEmpty()
                        .and(quickTable.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0)))
                .or(quickFilter.textProperty().isNotEmpty()
                        .and(quickTable.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0))));

        nextButton.disableProperty().bind(menuItemNotSelected
                .or(quickTable.getSelectionModel().selectedIndexProperty().isEqualTo(-1)
                        .and(quickFilter.textProperty().isEmpty()))
                .or(Bindings.createBooleanBinding(() -> {
                    return (quickTable.getSelectionModel().getSelectedIndex()
                            == quickTableFilteredData.size() - 1);
                }, quickTable.getSelectionModel().selectedIndexProperty())));
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
        aboutAlert.getDialogPane().setPrefSize(480, 320);
        aboutAlert.setTitle("About " + App.NAME);
        aboutAlert.setGraphic(new ImageView(App.class.getResource("/wlfx.png").toString()));
        aboutAlert.setHeaderText(App.NAME + " " + App.VERSION + "\n" 
                + "A JavaFX-based Weblate client");
        aboutAlert.setContentText("Java: " 
                + System.getProperty("java.runtime.name") + " "
                + System.getProperty("java.runtime.version") + "\n"
                + "JavaFX: " + System.getProperty("javafx.runtime.version") + "\n"
                + "Libraries: jgettext, jackson-databind, java-diff-utils, "
                + "ikonli-javafx, ikonli-icomoon-pack" + "\n\n"
                + "Copyright © 2020, 2021 Javier Llorente" + "\n"
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
            poFile.updateEntry(entryIndexProperty.get(), translationTabController.getTranslations());
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
                    
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(submitResult);
                    
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        
                        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                        String contextText = "";
                        int acceptedChanges = jsonNode.get("accepted").asInt();
                        
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            String fieldName = field.getKey();
                            JsonNode fieldValue = field.getValue();
                            contextText += fieldName + ": " + fieldValue.asText() + "\n";
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
                    });
                    
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
