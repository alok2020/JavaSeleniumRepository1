package com.translator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import com.translator.model.Article;
import com.translator.scraper.ElPaisOpinionScraper;
import com.translator.service.TranslationService;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class ElPaisBrowserStackTest {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisBrowserStackTest.class);
    private WebDriver driver;
    private ElPaisOpinionScraper scraper;
    private TranslationService translator;

    private static final String BROWSERSTACK_USERNAME = "ak_UQ2Gsq";
    private static final String BROWSERSTACK_ACCESS_KEY = "eDHsRmFvWZvPhcDqpxVv";
    private static final String BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeMethod
    @Parameters({"browser", "os", "osVersion"})
    public void setup(String browser, String os, String osVersion) {
        try {
            logger.info("Setting up BrowserStack session for {} on {} {}", browser, os, osVersion);

            DesiredCapabilities capabilities = new DesiredCapabilities();
            HashMap<String, Object> bstackOptions = new HashMap<>();

            // Set common capabilities
            bstackOptions.put("userName", BROWSERSTACK_USERNAME);
            bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
            bstackOptions.put("os", os);
            bstackOptions.put("osVersion", osVersion);
            bstackOptions.put("browserVersion", "latest");
            bstackOptions.put("projectName", "El Pais Test");
            bstackOptions.put("buildName", "Build 1.0");
            bstackOptions.put("sessionName", browser + " on " + os + " " + osVersion);
            bstackOptions.put("local", "false");
            bstackOptions.put("debug", true);

            capabilities.setCapability("bstack:options", bstackOptions);
            capabilities.setCapability("browserName", browser);

            // Create driver
            driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);
            String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
            logger.info("BrowserStack session started with ID: {}", sessionId);

            // Initialize components
            scraper = new ElPaisOpinionScraper(driver);
            translator = new TranslationService();

        } catch (Exception e) {
            logger.error("Failed to initialize BrowserStack session: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testArticleScraping() {
        try {
            logger.info("Starting article scraping test");
            scraper.navigateToOpinionSection();
            List<Article> articles = scraper.scrapeArticles(5);

            logger.info("Successfully scraped {} articles", articles.size());
            articles.forEach(article -> {
                logger.info("Article: {}", article.getHeadline());
            });

        } catch (Exception e) {
            logger.error("Article scraping test failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testTranslation() {
        try {
            logger.info("Starting translation test");
            scraper.navigateToOpinionSection();
            List<Article> articles = scraper.scrapeArticles(2);

            articles.forEach(article -> {
                try {
                    String translatedTitle = translator.translateToEnglish(article.getHeadline());
                    article.setTranslatedHeadline(translatedTitle);
                    logger.info("Original: {} -> Translated: {}",
                            article.getHeadline(), translatedTitle);
                } catch (Exception e) {
                    logger.error("Translation failed for article: {}",
                            article.getHeadline(), e);
                }
            });

        } catch (Exception e) {
            logger.error("Translation test failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                logger.info("Closing BrowserStack session");
                driver.quit();
                logger.info("BrowserStack session closed");
            } catch (Exception e) {
                logger.error("Error closing BrowserStack session: {}", e.getMessage());
            }
        }
    }
}