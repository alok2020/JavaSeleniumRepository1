package com.translator;

import com.translator.model.Article;
import com.translator.scraper.ElPaisScraper;
import com.translator.service.TranslationService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.URL;
import java.util.List;

public class ElPaisTranslationTest {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisTranslationTest.class);
    private WebDriver driver;
    private static final String BROWSERSTACK_USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    private static final String BROWSERSTACK_ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private static final String BROWSERSTACK_URL = "https://hub.browserstack.com/wd/hub";

    @Parameters({"browser", "device", "os"})
    @BeforeMethod
    public void setUp(String browser, String device, String os) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // Set BrowserStack Credentials
        capabilities.setCapability("browserstack.user", BROWSERSTACK_USERNAME);
        capabilities.setCapability("browserstack.key", BROWSERSTACK_ACCESS_KEY);

        // Set test metadata
        capabilities.setCapability("project", "ElPais Translator");
        capabilities.setCapability("build", "Cross Browser Tests " + System.currentTimeMillis());
        capabilities.setCapability("name", browser + " on " + os);

        // Set platform specific capabilities
        if (device.equals("Windows") || device.equals("OS X")) {
            // Desktop configuration
            capabilities.setCapability("browser", browser);
            capabilities.setCapability("browser_version", "latest");
            capabilities.setCapability("os", device);
            capabilities.setCapability("os_version", os);
        } else {
            // Mobile configuration
            capabilities.setCapability("device", device);
            capabilities.setCapability("os_version", os);
            capabilities.setCapability("real_mobile", "true");
        }

        // Create RemoteWebDriver instance
        driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);
        logger.info("Started session on BrowserStack: {} on {}", browser, os);
    }

    @Test
    public void testElPaisTranslation() {
        try {
            // Initialize services
            ElPaisScraper scraper = new ElPaisScraper(driver);
            TranslationService translator = new TranslationService();

            // Test article scraping
            List<Article> articles = scraper.scrapeArticles();
            Assert.assertFalse(articles.isEmpty(), "No articles were scraped");
            logger.info("Successfully scraped {} articles", articles.size());

            // Test translations
            int translationCount = 0;
            for (Article article : articles.subList(0, Math.min(3, articles.size()))) {
                String translatedHeadline = translator.translateToEnglish(article.getHeadline());
                Assert.assertNotNull(translatedHeadline, "Translation failed for headline: " + article.getHeadline());
                Assert.assertFalse(translatedHeadline.isEmpty(), "Empty translation returned for: " + article.getHeadline());
                translationCount++;
            }
            logger.info("Successfully translated {} headlines", translationCount);

        } catch (Exception e) {
            logger.error("Test failed: {}", e.getMessage());
            Assert.fail("Test execution failed: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            logger.info("Closed BrowserStack session");
        }
    }
}