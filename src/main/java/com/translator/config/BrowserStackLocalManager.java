package com.translator.config;

import com.browserstack.local.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BrowserStackLocalManager {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackLocalManager.class);
    private static Local bsLocal;
    private static String localIdentifier;

    public static void startLocal(String accessKey) throws Exception {
        if (bsLocal == null) {
            bsLocal = new Local();
        }

        if (!bsLocal.isRunning()) {
            try {
                logger.info("Starting BrowserStack Local...");

                localIdentifier = "local_" + System.currentTimeMillis();

                Map<String, String> options = new HashMap<>();
                options.put("key", accessKey);
                options.put("localIdentifier", localIdentifier);
                options.put("forceLocal", "true");
                options.put("verbose", "3");
                options.put("logFile", "browserstack-local.log");

                bsLocal.start(options);

                logger.info("BrowserStack Local is running with identifier: {}", localIdentifier);
            } catch (Exception e) {
                logger.error("Failed to start BrowserStack Local: {}", e.getMessage());
                throw new RuntimeException("Could not start BrowserStack Local", e);
            }
        } else {
            logger.info("BrowserStack Local is already running");
        }
    }

    public static void stopLocal() throws Exception {
        if (bsLocal != null && bsLocal.isRunning()) {
            try {
                logger.info("Stopping BrowserStack Local...");
                bsLocal.stop();
                logger.info("BrowserStack Local stopped successfully");
            } catch (Exception e) {
                logger.error("Failed to stop BrowserStack Local: {}", e.getMessage());
            }
        }
    }

    public static String getLocalIdentifier() {
        return localIdentifier;
    }

    public static boolean isRunning() throws Exception {
        return bsLocal != null && bsLocal.isRunning();
    }
}