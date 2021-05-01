package org.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.parser.file.FileParserUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.parser.analysis.AnalysisIterativeMethod.replaceRecursiveWithIterativeMethod;
import static org.parser.analysis.AnalysisMethod.*;
import static org.parser.analysis.AnalysisRecursiveMethod.retrieveRecursiveFile;
import static org.parser.analysis.AnalysisStatementConstructs.checkAllConstruct;
import static org.parser.file.FileParserUtils.*;

/**
 * <h1> JavaParser </h1>
 *
 * <b>This program is used to convert recursive methods to iterative ones.</b>
 * It analyzes one or more Java files, extracts only the recursive methods and compares them with the recursive methods contained in the application.
 * Only those that satisfy all the comparison conditions will be converted into the corresponding iterative versions.
 */
public class App {

    /**
     * This is the main method from which methods to analyze user files will be called.
     *
     * @param args the input arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {

        for (File userFile : retrieveUserFilesList()) {
            List<MethodDeclaration> listUserRecursiveMethods = FileParserUtils.getRecursiveUserMethodList(userFile);
            File[] algorithmList = retrieveAlgorithmsToExaminedList();

            for (MethodDeclaration userMethod : listUserRecursiveMethods) {

                for (File file : algorithmList) {
                    List<File> files = new ArrayList(Arrays.asList(file.listFiles()));

                    MethodDeclaration recursiveMethod = retrieveCompilationUnitRecursiveMethod(retrieveRecursiveFile(files));

                    if (checkMethodSignature(userMethod, recursiveMethod)) {

                        if (compareSizeLists(userMethod, recursiveMethod)) {
                            System.out.println("Different number of iterative or conditional constructs!");
                            continue;
                        }

                        if (checkAllConstruct(userMethod, recursiveMethod)) {
                            System.out.println("The iterative version of the following recursive method is not available: " + recursiveMethod);
                            continue;
                        }

                        if (checkRecursiveCallArguments(userMethod, recursiveMethod)) {
                            System.out.println("The arguments of the method call are different!");
                            System.out.println("The iterative version of the following recursive method is not available: " + recursiveMethod);
                            continue;
                        }

                        System.out.println("Same arguments in the recursive call to the method!");

                        replaceRecursiveWithIterativeMethod(files, userMethod);
                        break;
                    }
                }
            }

            FileParserUtils.updateUserFile();
        }
    }
}