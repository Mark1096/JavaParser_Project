package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ForEachStmt;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorException;

import java.util.List;

/**
 * <h1> AnalysisForEachConstruct </h1>
 * <p>
 * This class analyzes everything about the iterative ForEach construct.
 */
public class AnalysisForEachConstruct extends AnalysisMethod {

    /**
     * Returns the type of the iterative element of the forEach.
     *
     * @param forEachStmt the for each stmt
     * @return string
     */
    private static String retrieveForEachVariableType(ForEachStmt forEachStmt) {
        return retrieveVariableType(forEachStmt.getVariable().asVariableDeclarationExpr().getVariable(0));
    }

    /**
     * Compares the contents of the forEach conditions of the two methods passed as input and checks for a match.
     *
     * @param user the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean checkForEachCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ForEachStmt> list1 = retrieveStatementsList(user, ForEachStmt.class);
        List<ForEachStmt> list2 = retrieveStatementsList(recursive, ForEachStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !StringUtils.equals(retrieveForEachVariableType(pair.getKey()), retrieveForEachVariableType(pair.getValue())))
                .anyMatch(pair -> checkElementContent(user, recursive, pair.getKey().getIterable(), pair.getValue().getIterable()));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the forEach construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, ForEachStmt.class) && checkForEachCondition(user, recursive);
    }

}
