package com.translator;

import com.translator.model.Article;
import com.translator.scraper.ElPaisScraper;
import com.translator.service.TranslationService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;

public class ElPaisTranslationTest {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisTranslationTest.class);
    private WebDriver driver;
    private ElPaisScraper scraper;
    private TranslationService translator;

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver(); // Use WebDriverConfig if you have one
        scraper = new ElPaisScraper(driver);
        translator = new TranslationService();
    }

    @Test(priority = 1)
    public void testArticleScraping() {
        List<Article> articles = scraper.scrapeArticles();
        Assert.assertFalse(articles.isEmpty(), "No articles were scraped");

        // Verify first article has required fields
        Article firstArticle = articles.get(0);
        Assert.assertNotNull(firstArticle.getHeadline(), "Headline is null");
        Assert.assertNotNull(firstArticle.getSummary(), "Summary is null");

        logger.info("Successfully scraped {} articles", articles.size());
        logger.info("Sample article: {}", firstArticle);
    }

    @Test(priority = 2)
    public void testTranslation() {
        String testText = "Hola mundo";
        String translated = translator.translateToEnglish(testText);
        Assert.assertNotNull(translated, "Translation returned null");
        Assert.assertNotEquals(translated, testText, "Text was not translated");
        logger.info("Translation test successful: '{}' -> '{}'", testText, translated);
    }

    @Test(priority = 3)
    public void testCompleteWorkflow() {
        List<Article> articles = scraper.scrapeArticles();
        Assert.assertFalse(articles.isEmpty(), "No articles were scraped");

        // Test translation of first article
        Article article = articles.get(0);
        article.setTranslatedHeadline(translator.translateToEnglish(article.getHeadline()));
        article.setTranslatedSummary(translator.translateToEnglish(article.getSummary()));

        Assert.assertNotNull(article.getTranslatedHeadline(), "Translated headline is null");
        Assert.assertNotNull(article.getTranslatedSummary(), "Translated summary is null");

        logger.info("Complete workflow test successful");
        logger.info("Article details: {}", article);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}