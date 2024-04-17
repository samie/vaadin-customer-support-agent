package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This example is from https://github.com/langchain4j/langchain4j-examples/tree/main/spring-boot-example,
 * which is licensed under the Apache License 2.0.
 */
@SpringBootApplication
@Theme(value = "customer-support-agent")
@Push
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
