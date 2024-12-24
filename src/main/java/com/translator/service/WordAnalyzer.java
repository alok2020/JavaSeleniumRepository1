package com.translator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WordAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(WordAnalyzer.class);

    public Map<String, Integer> findRepeatedWords(String[] texts) {
        Map<String, Integer> wordCount = new HashMap<>();

        Arrays.stream(texts)
                // Clean up the text before analysis
                .map(text -> text.replace("[Translation Error]", "")) // Remove error messages
                .map(text -> text.replaceAll("\\[|\\]", "")) // Remove square brackets
                .map(text -> text.replaceAll("&#39;", "'")) // Replace HTML entities
                .map(String::toLowerCase)
                .flatMap(text -> Arrays.stream(text.split("\\s+")))
                .filter(word -> word.length() > 3) // Ignore small words
                .filter(word -> word.matches("[a-z]+")) // Only keep pure words, no numbers or special chars
                .filter(word -> !word.isEmpty()) // Skip empty strings
                .forEach(word -> wordCount.merge(word, 1, Integer::sum));

        return wordCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // Keep first value in case of duplicates
                        HashMap::new    // Use HashMap as the result container
                ));
    }

    public void printWordAnalysis(Map<String, Integer> repeatedWords) {
        if (repeatedWords.isEmpty()) {
            logger.info("No words were repeated more than twice.");
            return;
        }

        logger.info("\nWord frequency analysis (words appearing more than twice):");
        repeatedWords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        logger.info("'{}' appears {} times", entry.getKey(), entry.getValue()));
    }
}