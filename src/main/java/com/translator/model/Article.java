package com.translator.model;

public class Article {
    private final String headline;  // Changed from title
    private final String summary;   // Changed from content
    private final String imageUrl;
    private String translatedHeadline;  // Changed from translatedTitle
    private String translatedSummary;   // Changed from translatedContent

    public Article(String headline, String summary, String imageUrl) {
        this.headline = headline;
        this.summary = summary;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getHeadline() { return headline; }
    public String getSummary() { return summary; }
    public String getImageUrl() { return imageUrl; }
    public String getTranslatedHeadline() { return translatedHeadline; }
    public String getTranslatedSummary() { return translatedSummary; }

    // Setters
    public void setTranslatedHeadline(String translatedHeadline) {
        this.translatedHeadline = translatedHeadline;
    }

    public void setTranslatedSummary(String translatedSummary) {
        this.translatedSummary = translatedSummary;
    }

    @Override
    public String toString() {
        return String.format("""
            Headline (Spanish): %s
            Headline (English): %s
            Summary (Spanish): %s
            Summary (English): %s
            Image URL: %s
            """,
                headline, translatedHeadline, summary, translatedSummary, imageUrl);
    }
}
