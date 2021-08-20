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
package com.javierllorente.wlfx.net;

import com.javierllorente.wlfx.App;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author javier
 */
public class WeblateHttp {

    private static final Logger logger = Logger.getLogger(WeblateHttp.class.getName());
    private HttpClient client;
    private String username;
    private String password;
    private String authToken;
    private URI apiURI;
    boolean authenticated;

    public WeblateHttp() {
        authenticated = false;
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public WeblateHttp(URI apiURI) {
        this();
        this.apiURI = apiURI;
    }    

    public URI getApiURI() {
        return apiURI;
    }

    public void setApiURI(URI apiURI) {
        this.apiURI = apiURI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = "Token " + authToken;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    } 
    
    public void authenticate() throws AuthenticationException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", App.NAME + " " + App.VERSION)
                .header("Authorization", authToken)
                .GET().uri(apiURI)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        authenticated = (response.statusCode() == HttpsURLConnection.HTTP_OK);
        if (!authenticated) {
            throw new AuthenticationException(Integer.toString(response.statusCode()));
        }
        logger.info(getConnectionInfo(response));
    }
    
    public String get(URI uri) throws IOException, InterruptedException {       
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", App.NAME + " " + App.VERSION)
                .header("Authorization", authToken)
                .GET().uri(uri)
                .build();        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info(getConnectionInfo(response));
        if (response.statusCode() != HttpsURLConnection.HTTP_OK) {
            throw new IOException(Integer.toString(response.statusCode()));
        }
        return response.body();
    } 
    
    public String post(URI uri, String strings) throws IOException, InterruptedException {
        String boundary = App.NAME + System.currentTimeMillis();
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("method", "translate");
        formParams.put("conflicts", "replace-translated");
        
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", App.NAME + " " + App.VERSION)
                .header("Authorization", authToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(ofMimeMultipartData(formParams, strings, boundary))
                .uri(uri)
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        logger.info(getConnectionInfo(response));

        return response.body();
    }
    
    private BodyPublisher ofMimeMultipartData(Map<String, String> formParams, 
            String strings, String boundary) throws IOException {        
        List<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("--" + boundary + "\r\n" + "Content-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        
        byteArrays.add(separator);
        byteArrays.add(("\"" + "file" + "\"; filename=\"" + "strings.po" + "\"\r\n" 
                + "Content-Type: " + "text/x-gettext-translation" + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(strings.getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("\r\n").getBytes(StandardCharsets.UTF_8));
        
        formParams.entrySet().forEach(entry -> {
            byteArrays.add(separator);
            byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                    .getBytes(StandardCharsets.UTF_8));
        });
        
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        
        return BodyPublishers.ofByteArrays(byteArrays);
    }

    private String getConnectionInfo(HttpResponse<String> response) throws IOException {
        HttpHeaders headers = response.headers();
        headers.map().forEach((k, v) -> System.out.println(k + ":" + v));
        return "URL: " + response.uri() + 
                ", method: " + response.request().method() + 
                ", response: " + response.statusCode();
    }    
}
