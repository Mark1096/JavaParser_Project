package org.parser.analysis;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> AnalysisRecursiveMethod </h1>
 * <p>
 * This class takes care of searching for recursive methods within the class passed in by the user,
 * so as to filter out only the methods needed for iterative conversion.
 */
public abstract class AnalysisRecursiveMethod extends AnalysisMethod {

    /**
     * It takes the file containing only the recursive version of the algorithm to be compared with the user's one.
     *
     * @param files the files
     * @return file
     * @throws ErrorException the error exception
     */
    public static File retrieveRecursiveFile(List<File> files) throws ErrorException {
        return retrieveMethodFile(files, "Recursive");
    }

    /**
     * Returns the recursive method call that is found within the body of the method passed in, null otherwise.
     *
     * @param methodDeclaration the method declaration
     * @return recursive method call
     */
    public static MethodCallExpr getRecursiveMethodCall(MethodDeclaration methodDeclaration) {
        List<MethodCallExpr> listMethodCall = methodDeclaration.findAll(MethodCallExpr.class);

        for (MethodCallExpr method : listMethodCall) {
            NameExpr nameExpr = method.getNameAsExpression();
            NodeList arguments = method.getArguments();

            if (methodDeclaration.getNameAsExpression().equals(nameExpr) &&
                    methodDeclaration.getParameters().size() == arguments.size()) {

                List parametersType = methodDeclaration.getSignature().getParameterTypes();
                List argumentsType = retrieveArgumentsType(arguments, methodDeclaration, parametersType);

                if (parametersType.size() != argumentsType.size()) {
                    System.out.println("The arguments of the recursive call can only be local variables or formal parameters!");
                    return null;
                }

                boolean sameParameters = true;

                for (int i = 0; i < parametersType.size(); i++) {
                    if (!(StringUtils.equals(parametersType.get(i).toString(), argumentsType.get(i).toString()))) {
                        sameParameters = false;
                        break;
                    }
                }

                if (sameParameters) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Checks the list of local variable declarations whose names match those of the arguments passed in the method call.
     *
     * @param methodVariables the method variables
     * @param argument the argument
     * @return list
     */
    private static List<VariableDeclarationExpr> retrieveMethodArgumentsList(List<VariableDeclarationExpr> methodVariables, String argument) {
        return CollectionUtils.emptyIfNull(methodVariables)
                .stream()
                .filter(variable -> StringUtils.equals(variable.getVariable(0).getNameAsString(), argument))
                .collect(Collectors.toList());
    }

    /**
     * Returns the type of the arguments to the method call, doing a name search between the current argument
     * and the local variables or formal parameters (in case you don't match the former).
     *
     * @param arguments the arguments
     * @param methodDeclaration the method declaration
     * @param parametersType the parameters type
     * @return list
     */
    private static List retrieveArgumentsType(NodeList arguments, MethodDeclaration methodDeclaration, List parametersType) {
        List argumentsType = new ArrayList();
        CollectionUtils.emptyIfNull(arguments)
                .forEach(arg -> {
                    String argument = isBinary(arg.toString(), null);
                    List<VariableDeclarationExpr> methodVariables =
                            methodDeclaration.getBody().get().findAll(VariableDeclarationExpr.class);

                    CollectionUtils.emptyIfNull(retrieveMethodArgumentsList(methodVariables, argument))
                            .stream()
                            .findFirst()
                            .map(element -> argumentsType.add(element.getVariable(0).getType()));

                    if (argumentsType.size() == arguments.indexOf(arg)) {
                        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                            if (methodDeclaration.getParameter(i).getNameAsString().equals(argument)) {
                                argumentsType.add(parametersType.get(i));
                            }
                        }
                    }
                });
        return argumentsType;
    }

}
