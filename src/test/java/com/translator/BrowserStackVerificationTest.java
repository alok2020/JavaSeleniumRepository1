package com.translator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;
import java.net.URL;
import java.util.HashMap;

public class BrowserStackVerificationTest {

    @Test
    public void verifyBrowserStackConnection() {
        WebDriver driver = null;
        try {
            System.out.println("Starting BrowserStack verification test...");

            String USERNAME = "ak_UQ2Gsq";
            String ACCESS_KEY = "eDHsRmFvWZvPhcDqpxVv";
            String BS_URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

            System.out.println("Setting up capabilities...");
            DesiredCapabilities caps = new DesiredCapabilities();
            HashMap<String, Object> bstackOptions = new HashMap<>();
            bstackOptions.put("browserName", "Chrome");
            bstackOptions.put("browserVersion", "latest");
            bstackOptions.put("os", "Windows");
            bstackOptions.put("osVersion", "10");
            bstackOptions.put("projectName", "First Test");
            bstackOptions.put("buildName", "browserstack-build-1");
            bstackOptions.put("sessionName", "Verification Test");
            bstackOptions.put("local", "false");
            bstackOptions.put("seleniumVersion", "4.15.0");

            caps.setCapability("bstack:options", bstackOptions);

            System.out.println("Connecting to BrowserStack with URL: " + BS_URL);
            System.out.println("Capabilities: " + caps.toString());

            driver = new RemoteWebDriver(new URL(BS_URL), caps);

            String sessionId = ((RemoteWebDriver) driver).getSessionId().toString();
            System.out.println("BrowserStack Session created with ID: " + sessionId);

            driver.get("https://www.google.com");
            System.out.println("Navigated to Google. Page title: " + driver.getTitle());

            Thread.sleep(5000); // Wait to see the page

        } catch (Exception e) {
            System.out.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                System.out.println("Closing BrowserStack session...");
                driver.quit();
                System.out.println("BrowserStack session closed.");
            }
        }
    }
}