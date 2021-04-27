package org.parser.file;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;
import java.io.File;
import static org.parser.error.ErrorCode.generateErrorException;

/**
 * <h1> FileUser </h1>
 *
 * This class deals with user-supplied files.
 */
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
