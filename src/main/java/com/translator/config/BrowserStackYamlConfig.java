package com.translator.config;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.HashMap;

public class BrowserStackYamlConfig {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackYamlConfig.class);
    private final Map<String, Object> configData;
    private static final String BROWSERSTACK_URL = "https://hub-cloud.browserstack.com/wd/hub";

    public BrowserStackYamlConfig(String yamlPath) {
        try (InputStream input = new FileInputStream(yamlPath)) {
            Yaml yaml = new Yaml();
            this.configData = yaml.load(input);
            logger.info("Loaded BrowserStack configuration: {}", configData);
        } catch (Exception e) {
            logger.error("Error loading browserstack.yml: {}", e.getMessage());
            throw new RuntimeException("Failed to load BrowserStack configuration", e);
        }
    }

    public Map<String, Object> getConfigData() {
        return configData;
    }

    public WebDriver createDriver(String browser, String device, String os) {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();

            // Set BrowserStack credentials
            capabilities.setCapability("browserstack.user", configData.get("userName"));
            capabilities.setCapability("browserstack.key", configData.get("accessKey"));

            // Enable video recording and debug
            capabilities.setCapability("browserstack.video", "true");
            capabilities.setCapability("browserstack.debug", "true");
            capabilities.setCapability("browserstack.networkLogs", "true");

            // Set project and build information
            capabilities.setCapability("project", configData.get("projectName"));
            capabilities.setCapability("build", configData.get("buildName"));
            capabilities.setCapability("name", browser + " on " + os);

            // Set local testing capabilities if enabled
            if (Boolean.TRUE.equals(configData.get("browserstackLocal"))) {
                capabilities.setCapability("browserstack.local", "true");
                if (BrowserStackLocalManager.isRunning()) {
                    capabilities.setCapability("browserstack.localIdentifier",
                            BrowserStackLocalManager.getLocalIdentifier());
                }
            }

            // Set platform specific capabilities
            if (device.equals("Windows") || device.equals("OS X")) {
                // Desktop configuration
                capabilities.setCapability("browser", browser);
                capabilities.setCapability("browser_version", "latest");
                capabilities.setCapability("os", device);
                capabilities.setCapability("os_version", os);
                capabilities.setCapability("resolution", "1920x1080");
            } else {
                // Mobile configuration
                capabilities.setCapability("device", device);
                capabilities.setCapability("os_version", os);
                capabilities.setCapability("real_mobile", "true");
            }

            logger.info("Creating BrowserStack session with capabilities: {}", capabilities);
            return new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);

        } catch (Exception e) {
            logger.error("Error creating BrowserStack driver: {}", e.getMessage());
            throw new RuntimeException("Failed to create BrowserStack driver", e);
        }
    }
}