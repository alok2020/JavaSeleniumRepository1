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
    private final JavascriptExecutor js;

    public ElPaisOpinionScraper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.js = (JavascriptExecutor) driver;
    }

    public void navigateToOpinionSection() {
        logger.info("Navigating to Opinion section...");
        driver.get(OPINION_URL);

        // Wait for page load
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));

        handleCookieConsent();
    }

    private void handleCookieConsent() {
        try {
            // Wait longer for dynamic content to load
            Thread.sleep(3000);

            // More specific selectors based on the actual DOM structure
            String[] cookieSelectors = {
                    "#didomi-notice-agree-button",                    // Most specific ID
                    "[aria-label='Aceptar y cerrar']",               // Using aria-label
                    "[aria-label='Consent']",                        // Alternative aria-label
                    "button[class*='didomi-consent-button']",        // Partial class match
                    "button.didomi-components-button--primary",      // Original selector
                    "#didomi-host button"                            // Generic didomi button
            };

            // First try to wait for the didomi frame/container to be present
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#didomi-host, .didomi-notice-container")));


            // Try each selector in the main document
            for (String selector : cookieSelectors) {
                try {
                    By buttonSelector = By.cssSelector(selector);
                    WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(buttonSelector));

                    // Try regular click first
                    try {
                        acceptButton.click();
                        logger.info("Accepted cookies using regular click with selector: {}", selector);
                        return;
                    } catch (ElementClickInterceptedException e) {
                        // If regular click fails, try JavaScript click
                        js.executeScript("arguments[0].click();", acceptButton);
                        logger.info("Accepted cookies using JavaScript click with selector: {}", selector);
                        return;
                    }
                } catch (TimeoutException ignored) {
                    continue;
                }
            }


            // Now try each button selector
            for (String selector : cookieSelectors) {
                try {
                    // Use a shorter wait for each individual selector
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                    WebElement acceptButton = shortWait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector(selector)
                    ));

                    // Try different click methods
                    try {
                        // Try regular click first
                        acceptButton.click();
                        logger.info("Accepted cookies using regular click with selector: {}", selector);
                        return;
                    } catch (ElementClickInterceptedException e1) {
                        try {
                            // Try JavaScript click if regular click fails
                            js.executeScript("arguments[0].click();", acceptButton);
                            logger.info("Accepted cookies using JavaScript click with selector: {}", selector);
                            return;
                        } catch (Exception e2) {
                            // Try Actions click if both previous methods fail
                            new org.openqa.selenium.interactions.Actions(driver)
                                    .moveToElement(acceptButton)
                                    .click()
                                    .perform();
                            logger.info("Accepted cookies using Actions click with selector: {}", selector);
                            return;
                        }
                    }
                } catch (TimeoutException e) {
                    continue;
                }
            }
// If we get here, try to find and interact with any iframe
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                try {
                    driver.switchTo().frame(iframe);

                    for (String selector : cookieSelectors) {
                        try {
                            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                                    By.cssSelector(selector)
                            ));
                            js.executeScript("arguments[0].click();", acceptButton);
                            logger.info("Accepted cookies in iframe using selector: {}", selector);
                            driver.switchTo().defaultContent();
                            return;
                        } catch (TimeoutException e) {
                            continue;
                        }
                    }

                    driver.switchTo().defaultContent();
                } catch (Exception e) {
                    driver.switchTo().defaultContent();
                }
            }

            logger.info("No cookie banner found or already accepted");
        } catch (Exception e) {
            logger.warn("Error handling cookie consent: {}", e.getMessage());
            try {
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
        }
    }

    public List<Article> scrapeArticles(int limit) {
        logger.info("Scraping {} articles from Opinion section...", limit);
        List<Article> articles = new ArrayList<>();

        try {
            // First, wait for any container elements that hold the articles
            By[] containerSelectors = {
                    By.cssSelector("section[data-dtm-region='portada_apertura']"),
                    By.cssSelector(".b.b-d"),
                    By.cssSelector("section[class*='portada']"),
                    By.cssSelector("main")
            };

            WebElement container = null;
            for (By selector : containerSelectors) {
                try {
                    container = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
                    if (container != null) {
                        logger.info("Found article container using selector: {}", selector);
                        break;
                    }
                } catch (TimeoutException ignored) {}
            }

            if (container == null) {
                logger.error("Could not find article container");
                return articles;
            }

            // Article selectors updated based on actual HTML structure
            By[] articleSelectors = {
                    By.cssSelector("article.c"),
                    By.cssSelector(".c.c-o"),
                    By.cssSelector(".c.c-d"),
                    By.cssSelector("article[class*='c']"),
                    By.cssSelector("[class*='article']")
            };

            List<WebElement> articleElements = null;
            for (By selector : articleSelectors) {
                try {
                    articleElements = container.findElements(selector);
                    if (articleElements != null && !articleElements.isEmpty()) {
                        logger.info("Found {} articles using selector: {}", articleElements.size(), selector);
                        break;
                    }
                } catch (Exception e) {
                    logger.debug("Selector {} failed: {}", selector, e.getMessage());
                }
            }

            if (articleElements == null || articleElements.isEmpty()) {
                logger.error("Could not find any articles with available selectors");
                return articles;
            }

            // Process found articles
            for (int i = 0; i < Math.min(limit, articleElements.size()); i++) {
                WebElement articleElement = articleElements.get(i);
                try {
                    // Scroll element into view for better interaction
                    js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", articleElement);
                    Thread.sleep(500);

                    String title = getTitle(articleElement);
                    String content = getContent(articleElement);
                    String imageUrl = getImageUrl(articleElement);

                    if (!title.isEmpty()) {
                        articles.add(new Article(title, content, imageUrl));
                        logger.info("Added article: {}", title);
                    }
                } catch (Exception e) {
                    logger.warn("Error processing article: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error scraping articles: {}", e.getMessage());
        }

        logger.info("Successfully scraped {} articles", articles.size());
        return articles;
    }

    private String getTitle(WebElement article) {
        String[] titleSelectors = {
                "h2.c_t a",
                ".c_h h2 a",
                "h2[class*='c_t'] a",
                ".c_t a",
                "h2 a",
                "h2"
        };

        return findElementText(article, titleSelectors);
    }

    private String getContent(WebElement article) {
        String[] contentSelectors = {
                "p.c_d",
                ".c_d",
                "p[class*='c_d']",
                ".c_st",
                ".article_lead",
                ".summary",
                "p.summary"
        };

        return findElementText(article, contentSelectors);
    }

    private String getImageUrl(WebElement article) {
        String[] imageSelectors = {
                "img.c_m_e",
                ".c_m img",
                "img[class*='c_m']",
                "figure img",
                ".article_image img",
                "img[data-src]",
                "img[src]"
        };

        try {
            for (String selector : imageSelectors) {
                List<WebElement> elements = article.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    WebElement img = elements.get(0);

                    // Try different image attributes
                    String[] imageAttributes = {"src", "data-src", "srcset"};
                    for (String attr : imageAttributes) {
                        String url = img.getAttribute(attr);
                        if (url != null && !url.isEmpty()) {
                            return url;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting image URL: {}", e.getMessage());
        }
        return "";
    }

    private String findElementText(WebElement article, String[] selectors) {
        try {
            for (String selector : selectors) {
                List<WebElement> elements = article.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    String text = elements.get(0).getText().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error finding element text: {}", e.getMessage());
        }
        return "";
    }
}