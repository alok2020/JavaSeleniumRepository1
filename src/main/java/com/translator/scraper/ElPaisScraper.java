package com.translator.scraper;

import com.translator.model.Article;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ElPaisScraper {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisScraper.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ElPaisScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public List<Article> scrapeArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            logger.info("Navigating to El País homepage...");
            driver.get("https://elpais.com/opinion/");

            // Wait for cookie banner and accept if present
            handleCookieBanner();

            // Wait for articles to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article")));

            // Find all article elements
            List<WebElement> articleElements = driver.findElements(By.cssSelector("article"));
            logger.info("Found {} article elements", articleElements.size());

            for (WebElement articleElement : articleElements) {
                try {
                    String headline = getHeadline(articleElement);
                    String summary = getSummary(articleElement);
                    String imageUrl = getImageUrl(articleElement);

                    if (!headline.isEmpty()) {
                        Article article = new Article(headline, summary, imageUrl);
                        articles.add(article);
                        logger.debug("Scraped article: {}", headline);
                    }
                } catch (Exception e) {
                    logger.warn("Error scraping individual article: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error scraping articles: {}", e.getMessage(), e);
        }

        logger.info("Scraped {} articles", articles.size());
        return articles;
    }

    private void handleCookieBanner() {
        try {
            // Wait for cookie banner and accept button
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.didomi-components-button--primary")));
            acceptButton.click();
            logger.info("Accepted cookies");
        } catch (TimeoutException e) {
            logger.info("No cookie banner found or already accepted");
        }
    }

    private String getHeadline(WebElement articleElement) {
        try {
            // Try different possible selectors for headlines
            String[] headlineSelectors = {
                    "h2",
                    ".article_header h2",
                    ".headline a",
                    "header h2"
            };

            for (String selector : headlineSelectors) {
                try {
                    WebElement headlineElement = articleElement.findElement(By.cssSelector(selector));
                    String headline = headlineElement.getText().trim();
                    if (!headline.isEmpty()) {
                        return headline;
                    }
                } catch (NoSuchElementException ignored) {}
            }
        } catch (Exception e) {
            logger.warn("Error getting headline: {}", e.getMessage());
        }
        return "";
    }

    private String getSummary(WebElement articleElement) {
        try {
            // Try different possible selectors for summaries
            String[] summarySelectors = {
                    ".article_lead",
                    ".summary",
                    "p.summary",
                    ".article_body p:first-child"
            };

            for (String selector : summarySelectors) {
                try {
                    WebElement summaryElement = articleElement.findElement(By.cssSelector(selector));
                    String summary = summaryElement.getText().trim();
                    if (!summary.isEmpty()) {
                        return summary;
                    }
                } catch (NoSuchElementException ignored) {}
            }
        } catch (Exception e) {
            logger.warn("Error getting summary: {}", e.getMessage());
        }
        return "";
    }

    private String getImageUrl(WebElement articleElement) {
        try {
            // Try different possible selectors for images
            String[] imageSelectors = {
                    "img",
                    ".article_image img",
                    "figure img"
            };

            for (String selector : imageSelectors) {
                try {
                    WebElement imageElement = articleElement.findElement(By.cssSelector(selector));
                    String imageUrl = imageElement.getAttribute("src");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        return imageUrl;
                    }
                } catch (NoSuchElementException ignored) {}
            }
        } catch (Exception e) {
            logger.warn("Error getting image URL: {}", e.getMessage());
        }
        return "";
    }
}