package com.translator.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrowserStackConfig {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackConfig.class);
    private static final String BROWSERSTACK_URL = "https://hub-cloud.browserstack.com/wd/hub";

    public static WebDriver createDriver(String browser, String device, String os) {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("browserName", browser);
            capabilities.setCapability("browserstack.user", System.getenv("BROWSERSTACK_USERNAME"));
            capabilities.setCapability("browserstack.key", System.getenv("BROWSERSTACK_ACCESS_KEY"));

            Map<String, String> browserstackOptions = new HashMap<>();
            browserstackOptions.put("os", os);
            browserstackOptions.put("osVersion", "latest");
            browserstackOptions.put("deviceName", device);
            browserstackOptions.put("projectName", "El País Translator");
            browserstackOptions.put("buildName", "Translation Test");
            browserstackOptions.put("sessionName", browser + " - " + device);
            browserstackOptions.put("local", "false");
            browserstackOptions.put("debug", "true");
            browserstackOptions.put("networkLogs", "true");

            capabilities.setCapability("bstack:options", browserstackOptions);

            logger.info("Creating BrowserStack session for {}", browser);
            return new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);

        } catch (Exception e) {
            logger.error("Error creating BrowserStack driver: {}", e.getMessage());
            throw new RuntimeException("Failed to create BrowserStack driver", e);
        }
    }
}