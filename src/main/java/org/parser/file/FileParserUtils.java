package org.parser.file;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.parser.analysis.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.parser.error.ErrorCode.generateErrorException;

/**
 * <h1> FileParserUtils </h1>
 * <p>
 * This class deals with the analysis of user and application files.
 * It contains methods for extrapolating all and only the user's recursive methods,
 * the recursive algorithms provided by the application,
 * the creation of new files that will contain all the changes in the methods provided by the user's source files.
 */
public class FileParserUtils {

    private static FileUser userFile;
    private static final String inputFolder = "userCode";
    private static final String outputFolder = "userFileConverted/";

    private FileParserUtils() {
    }

    /**
     * Extrapolation of all methods contained in the class passed inside the file
     * and saving all and only the recursive methods inside a list.
     *
     * @param file the file
     * @return list method declaration
     * @throws ErrorException the error exception
     */
    public static List<MethodDeclaration> getRecursiveUserMethodList(File file) throws ErrorException {
        userFile = new FileUser(file);
        CompilationUnit cu = userFile.getUserCompilationUnit();

        return CollectionUtils.emptyIfNull(cu.findAll(MethodDeclaration.class))
                .stream()
                .filter(element -> getRecursiveMethodCall(element) != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the CompilationUnit instance of a file passed in as input.
     *
     * @param file the file
     * @return compilation unit
     * @throws ErrorException the error exception
     */
    public static CompilationUnit retrieveCompilationUnit(File file) throws ErrorException {
        try {
            return StaticJavaParser.parse(file);
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.TROUBLE_PARSING_FILE);
        }
    }

    /**
     * Returns the current CompilationUnit instance of the user file.
     *
     * @return compilation unit
     */
    public static CompilationUnit retrieveUserCompilationUnit() {
        return userFile.getUserCompilationUnit();
    }

    /**
     * Returns all methods within the user file.
     *
     * @return list method declaration
     */
    public static List<MethodDeclaration> getAllUserMethodList() {
        return retrieveUserCompilationUnit().findAll(MethodDeclaration.class);
    }

    /**
     * Creates a new file containing the result of program execution on the user's source file.
     *
     * @throws ErrorException the error exception
     */
    public static void updateUserFile() throws ErrorException {
        File directory = new File(retrieveUserFolderPath(outputFolder));
        if (!directory.exists()) {
            directory.mkdir();
        }

        File newFile = new File(directory + "/" + userFile.getFile().getName());

        try (FileWriter fooWriter = new FileWriter(newFile, false)) {
            fooWriter.write(retrieveUserCompilationUnit().toString());
        } catch (Exception e) {
            throw generateErrorException(ErrorCode.BAD_WRITING_FILE);
        }
    }

    /**
     * Returns the list of all algorithms to be examined in the directory "algorithms".
     *
     * @return file [ ]
     */
    public static File[] retrieveAlgorithmsToExaminedList() {
        File directoryPath = new File(retrieveAlgorithmsPath());
        return directoryPath.listFiles();
    }

    /**
     * Returns the method containing the program's recursive algorithm.
     *
     * @param file the file
     * @return method declaration
     * @throws ErrorException the error exception
     */
    public static MethodDeclaration retrieveCompilationUnitRecursiveMethod(File file) throws ErrorException {
        return Optional.ofNullable(retrieveCompilationUnit(file)
                .findFirst(MethodDeclaration.class))
                .get()
                .orElseThrow(() -> generateErrorException(ErrorCode.METHODLESS_CLASS_APPLICATION));
    }

    /**
     * Returns the absolute path of the project.
     *
     * @return string
     */
    private static String retrieveRootPathProject() {
        return Paths.get("").toAbsolutePath().toString().split("target")[0];
    }

    /**
     * Returns the path of the folder related to the algorithms made available by the program.
     *
     * @return string
     */
    private static String retrieveAlgorithmsPath() {
        return retrieveRootPathProject().concat("algorithms");
    }

    /**
     * Adds all files found in the folder to the list
     *
     * @param list the list
     * @param folder the folder
     */
    private static void listFilesForFolder(List<File> list, final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(list, fileEntry);
            } else {
                list.add(fileEntry);
            }
        }
    }

    /**
     * Returns the extension of the file passed as input.
     *
     * @param file the file
     * @return string
     */
    private static String retrieveExtensionFilename(File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    /**
     * Returns a list containing the user files to be analyzed.
     *
     * @return list
     */
    public static List<File> retrieveUserFilesList() {
        List<File> list = new ArrayList<>();
        listFilesForFolder(list, new File(retrieveUserFolderPath(inputFolder)));
        return CollectionUtils.emptyIfNull(list)
                .stream()
                .filter(element -> StringUtils.contains(retrieveExtensionFilename(element), "java"))
                .collect(Collectors.toList());
    }

    /**
     * Returns the path of the folder whose name is passed as input.
     *
     * @param folderName the folder name
     * @return string
     */
    public static String retrieveUserFolderPath(String folderName) {
        return retrieveRootPathProject().concat(folderName);
    }
}
