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
package com.javierllorente.wlfx;

import com.javierllorente.jwl.TranslationProvider;
import com.javierllorente.wlfx.crypto.AuthTokenEncryptor;
import com.javierllorente.jwl.Weblate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import javafx.scene.image.Image;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final String NAME = "WLFX";
    public static final String VERSION = "0.1";
    public static final String USER_AGENT = NAME + "/" + VERSION;
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final String ICON = "/wlfx.png";
    
    public static final String TRANSLATOR_NAME = "translator_name";
    public static final String TRANSLATOR_EMAIL = "translator_email";
    public static final String TRANSLATOR_LANGUAGE = "translator_language";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String API_URI = "api_uri";
    public static final String AUTOLOGIN = "autologin";    
    
    private final String applicationView = "Browser";
    private final String applicationStyle = "style.css";
    private final String applicationBundle = getClass().getPackageName() + ".i18n.ApplicationBundle";
    
    private static Scene scene;
    private static TranslationProvider translationProvider;
    private static AuthTokenEncryptor authTokenEncryptor;
    private static ResourceBundle bundle;

    public App() throws URISyntaxException {
        translationProvider = new Weblate();
        translationProvider.setUserAgent(USER_AGENT);
        authTokenEncryptor = new AuthTokenEncryptor();
        bundle = ResourceBundle.getBundle(applicationBundle);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = getFXMLLoader(applicationView);
        scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(applicationStyle);
        stage.setScene(scene);
        stage.setTitle(NAME);
        stage.getIcons().add(new Image(App.class.getResourceAsStream(ICON)));
        stage.show(); 
        BrowserController browserController = (BrowserController) loader.getController();
        browserController.setupScene(scene);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = getFXMLLoader(fxml);
        return fxmlLoader.load();
    }
    
    public static FXMLLoader getFXMLLoader(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"), bundle);
        return fxmlLoader;
    }
    
    public static TranslationProvider getTranslationProvider() {
        return translationProvider;
    }

    public static AuthTokenEncryptor getAuthTokenEncryptor() {
        return authTokenEncryptor;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void main(String[] args) {
        launch();
    }
}