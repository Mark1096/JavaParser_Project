package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisDoWhileConstruct </h1>
 * <p>
 * This class analyzes everything about the iterative DoWhile construct.
 */
public class AnalysisDoWhileConstruct extends AnalysisMethod {

    /**
     * Compares the contents of the do-while conditions of the two methods passed as input and checks for a match.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean checkDoWhileCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<DoStmt> list1 = retrieveStatementsList(user, DoStmt.class);
        List<DoStmt> list2 = retrieveStatementsList(recursive, DoStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the do-while construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, DoStmt.class) && checkDoWhileCondition(user, recursive);
    }

}
