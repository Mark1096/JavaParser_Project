package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class Singleton {
    public static final String userDirectory = Paths.get("").toAbsolutePath().toString().split("target")[0];
    public static final String file_path = userDirectory + "src/main/java/org/example/TestClass.java";
    private static CompilationUnit cu;
    private static final File file = new File(file_path);

    private Singleton() {
    }

    public static CompilationUnit getInstance() throws FileNotFoundException {
        if (cu == null) {
            cu = StaticJavaParser.parse(file);
        }
        return cu;
    }

    // TODO: da rivedere implementazione e possibile creazione di un'altra classe con questo metodo
    public static File getFile() {
        return file;
    }
}
