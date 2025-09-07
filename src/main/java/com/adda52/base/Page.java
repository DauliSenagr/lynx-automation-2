package com.adda52.base;

import com.adda52.controllers.ExecutionController;
import com.adda52.driver.DriverManager;
import com.adda52.logging.Logging;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Base Page class providing common functionalities and interactions with web elements.
 * All page classes should extend this class to inherit its methods.
 */

public abstract class Page implements Logging {
    /**
     * Initializes the PageFactory for the current driver instance.
     */
    public Page() {
        PageFactory.initElements(DriverManager.getDriver(), this);
    }

    /**
     * Attempts to click on a web element with retry functionality.
     * Retries a specified number of times in case of exceptions.
     *
     * @param element                      The web element to click
     * @param elementName                  The name/description of the element
     * @param retryCount                   The number of attempts to retry the click action
     * @param pauseBetweenRetriesInSeconds The duration to pause between retry attempts (in seconds)
     */
    public void click(WebElement element, String elementName, int retryCount, int pauseBetweenRetriesInSeconds) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                element.click();
                return; // Exit the loop if click is successful
            } catch (StaleElementReferenceException | ElementNotInteractableException | NoSuchElementException e) {
                if (attempts == retryCount - 1) {
                    // If it's the last attempt, handle the exception
                    handleException("click", elementName, e);
                } else {
                    // If not the last attempt, wait and retry
                    attempts++;
                    getLogger().warn("Exception occurred while trying to click: " + elementName + ". Retrying attempt " + (attempts + 1));
                    ExecutionController.pauseExecution(pauseBetweenRetriesInSeconds);
                }
            } catch (Exception e) {
                // Handle other exceptions as before
                handleException("click", elementName, e);
            }
        }
    }

    /**
     * Attempts to click on a web element .
     *
     * @param element     The web element to click
     * @param elementName The name/description of the element
     */
    public void click(WebElement element, String elementName) {
        try {
            element.click();
        } catch (Exception e) {
            handleException("click", elementName, e);
        }
    }

    /**
     * Handles exceptions encountered during actions performed on web elements.
     * Logs the error message and throws a RuntimeException.
     *
     * @param action      The action attempted on the element
     * @param elementName The name/description of the element
     * @param e           The exception encountered
     */
    private void handleException(String action, String elementName, Exception e) {
        String errorMessage = "Unable to " + action + " on element: " + elementName;
        getLogger().error(errorMessage, e);
        throw new RuntimeException(errorMessage, e);
    }

    /**
     * Enters text into a web element.
     * Handles exceptions encountered during the 'sendKeys' action.
     *
     * @param element     The web element to send keys to
     * @param keys        The keys/text to be sent
     * @param elementName The name/description of the element
     */
    public void sendKeys(WebElement element, String keys, String elementName) {
        try {
            element.sendKeys(keys);
        } catch (Exception e) {
            handleException("send keys", elementName, e);
        }
    }

    /**
     * Retrieves a WebDriverWait instance based on the specified duration.
     *
     * @param secs Duration in seconds for the WebDriverWait
     * @return WebDriverWait instance
     */
    private WebDriverWait getWebDriverWait(int secs) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(secs));
    }

    /**
     * Waits for the specified web element to become visible within the given time frame.
     * Throws a TimeoutException if the element does not appear within the specified duration.
     *
     * @param element     The web element to wait for visibility
     * @param secs        The duration to wait for the element's visibility (in seconds)
     * @param elementName The name/description of the element
     * @throws TimeoutException If the element is not visible within the specified time
     */
    public void waitForWebElement(WebElement element, int secs, String elementName) throws TimeoutException {
        try {
            getWebDriverWait(secs).until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            String errorMessage = elementName + " did not appear. Waited for " + secs + " secs";
            getLogger().error(errorMessage, e);
            throw new TimeoutException(errorMessage, e);
        }
    }

    /**
     * Waits for the specified web element to become clickable within the given time frame.
     * Throws a TimeoutException if the element does not become clickable within the specified duration.
     *
     * @param element     The web element to wait for visibility
     * @param secs        The duration to wait for the element's visibility (in seconds)
     * @param elementName The name/description of the element
     * @throws TimeoutException If the element is not visible within the specified time
     */
    public void waitForWebElementToBeClickable(WebElement element, int secs, String elementName) throws TimeoutException {
        try {
            getWebDriverWait(secs).until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            String errorMessage = elementName + "is not clickable . Waited for " + secs + " secs";
            getLogger().error(errorMessage, e);
            throw new TimeoutException(errorMessage, e);
        }
    }

    /**
     * Waits for the specified web element to disappear within the given time frame.
     * Throws a RuntimeException if the element does not disappear within the specified duration.
     *
     * @param element     The web element to wait for invisibility
     * @param secs        The duration to wait for the element's disappearance (in seconds)
     * @param elementName The name/description of the element
     */
    public void waitForWebElementToDisappear(WebElement element, int secs, String elementName) {
        try {
            getWebDriverWait(secs).until(ExpectedConditions.invisibilityOf(element));
        } catch (Exception e) {
            String errorMessage = elementName + " did not disappear. Waited for " + secs + " secs";
            getLogger().error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Waits for a frame with the specified identifier to be available and switches to it within the given time frame.
     * Throws a RuntimeException if the frame does not appear within the specified duration.
     *
     * @param frameId The identifier of the frame to wait for and switch to
     * @param secs    The duration to wait for the frame's availability (in seconds)
     */
    public void waitForFrameToAppearAndSwitchToIt(String frameId, int secs) {
        try {
            getWebDriverWait(secs).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameId));
        } catch (Exception e) {
            String errorMessage = frameId + " did not appear. Waited for " + secs + " secs";
            getLogger().error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Retrieves the visible text of the specified web element.
     *
     * @param element     The web element to retrieve text from
     * @param elementName The name/description of the element
     * @return The visible text of the element, or null if an exception occurs
     */
    public String getText(WebElement element, String elementName) {
        try {
            return element.getText();
        } catch (Exception e) {
            handleException("get text", elementName, e);
            return null;
        }
    }

    /**
     * Checks whether the specified web element is displayed.
     *
     * @param element     The web element to check for display
     * @param elementName The name/description of the element
     * @return True if the element is displayed, otherwise false
     */
    public boolean isElementDisplayed(WebElement element, String elementName) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            getLogger().info(elementName + " is not displayed");
            return false;
        }
    }

    /**
     * Scrolls to the specified web element using JavaScriptExecutor with retry functionality.
     * Retries a specified number of times in case of exceptions while scrolling.
     *
     * @param element                      The web element to scroll to
     * @param elementName                  The name/description of the element
     * @param retryCount                   The number of attempts to retry the scroll action
     * @param pauseBetweenRetriesInSeconds The duration to pause between retry attempts (in seconds)
     */
    public void scrollToElementUsingJavaScriptExecutor(WebElement element, String elementName, int retryCount, int pauseBetweenRetriesInSeconds) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
                return; // Exit the loop if scroll is successful
            } catch (StaleElementReferenceException | ElementNotInteractableException | NoSuchElementException e) {
                if (attempts == retryCount - 1) {
                    // If it's the last attempt, handle the exception
                    handleException("scroll", elementName, e);
                } else {
                    // If not the last attempt, wait and retry
                    attempts++;
                    getLogger().warn("Exception occurred while trying to scroll to: " + elementName + ". Retrying attempt " + (attempts + 1));
                    ExecutionController.pauseExecution(pauseBetweenRetriesInSeconds);
                }
            } catch (Exception e) {
                // Handle other exceptions as before
                handleException("scroll", elementName, e);
            }
        }
    }

    /**
     * Hover over the specified web element.
     *
     * @param element the WebElement to hover over.
     */
    public void hoverOverElement(WebElement element) {
        Actions actions = new Actions(DriverManager.getDriver());
        actions.moveToElement(element).perform();
    }

    /**
     * Click on the specified web element using JavaScript Executor.
     *
     * @param element the WebElement to click on.
     */
    public void clickOnElementUsingJavaScriptExecutor(WebElement element) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", element);
    }

    /**
     * Enter a string into the specified web element using JavaScript Executor.
     *
     * @param element the WebElement to enter the string into.
     * @param text    the string to enter.
     */
    public void enterStringUsingJavaScriptExecutor(WebElement element, String text) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].value = arguments[1];", element, text);
    }

    /**
     * Get the content of the specified web element using JavaScript Executor.
     *
     * @param element the WebElement to get the content from.
     * @return the content of the web element as a String.
     */
    public String getContentUsingJavaScriptExecutor(WebElement element) {
        return ((JavascriptExecutor) DriverManager.getDriver())
                .executeScript("return window.getComputedStyle(arguments[0], ':after').getPropertyValue('content');", element).toString();
    }

    public String getElementAttributeValue(WebElement element, String attribute, String elementName) {
        try {
            return element.getAttribute(attribute);

        } catch (Exception e) {
            handleException("get attribute", elementName, e);
            return "";
        }
    }

    public String getText(WebElement element, String elementName, int retryCount, int pauseBetweenRetriesInSeconds) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                return element.getText();
            } catch (StaleElementReferenceException | ElementNotInteractableException | NoSuchElementException e) {
                if (attempts == retryCount - 1) {
                    handleException("get text", elementName, e);
                } else {
                    attempts++;
                    getLogger().warn("Exception occurred while trying to get text: " + elementName + ". Retrying attempt " + (attempts + 1));
                    ExecutionController.pauseExecution(pauseBetweenRetriesInSeconds);
                }
            } catch (Exception e) {
                handleException("get text", elementName, e);
            }
        }
        return null;
    }


    public static boolean isElementAppears( String cssSelector) {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        String script = "return document.querySelector('" + cssSelector + "') !== null && window.getComputedStyle(document.querySelector('" + cssSelector + "')).display !== 'none' && window.getComputedStyle(document.querySelector('" + cssSelector + "')).visibility !== 'hidden';";
        return (Boolean) js.executeScript(script); }

    }


