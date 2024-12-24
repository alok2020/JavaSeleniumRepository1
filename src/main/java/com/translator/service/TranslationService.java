package com.translator.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final Translate translate;

    public TranslationService() {
        logger.info("Initializing Translation Service...");
        try {
            // Explicitly check for credentials
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialsPath == null || credentialsPath.isEmpty()) {
                throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS environment variable not set");
            }

            // Initialize the translation service
            translate = TranslateOptions.getDefaultInstance().getService();
            logger.info("Translation Service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Translation Service: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Translation Service", e);
        }
    }

    public String translateToEnglish(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage("es"),
                    Translate.TranslateOption.targetLanguage("en")
            );
            return translation.getTranslatedText();
        } catch (Exception e) {
            logger.error("Translation error: {}", e.getMessage());
            return "[Translation Error]";
        }
    }
}