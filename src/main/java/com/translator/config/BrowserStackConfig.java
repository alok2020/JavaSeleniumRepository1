package com.translator.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.HashMap;

public class BrowserStackConfig {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackConfig.class);
    private static final String BROWSERSTACK_URL = "https://ak_UQ2Gsq:eDHsRmFvWZvPhcDqpxVv@hub-cloud.browserstack.com/wd/hub";

    public static WebDriver createDriver(String browser, String device, String os) {
        try {
            logger.info("Setting up BrowserStack with browser: {}, device: {}, os: {}", browser, device, os);

            HashMap<String, Object> browserstackOptions = new HashMap<>();
            browserstackOptions.put("seleniumVersion", "4.15.0");
            browserstackOptions.put("projectName", "El Pais Translator");
            browserstackOptions.put("buildName", "Translation Test " + java.time.LocalDateTime.now());
            browserstackOptions.put("sessionName", browser + " on " + device + " " + os);
            browserstackOptions.put("debug", true);
            browserstackOptions.put("networkLogs", true);
            browserstackOptions.put("consoleLogs", "info");

            if (device.equals("Windows") || device.equals("OS X")) {
                // Desktop configuration
                browserstackOptions.put("os", device);
                browserstackOptions.put("osVersion", os);
                browserstackOptions.put("browserName", browser);
                browserstackOptions.put("browserVersion", "latest");
                browserstackOptions.put("resolution", "1920x1080");
            } else {
                // Mobile configuration
                browserstackOptions.put("deviceName", device);
                browserstackOptions.put("osVersion", os);
                if (device.contains("iPhone")) {
                    browserstackOptions.put("browserName", "safari");
                } else {
                    browserstackOptions.put("browserName", "chrome");
                }
                browserstackOptions.put("realMobile", "true");
            }

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("bstack:options", browserstackOptions);

            logger.info("Creating BrowserStack driver with capabilities: {}", capabilities);
            WebDriver driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);
            logger.info("Successfully created BrowserStack driver");

            return driver;
        } catch (Exception e) {
            logger.error("Failed to create BrowserStack driver: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create BrowserStack driver", e);
        }
    }
}