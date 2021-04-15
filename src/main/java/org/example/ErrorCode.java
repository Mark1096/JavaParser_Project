package org.example;

import lombok.Getter;

@Getter
public enum ErrorCode {
    MALFORMED_FILENAME_APPLICATION("Malformed filename class application"),
    METHODLESS_CLASS_APPLICATION("Methodless class application"),
    METHODLESS_CLASS_USER("Methodless class user"),
    BAD_WRITING_FILE("Error in writing file"),
    TROUBLE_PARSING_FILE("Trouble parsing file"),
    ABSENCE_BODY_METHOD("Absence body method");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public static ErrorException generateErrorException(ErrorCode errorCode) {
        return new ErrorException(errorCode.getMessage());
    }
}
