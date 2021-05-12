package org.parser.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;

import java.io.File;
import java.util.List;

import static org.parser.error.ErrorCode.generateErrorException;
import static org.parser.file.FileParserUtils.getAllUserMethodList;
import static org.parser.file.FileParserUtils.retrieveCompilationUnitRecursiveMethod;

/**
 * <h1> AnalysisIterativeMethod </h1>
 * <p>
 * This class takes care of replacing the user's recursive method with the corresponding iterative version,
 * leaving the names of the formal parameters provided by the user unchanged.
 */
public abstract class AnalysisIterativeMethod extends AnalysisMethod {

    /**
     * Returns the file containing the iterative version of the algorithm.
     *
     * @param files the files
     * @return file
     * @throws ErrorException the error exception
     */
    public static File retrieveIterativeFile(List<File> files) throws ErrorException {
        return retrieveMethodFile(files, "Iterative");
    }

    /**
     * Replaces the recursive version of the user method with the iterative one, keeping the same formal parameter names.
     *
     * @param files      the files
     * @param userMethod the user method
     * @throws ErrorException the error exception
     */
    public static void replaceRecursiveWithIterativeMethod(List<File> files, MethodDeclaration userMethod) throws ErrorException {
        File iterativePath = retrieveIterativeFile(files);
        MethodDeclaration iterative_method = retrieveCompilationUnitRecursiveMethod(iterativePath);
        MethodDeclaration newIterativeMethod = replaceMethodParametersName(iterative_method, userMethod);

        MethodDeclaration method = CollectionUtils.emptyIfNull(getAllUserMethodList())
                .stream()
                .filter(element -> StringUtils.equals(element.getDeclarationAsString(), userMethod.getDeclarationAsString()))
                .findFirst()
                .orElseThrow(() -> generateErrorException(ErrorCode.METHODLESS_CLASS_USER));

        method.setParameters(newIterativeMethod.getParameters());
        method.setBody(newIterativeMethod.getBody().get().asBlockStmt());
    }

    /**
     * Before replacing the iterative method with the recursive (user's) method, the underlying method is called
     * so that the parameter names of the recursive (user's) version remain unchanged.
     *
     * @param iterativeMethod the iterative method
     * @param userMethod      the user method
     * @return method declaration
     */
    public static MethodDeclaration replaceMethodParametersName(MethodDeclaration iterativeMethod, MethodDeclaration userMethod) {
        String bodyMethod = iterativeMethod.toString();

        for (int i = 0; i < iterativeMethod.getParameters().size(); i++) {
            Parameter iterativeParameter = iterativeMethod.getParameter(i);
            Parameter userParameter = userMethod.getParameter(i);

            if (StringUtils.equals(iterativeParameter.getType().toString(), userParameter.getType().toString())) {
                bodyMethod = bodyMethod.replaceAll("\\b" + iterativeParameter.getNameAsString() + "\\b", userParameter.getNameAsString());
            }
        }

        return StaticJavaParser.parseMethodDeclaration(bodyMethod);
    }

}
