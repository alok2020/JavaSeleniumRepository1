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

public class ElPaisTranslationTest {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisTranslationTest.class);
    private WebDriver driver;
    private ElPaisOpinionScraper scraper;
    private TranslationService translator;

    // Force BrowserStack configuration
    private static final String BROWSERSTACK_USERNAME = "ak_UQ2Gsq";
    private static final String BROWSERSTACK_ACCESS_KEY = "eDHsRmFvWZvPhcDqpxVv";
    private static final String BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    @BeforeMethod
    @Parameters({"browser", "device", "os"})
    public void setup(String browser, String device, String os) {
        try {
            logger.info("Initializing BrowserStack session with browser: {}, device: {}, os: {}", browser, device, os);

            DesiredCapabilities capabilities = new DesiredCapabilities();
            HashMap<String, Object> bstackOptions = new HashMap<>();

            // Set BrowserStack specific capabilities
            bstackOptions.put("userName", BROWSERSTACK_USERNAME);
            bstackOptions.put("accessKey", BROWSERSTACK_ACCESS_KEY);
            bstackOptions.put("browsername", browser);
            bstackOptions.put("os", device);
            bstackOptions.put("os_version", os);
            bstackOptions.put("projectName", "El Pais Test");
            bstackOptions.put("buildName", "Build 1.0");
            bstackOptions.put("sessionName", browser + " Test");
            bstackOptions.put("debug", "true");
            bstackOptions.put("networkLogs", "true");

            capabilities.setCapability("bstack:options", bstackOptions);

            logger.info("Connecting to BrowserStack with URL: {}", BROWSERSTACK_URL);
            logger.info("Capabilities: {}", capabilities);

            // Create RemoteWebDriver instance
            driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);

            // Initialize other components
            scraper = new ElPaisOpinionScraper(driver);
            translator = new TranslationService();

            logger.info("BrowserStack session created successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize BrowserStack session: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testArticleScraping() {
        try {
            logger.info("Starting article scraping test on BrowserStack");
            String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
            logger.info("BrowserStack Session ID: {}", sessionId);

            scraper.navigateToOpinionSection();
            List<Article> articles = scraper.scrapeArticles(5);

            articles.forEach(article -> {
                logger.info("Scraped Article: {}", article.getHeadline());
            });

            logger.info("Article scraping test completed successfully");
        } catch (Exception e) {
            logger.error("Article scraping test failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testTranslation() {
        try {
            logger.info("Starting translation test on BrowserStack");
            String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
            logger.info("BrowserStack Session ID: {}", sessionId);

            scraper.navigateToOpinionSection();
            List<Article> articles = scraper.scrapeArticles(2);

            articles.forEach(article -> {
                try {
                    String translatedTitle = translator.translateToEnglish(article.getHeadline());
                    logger.info("Original: {} -> Translated: {}", article.getHeadline(), translatedTitle);
                } catch (Exception e) {
                    logger.error("Translation failed for article: " + article.getHeadline(), e);
                }
            });

            logger.info("Translation test completed successfully");
        } catch (Exception e) {
            logger.error("Translation test failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                logger.info("Closing BrowserStack session");
                driver.quit();
                logger.info("BrowserStack session closed successfully");
            } catch (Exception e) {
                logger.error("Error closing BrowserStack session: " + e.getMessage());
            }
        }
    }
}