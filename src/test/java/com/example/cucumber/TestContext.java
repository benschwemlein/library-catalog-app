package com.example.cucumber;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class TestContext {

    private String authToken;
    private String refreshToken;
    private Response lastResponse;
    private Long lastCreatedId;
    private String lastCreatedBarcode;
    private final Map<String, String> variables = new HashMap<>();

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public Long getLastCreatedId() { return lastCreatedId; }
    public void setLastCreatedId(Long lastCreatedId) { this.lastCreatedId = lastCreatedId; }

    public String getLastCreatedBarcode() { return lastCreatedBarcode; }
    public void setLastCreatedBarcode(String barcode) { this.lastCreatedBarcode = barcode; }

    public void setVariable(String name, String value) { variables.put(name, value); }
    public String getVariable(String name) { return variables.get(name); }

    public String resolveUrl(String urlTemplate) {
        String url = urlTemplate;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            url = url.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return url;
    }
}
