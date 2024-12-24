package com.translator;

import com.translator.config.WebDriverConfig;
import com.translator.model.Article;
import com.translator.scraper.ElPaisScraper;
import com.translator.service.ImageDownloader;
import com.translator.service.TranslationService;
import com.translator.service.WordAnalyzer;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElPaisTranslatorApp {
    private static final Logger logger = LoggerFactory.getLogger(ElPaisTranslatorApp.class);

    public static void main(String[] args) {
        WebDriver driver = null;
        try {
            // Initialize WebDriver and services
            driver = WebDriverConfig.createDriver();
            ElPaisScraper scraper = new ElPaisScraper(driver);
            TranslationService translator = new TranslationService();
            ImageDownloader imageDownloader = new ImageDownloader();
            WordAnalyzer wordAnalyzer = new WordAnalyzer();

            // For collecting translated headlines
            List<String> translatedHeadlines = new ArrayList<>();

            // Scrape articles
            List<Article> articles = scraper.scrapeArticles();
            logger.info("Translating {} articles...", articles.size());

            // Process articles
            for (Article article : articles) {
                try {
                    // Translate headline
                    String translatedHeadline = translator.translateToEnglish(article.getHeadline());
                    article.setTranslatedHeadline(translatedHeadline);

                    // Only add valid translations to analysis
                    if (translatedHeadline != null && !translatedHeadline.contains("[Translation Error]")) {
                        translatedHeadlines.add(translatedHeadline);
                    }

                    // Translate summary if present
                    if (!article.getSummary().isEmpty()) {
                        String translatedSummary = translator.translateToEnglish(article.getSummary());
                        article.setTranslatedSummary(translatedSummary);
                    }

                    // Download image if URL is present
                    if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                        String fileName = article.getHeadline()
                                .replaceAll("[^a-zA-Z0-9]", "_")
                                .substring(0, Math.min(50, article.getHeadline().length()));
                        imageDownloader.downloadImage(article.getImageUrl(), fileName);
                    }

                    // Print article details
                    logger.info("\n=== Article ===\n{}", article);
                } catch (Exception e) {
                    logger.error("Error processing article: {}", e.getMessage());
                }
            }

            // After processing all articles, only analyze if we have valid translations
            if (!translatedHeadlines.isEmpty()) {
                logger.info("\n=== Word Frequency Analysis ===");
                Map<String, Integer> repeatedWords = wordAnalyzer.findRepeatedWords(
                        translatedHeadlines.toArray(new String[0])
                );
                wordAnalyzer.printWordAnalysis(repeatedWords);
            } else {
                logger.warn("No valid translations to analyze.");
            }

        } catch (Exception e) {
            logger.error("Application error: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}