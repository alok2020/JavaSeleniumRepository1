package com.translator;

import com.translator.config.BrowserStackConfig;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTest.class);

    @Test
    public void testBrowserStackConnection() {
        logger.info("Starting BrowserStack connection test");
        WebDriver driver = null;
        try {
            driver = BrowserStackConfig.createDriver("Chrome", "Windows", "10");
            logger.info("Driver created successfully");

            driver.get("https://www.google.com");
            logger.info("Navigated to Google. Page title: {}", driver.getTitle());

        } catch (Exception e) {
            logger.error("Test failed: ", e);
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
                logger.info("Driver quit successfully");
            }
        }
    }
}