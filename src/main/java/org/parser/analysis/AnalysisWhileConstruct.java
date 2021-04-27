package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisWhileConstruct </h1>
 *
 * This class analyzes everything about the iterative While construct.
 */
public class AnalysisWhileConstruct extends AnalysisMethod {

    private static boolean checkWhileCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<WhileStmt> list1 = retrieveStatementList(user, WhileStmt.class);
        List<WhileStmt> list2 = retrieveStatementList(recursive, WhileStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, WhileStmt.class) && checkWhileCondition(user, recursive);
    }

}
