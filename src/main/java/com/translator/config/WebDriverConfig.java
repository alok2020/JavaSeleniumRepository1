package com.translator.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);

    public static WebDriver createDriver() {
        logger.info("Setting up ChromeDriver...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080"
        );

        logger.info("Creating new ChromeDriver instance...");
        return new ChromeDriver(options);
    }
}