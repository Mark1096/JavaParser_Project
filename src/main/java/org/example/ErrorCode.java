package org.example;

import lombok.Getter;

@Getter
public enum ErrorCode {
    MALFORMED_FILENAME_APPLICATION("Malformed filename class application"),
    METHODLESS_CLASS_APPLICATION("Methodless class application"),
    METHODLESS_CLASS_USER("Methodless class user"),
    BAD_WRITING_FILE("Error in writing file"),
    FILE_NOT_FOUND("File not found");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public static ErrorException generateErrorException(ErrorCode errorCode) {
        return new ErrorException(errorCode.getMessage());
    }
}
