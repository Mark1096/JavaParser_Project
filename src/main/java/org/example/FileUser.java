package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;

import static org.example.ErrorCode.generateErrorException;

public class FileUser {
    private CompilationUnit cu;
    private File file;

    public FileUser(File file) throws ErrorException {
        this.file = file;
        try {
            cu = StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.TROUBLE_PARSING_FILE);
        }
    }

    public CompilationUnit getCompilationUnit() {
        return cu;
    }

    public File getFile() {
        return file;
    }
}
