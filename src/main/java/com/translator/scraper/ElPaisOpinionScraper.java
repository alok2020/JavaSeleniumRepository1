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

public class ElPaisOpinionScraper {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisOpinionScraper.class);
    private static final String OPINION_URL = "https://elpais.com/opinion/";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ElPaisOpinionScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void navigateToOpinionSection() {
        logger.info("Navigating to Opinion section...");
        driver.get(OPINION_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    public List<Article> scrapeArticles(int limit) {
        logger.info("Scraping {} articles from Opinion section...", limit);
        List<Article> articles = new ArrayList<>();

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("article.c_a")));

            List<WebElement> articleElements = driver.findElements(
                    By.cssSelector("article.c_a"));

            for (int i = 0; i < Math.min(limit, articleElements.size()); i++) {
                WebElement articleElement = articleElements.get(i);
                try {
                    String title = getTitle(articleElement);
                    String content = getContent(articleElement);
                    String imageUrl = getImageUrl(articleElement);

                    if (!title.isEmpty()) {
                        articles.add(new Article(title, content, imageUrl));
                    }
                } catch (Exception e) {
                    logger.warn("Error scraping article: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error scraping articles: {}", e.getMessage());
        }

        logger.info("Scraped {} articles", articles.size());
        return articles;
    }

    private String getTitle(WebElement article) {
        try {
            WebElement titleElement = article.findElement(By.cssSelector("h2"));
            return titleElement.getText().trim();
        } catch (Exception e) {
            logger.warn("Could not find title: {}", e.getMessage());
            return "";
        }
    }

    private String getContent(WebElement article) {
        try {
            WebElement contentElement = article.findElement(
                    By.cssSelector("p.c_d"));
            return contentElement.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getImageUrl(WebElement article) {
        try {
            WebElement imageElement = article.findElement(
                    By.cssSelector("img"));
            return imageElement.getAttribute("src");
        } catch (Exception e) {
            return "";
        }
    }
}