package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.example.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.example.ErrorCode.generateErrorException;

public class FileParserUtils {

    private static FileUser fileUser;

    private FileParserUtils() {
    }

    // Estrapolazione di tutti i metodi contenuti nella classe passata all'interno del file
    // e salvataggio di tutti e soli i metodi ricorsivi all'interno di una lista
    public static List<MethodDeclaration> getRecursiveUserMethodList(File userFile) throws ErrorException {
        fileUser = new FileUser(userFile);
        CompilationUnit cu = fileUser.getCompilationUnit();  // Analisi e salvataggio del contenuto del file

        return CollectionUtils.emptyIfNull(cu.findAll(MethodDeclaration.class))
                .stream()
                .filter(element -> getRecursiveMethodCall(element) != null)
                .collect(Collectors.toList());
    }

    public static CompilationUnit retrieveApplicationCompilationUnit(File file) throws ErrorException {
        try {
            return StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.TROUBLE_PARSING_FILE);
        }
    }

    public static CompilationUnit retrieveUserCompilationUnit() throws ErrorException {
        return fileUser.getCompilationUnit();
    }

    public static List<MethodDeclaration> getAllUserMethodList() throws ErrorException {
        return retrieveUserCompilationUnit().findAll(MethodDeclaration.class);
    }

    public static void updateUserFile() throws ErrorException {
        // Aggiornamento del vecchio contenuto del file utente con quello nuovo.
        try (FileWriter fooWriter = new FileWriter(fileUser.getFile(), false)) {
            fooWriter.write(retrieveUserCompilationUnit().toString());
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.BAD_WRITING_FILE);
        }
    }

    // TODO : DA SISTEMARE I COMMENTI + PUNTAMENTO FILE
    public static File[] retrieveAlgorithmsToExaminedList() {

        // Directory che contiene le versioni standard di alcuni algoritmi ricorsivi e le corrispondenti versioni iterative
        File directoryPath = new File(retrieveAlgorithmsPath());
        // Lista di tutti gli algoritmi da esaminare all'interno della directory "Algoritmi".
        return directoryPath.listFiles();
    }

    // Estrapolazione del metodo iterativo da andare a sostituire al metodo ricorsivo dell'utente.
    public static MethodDeclaration retrieveApplicationMethods(File file) throws ErrorException {
        return Optional.ofNullable(retrieveApplicationCompilationUnit(file).findFirst(MethodDeclaration.class))
                .get()
                .orElseThrow(() -> generateErrorException(ErrorCode.METHODLESS_CLASS_APPLICATION));
    }

    private static String retrieveRootPathProject() {
        return Paths.get("").toAbsolutePath().toString().split("target")[0];
    }

    private static String retrieveAlgorithmsPath() {
        return retrieveRootPathProject().concat("algorithms");
    }

    private static void listFilesForFolder(List<File> list, final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(list, fileEntry);
            } else {
                list.add(fileEntry);
            }
        }
    }

    private static String retrieveExtensionFilename(File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    public static List<File> retrieveUserFileList() {
        List<File> list = new ArrayList<>();
        listFilesForFolder(list, new File(retrieveUserFolderPath()));
        return CollectionUtils.emptyIfNull(list)
                .stream()
                .filter(element -> StringUtils.contains(retrieveExtensionFilename(element), "java"))
                .collect(Collectors.toList());
    }

    public static String retrieveUserFolderPath() {
        return retrieveRootPathProject().concat("userCode");
    }
}
