package com.translator.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);
    private final Path imageDir;

    public ImageDownloader() {
        // Get the project root directory
        String userDir = System.getProperty("user.dir");
        this.imageDir = Paths.get(userDir, "downloaded_images");
        createImageDirectory();
    }

    private void createImageDirectory() {
        try {
            if (!Files.exists(imageDir)) {
                Files.createDirectory(imageDir);
                logger.info("Created image directory at: {}", imageDir.toAbsolutePath());
            } else {
                logger.info("Using existing image directory at: {}", imageDir.toAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error creating image directory: {}", e.getMessage());
        }
    }

    public void downloadImage(String imageUrl, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            logger.warn("Skipping download - Empty image URL for {}", fileName);
            return;
        }

        try {
            URL url = new URL(imageUrl);
            Path destination = imageDir.resolve(sanitizeFileName(fileName) + ".jpg");
            FileUtils.copyURLToFile(url, destination.toFile());
            logger.info("Downloaded image to: {}", destination.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Error downloading image {} from {}: {}", fileName, imageUrl, e.getMessage());
        }
    }

    private String sanitizeFileName(String fileName) {
        // Remove invalid characters from filename
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}