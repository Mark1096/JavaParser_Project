package org.parser.error;

/**
 * <h1> ErrorException </h1>
 *
 * This class extends the Exception class, providing in the constructor an error message to handle.
 */
public class ErrorException extends Exception {

    public ErrorException(String message) {
        super(message);
    }

}
