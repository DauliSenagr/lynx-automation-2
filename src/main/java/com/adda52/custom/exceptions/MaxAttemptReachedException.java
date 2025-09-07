package com.adda52.custom.exceptions;

/**
 * @author Dauli Sengar
 * @since 24th Jan 2023
 * Custom exception indicating that the maximum attempt limit has been reached.
 * Extends the Exception class to handle situations where the maximum attempts are exceeded.
 */
public class MaxAttemptReachedException extends Exception {

    // Default error message for the exception
    private static final String DEFAULT_ERROR_MESSAGE = "Maximum attempt limit reached.";

    /**
     * Constructs a MaxAttemptReachedException with a default error message.
     */
    public MaxAttemptReachedException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Constructs a MaxAttemptReachedException with the specified error message.
     *
     * @param error The error message describing the exception
     */
    public MaxAttemptReachedException(String error) {
        super(error);
    }

    /**
     * Constructs a MaxAttemptReachedException with a default error message and a cause.
     *
     * @param cause The cause of the exception
     */
    public MaxAttemptReachedException(Throwable cause) {
        super(DEFAULT_ERROR_MESSAGE, cause);
    }

    // Other constructors or methods as required
}

