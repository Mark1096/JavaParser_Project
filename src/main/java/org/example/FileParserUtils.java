package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.example.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.example.ErrorCode.generateErrorException;

public class FileParserUtils {

    private FileParserUtils() {
    }

    // Estrapolazione di tutti i metodi contenuti nella classe passata all'interno del file
    // e salvataggio di tutti e soli i metodi ricorsivi all'interno di una lista
    public static List<MethodDeclaration> getRecursiveUserMethodList() throws ErrorException {
        CompilationUnit cu = FileUserSingleton.getInstance();  // Analisi e salvataggio del contenuto del file

        return CollectionUtils.emptyIfNull(cu.findAll(MethodDeclaration.class))
                .stream()
                .filter(element -> getRecursiveMethodCall(element) != null)
                .collect(Collectors.toList());
    }

    public static CompilationUnit retrieveCompilationUnit(File file) throws ErrorException {
        try {
            return StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    public static CompilationUnit retrieveUserCompilationUnit() throws ErrorException {
        return FileUserSingleton.getInstance();
    }

    public static List<MethodDeclaration> getAllUserMethodList() throws ErrorException {
        return retrieveUserCompilationUnit().findAll(MethodDeclaration.class);
    }

    public static void updateUserFile() throws ErrorException {
        // Aggiornamento del vecchio contenuto del file utente con quello nuovo.
        try (FileWriter fooWriter = new FileWriter(FileUserSingleton.getFile(), false)) {
            fooWriter.write(retrieveUserCompilationUnit().toString());
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.BAD_WRITING_FILE);
        }
    }

    // TODO : DA SISTEMARE I COMMENTI + PUNTAMENTO FILE
    public static File[] retrieveAlgorithmsToExaminedList() {
        // Directory che contiene le versioni standard di alcuni algoritmi ricorsivi e le corrispondenti versioni iterative
        File directoryPath = new File(FileUserSingleton.getFile().getParent() + "/Algoritmi");
        // Lista di tutti gli algoritmi da esaminare all'interno della directory "Algoritmi".
        return directoryPath.listFiles();
    }

    // Estrapolazione del metodo iterativo da andare a sostituire al metodo ricorsivo dell'utente.
    public static MethodDeclaration retrieveApplicationCompilationUnit(File file) throws ErrorException {
        return Optional.ofNullable(retrieveCompilationUnit(file).findFirst(MethodDeclaration.class))
                .get()
                .orElseThrow(() -> generateErrorException(ErrorCode.METHODLESS_CLASS_APPLICATION));
    }
}
