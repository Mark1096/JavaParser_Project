package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisIfConstruct </h1>
 *
 * This class analyzes everything about the iterative If construct.
 */
public class AnalysisIfConstruct extends AnalysisMethod {

    private static boolean checkIfCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<IfStmt> list1 = retrieveStatementList(user, IfStmt.class);
        List<IfStmt> list2 = retrieveStatementList(recursive, IfStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, IfStmt.class) && checkIfCondition(user, recursive);
    }
}
