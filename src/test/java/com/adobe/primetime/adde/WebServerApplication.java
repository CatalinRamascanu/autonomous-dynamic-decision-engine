package com.adobe.primetime.adde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@SpringBootApplication
public class WebServerApplication extends SpringBootServletInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(WebServerApplication.class);

    public static void main(String[] args) {
        //Setup engine
        LOG.info("Initializing Decision Engine...");
        DecisionEngine decisionEngine = FactoryDecisionEngine.getSingletonInstance();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");

        // Start spring
        SpringApplication.run(WebServerApplication.class, args);
    }

    @Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebServerApplication.class);
	}
}
