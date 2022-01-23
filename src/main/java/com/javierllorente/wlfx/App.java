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
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final String ICON = "/wlfx.png";
    
    public static final String TRANSLATOR_NAME = "translator_name";
    public static final String TRANSLATOR_EMAIL = "translator_email";
    public static final String TRANSLATOR_LANGUAGE = "translator_language";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String API_URI = "api_uri";
    public static final String AUTOLOGIN = "autologin";    
    
    private final String applicationStyle = "app.css";
    private final String applicationBundle = getClass().getPackageName() + "/i18n/ApplicationBundle";
    
    private static Scene scene;
    private static Weblate weblate;
    private static AuthTokenEncryptor authTokenEncryptor;
    private static ResourceBundle bundle;

    public App() throws URISyntaxException {
        weblate = new Weblate();
        authTokenEncryptor = new AuthTokenEncryptor();
        bundle = ResourceBundle.getBundle(applicationBundle);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = getFXMLLoader("Browser");
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
    
    public static Weblate getWeblate() {
        return weblate;
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