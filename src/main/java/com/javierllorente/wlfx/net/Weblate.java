/*
 * Copyright (C) 2020-2021 Javier Llorente <javier@opensuse.org>
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private List<String> translations;

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
    
    public boolean isLegacyMode() {
        return http.isLegacyMode();
    }

    public void setLegacyMode(boolean legacyMode) {
        http.setLegacyMode(legacyMode);
    }

    public List<String> getProjects() throws URISyntaxException, IOException, 
            InterruptedException {
        String response = http.get(new URI(getApiUrl() + "projects/"));
        List<String> projects = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode resultsNode = rootNode.path("results");
        Iterator<JsonNode> elements = resultsNode.elements();

        while (elements.hasNext()) {
            JsonNode project = elements.next();
            projects.add(project.path("slug").asText());
            System.out.println("Project: " + project.path("slug"));
        }

        return projects;
    }

    public List<String> getComponents(String project) throws URISyntaxException,
            IOException, InterruptedException {
        String resource = String.format("projects/%s/components/", project);
        String response = http.get(new URI(getApiUrl() + resource));
        List<String> components = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode resultsNode = rootNode.path("results");
        Iterator<JsonNode> elements = resultsNode.elements();

        while (elements.hasNext()) {
            JsonNode component = elements.next();
            components.add(component.path("slug").asText());
            System.out.println("Component: " + component.path("slug"));
        }

        return components;
    }

    public List<String> getTranslations(String project, String component, int page)
            throws URISyntaxException, IOException, InterruptedException {
        System.out.println("getTranslations() page: " + page);
        String resource = String.format("components/%s/%s/translations/?page=%d", project, component, page);
        String response = http.get(new URI(getApiUrl() + resource));

        if (page == 1) {
            translations = new ArrayList<>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode rootNode = objectMapper.readTree(response);

        JsonNode resultsNode = rootNode.path("results");
        Iterator<JsonNode> elements = resultsNode.elements();

        while (elements.hasNext()) {
            JsonNode translation = elements.next();
            translations.add(translation.path("language_code").asText());
            System.out.println("translation: " + translation.path("language_code"));
        }

        JsonNode nextNode = rootNode.path("next");
        if (!nextNode.isMissingNode() && !nextNode.asText().equals("null")) {
            getTranslations(project, component, ++page);
        }

        return translations;
    }

    public String getFile(String project, String component, String language)
            throws URISyntaxException, IOException, InterruptedException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        String response = http.get(new URI(getApiUrl() + resource));
        return response;
    }

    public String submit(String project, String component, String language, String file)
            throws URISyntaxException, IOException, InterruptedException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        String response = http.post(new URI(getApiUrl() + resource), file);
        logger.log(Level.INFO, "Response: {0}", response);
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode fieldNode = rootNode.get("result");

        if (fieldNode == null || fieldNode.isNull()) {
            throw new IOException(response);
        }
 
        return response;
    }    
}
