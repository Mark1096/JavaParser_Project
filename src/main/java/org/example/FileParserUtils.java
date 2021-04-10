package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.AnalysisMethod.getRecursiveMethodCall;

public class FileParserUtils {

    // Estrapolazione di tutti i metodi contenuti nella classe passata all'interno del file
    // e salvataggio di tutti e soli i metodi ricorsivi all'interno di una lista
    public static List<MethodDeclaration> getRecursiveUserMethodList() throws FileNotFoundException {
        CompilationUnit cu = Singleton.getInstance();  // Analisi e salvataggio del contenuto del file

        return CollectionUtils.emptyIfNull(cu.findAll(MethodDeclaration.class))
                .stream()
                .filter(element -> getRecursiveMethodCall(element) != null)
                .collect(Collectors.toList());
    }

    // TODO: Da rivedere
    public static List<MethodDeclaration> getAllUserMethodList() throws FileNotFoundException {
        return retrieveCompilationUnit().findAll(MethodDeclaration.class);
    }

    public static CompilationUnit retrieveCompilationUnit() throws FileNotFoundException {
        return Singleton.getInstance();
    }

    public static void updateUserFile() throws IOException {
        // Aggiornamento del vecchio contenuto del file utente con quello nuovo.
        FileWriter fooWriter = new FileWriter(Singleton.getFile(), false);
        fooWriter.write(retrieveCompilationUnit().toString());
        fooWriter.close();
    }
}
