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
package com.javierllorente.wlfx.net;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;

/**
 *
 * @author javier
 */
public class Weblate {

    private static final Logger logger = Logger.getLogger(Weblate.class.getName());
    private final WeblateHttp http;

    public Weblate() {
        http = new WeblateHttp();
    }

    public URI getApiUrl() {
        return http.getApiURI();
    }

    public void setApiUrl(URI apiUri) {
        http.setApiURI(apiUri);
    }
    
   public String getAuthToken() {
       return http.getAuthToken();
   }
   
   public void setAuthToken(String authToken) {
       http.setAuthToken(authToken);
   }

    public String getUsername() {
        return http.getUsername();
    }

    public void setUsername(String username) {
        http.setUsername(username);
    }

    public String getPassword() {
        return http.getPassword();
    }

    public void setPassword(String password) {
        http.setPassword(password);
    }

    public boolean isAuthenticated() {
        return http.isAuthenticated();
    }

    public void authenticate()
            throws AuthenticationException, IOException, InterruptedException {
        http.authenticate();
    }
    
    private void get(String resource, String path, int page, List<String> elements) 
            throws URISyntaxException, IOException, InterruptedException {
        String response = http.get(new URI(getApiUrl() + resource + "?page=" + page));

        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
            jsonObject = jsonReader.readObject();
        }
        
        JsonArray results = jsonObject.getJsonArray("results");        
        for (JsonValue value : results) {
            elements.add(value.asJsonObject().getString(path));
            System.out.println(value.asJsonObject().getString(path));
        }
        
        if (jsonObject.containsKey("next") && !jsonObject.isNull("next")) {
            get(resource, path, ++page, elements);
        }        
    }
    
    private List<String> getElements(String resource, String path) 
            throws URISyntaxException, IOException, InterruptedException {
        List<String> elements = new ArrayList<>();
        get(resource, path, 1, elements);
        return elements;
    }

    public List<String> getProjects() 
            throws URISyntaxException, IOException, InterruptedException {
        return getElements("projects/", "slug");
    }

    public List<String> getComponents(String project) 
            throws URISyntaxException, IOException, InterruptedException {
        return getElements("projects/" + project + "/components/", "slug");
    }

    public List<String> getTranslations(String project, String component)
            throws URISyntaxException, IOException, InterruptedException {
        return getElements("components/" + project + "/" + component + "/translations/", 
                "language_code");
    }
    
    public String getFileFormat(String project, String component, String language)
            throws URISyntaxException, IOException, InterruptedException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/";
        String response = http.get(new URI(getApiUrl() + resource));

        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
            jsonObject = jsonReader.readObject();
        }
        
        JsonObject componentObject = jsonObject.getJsonObject("component");
        return componentObject.getString("file_format");        
    }

    public String getFile(String project, String component, String language)
            throws URISyntaxException, IOException, InterruptedException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        String response = http.get(new URI(getApiUrl() + resource));
        return response;
    }

    public Map<String, String> submit(String project, String component, String language, String file)
            throws URISyntaxException, IOException, InterruptedException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        String response = http.post(new URI(getApiUrl() + resource), file);
        logger.log(Level.INFO, "Response: {0}", response);
        
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
            jsonObject = jsonReader.readObject();
        }
        
        if (!jsonObject.containsKey("result") || jsonObject.isNull("result")) {
            throw new IOException(response);
        }

        Map<String, String> result = new HashMap<>();
        for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());            
        }
        
        return result;
    }
    
}
