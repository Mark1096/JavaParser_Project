package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisWhileConstruct </h1>
 * <p>
 * This class analyzes everything about the iterative While construct.
 */
public class AnalysisWhileConstruct extends AnalysisMethod {

    /**
     * Compares the contents of the While conditions of the two methods passed as input and checks for a match.
     *
     * @param user the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean checkWhileCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<WhileStmt> list1 = retrieveStatementsList(user, WhileStmt.class);
        List<WhileStmt> list2 = retrieveStatementsList(recursive, WhileStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the while construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, WhileStmt.class) && checkWhileCondition(user, recursive);
    }

}
