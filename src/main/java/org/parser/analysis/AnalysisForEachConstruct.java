package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ForEachStmt;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisForEachConstruct </h1>
 *
 * This class analyzes everything about the iterative ForEach construct.
 */
public class AnalysisForEachConstruct extends AnalysisMethod {

    private static String retrieveForEachTypeVariable(ForEachStmt forEachStmt) {
        return retrieveVariableType(forEachStmt.getVariable().asVariableDeclarationExpr().getVariable(0));
    }

    private static boolean checkForEachCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ForEachStmt> list1 = retrieveStatementList(user, ForEachStmt.class);
        List<ForEachStmt> list2 = retrieveStatementList(recursive, ForEachStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !StringUtils.equals(retrieveForEachTypeVariable(pair.getKey()), retrieveForEachTypeVariable(pair.getValue())))
                .anyMatch(pair -> checkElementContent(user, recursive, pair.getKey().getIterable(), pair.getValue().getIterable()));
    }

    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, ForEachStmt.class) && checkForEachCondition(user, recursive);
    }

}
