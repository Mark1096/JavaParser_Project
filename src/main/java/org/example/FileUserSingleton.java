package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.nio.file.Paths;

import static org.example.ErrorCode.generateErrorException;

public class FileUserSingleton {
    // TODO : Verificare ottimizzazzione passaggio path del file utente
    public static final String userDirectory = Paths.get("").toAbsolutePath().toString().split("target")[0];
    public static final String file_path = userDirectory + "src/main/java/org/example/TestClass.java";
    private static CompilationUnit cu;
    private static final File file = new File(file_path);

    private FileUserSingleton() {
    }

    public static CompilationUnit getInstance() throws ErrorException {
        if (cu == null) {
            try {
                cu = StaticJavaParser.parse(file);
            } catch (Exception e) {
                throw generateErrorException(ErrorCode.FILE_NOT_FOUND);
            }
        }
        return cu;
    }

    public static File getFile() {
        return file;
    }
}
