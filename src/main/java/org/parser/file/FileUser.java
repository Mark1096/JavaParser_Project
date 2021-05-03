package org.parser.file;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;
import java.io.File;
import static org.parser.error.ErrorCode.generateErrorException;

/**
 * <h1> FileUser </h1>
 * <p>
 * This class deals with user-supplied files.
 */
public class FileUser {
    private CompilationUnit cu;
    private File file;

    /**
     * Instantiates a new File user.
     *
     * @param file the file
     * @throws ErrorException the error exception
     */
    public FileUser(File file) throws ErrorException {
        this.file = file;
        try {
            cu = StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.TROUBLE_PARSING_FILE);
        }
    }

    /**
     * Returns the CompilationUnit instance of the user file.
     *
     * @return user compilation unit
     */
    public CompilationUnit getUserCompilationUnit() {
        return cu;
    }

    /**
     * Returns the instance of File.
     *
     * @return file
     */
    public File getFile() {
        return file;
    }
}
