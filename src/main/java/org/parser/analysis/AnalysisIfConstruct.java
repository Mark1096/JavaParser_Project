package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisIfConstruct </h1>
 * <p>
 * This class analyzes everything about the iterative If construct.
 */
public class AnalysisIfConstruct extends AnalysisMethod {

    /**
     * Compares the contents of the If conditions of the two methods passed as input and checks for a match.
     *
     * @param user the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean checkIfCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<IfStmt> list1 = retrieveStatementsList(user, IfStmt.class);
        List<IfStmt> list2 = retrieveStatementsList(recursive, IfStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the if construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, IfStmt.class) && checkIfCondition(user, recursive);
    }
}
