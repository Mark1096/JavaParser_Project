package org.parser.error;

import lombok.Getter;

/**
 * <h1> ErrorCode </h1>
 * <p>
 * It is used to handle any exceptions that may occur during program execution.
 */
@Getter
public enum ErrorCode {
    /**
     * The Malformed filename application.
     */
    MALFORMED_FILENAME_APPLICATION("Malformed filename class application"),
    /**
     * The Methodless class application.
     */
    METHODLESS_CLASS_APPLICATION("Methodless class application"),
    /**
     * The Methodless class user.
     */
    METHODLESS_CLASS_USER("Methodless class user"),
    /**
     * The Bad writing file.
     */
    BAD_WRITING_FILE("Error in writing file"),
    /**
     * The Trouble parsing file.
     */
    TROUBLE_PARSING_FILE("Trouble parsing file"),
    /**
     * The Absence body method.
     */
    ABSENCE_BODY_METHOD("Absence body method");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }

    /**
     * Generate a new exception with the appropriate error message.
     *
     * @param errorCode the error code
     * @return error exception
     */
    public static ErrorException generateErrorException(ErrorCode errorCode) {
        return new ErrorException(errorCode.getMessage());
    }
}
