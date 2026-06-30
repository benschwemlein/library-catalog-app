package com.example.cucumber;

import com.example.catalog.CatalogApplication;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {CatalogApplication.class, CucumberSpringConfiguration.CucumberTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    @TestConfiguration
    @ComponentScan("com.example.cucumber")
    static class CucumberTestConfig {
    }

    @LocalServerPort
    private int port;

    @Before(order = 1)
    public void configureRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
