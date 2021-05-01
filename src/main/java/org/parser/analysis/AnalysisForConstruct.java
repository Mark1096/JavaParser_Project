package org.parser.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> AnalysisForConstruct </h1>
 * <p>
 * This class analyzes everything about the iterative For construct,
 * providing methods that check the three states (Initialization, Condition, Update).
 */
public class AnalysisForConstruct extends AnalysisMethod {

    /**
     * Returns the initialization status of the For.
     *
     * @param forStmt
     * @return NodeList<Expression>
     */
    private static NodeList<Expression> retrieveForInitialization(ForStmt forStmt) {
        return forStmt.getInitialization();
    }

    /**
     * Checks the existence of the condition status in the For.
     *
     * @param forStmt
     * @return boolean
     */
    private static boolean checkIsPresentForCompare(ForStmt forStmt) {
        return forStmt.getCompare().isPresent();
    }

    /**
     * Checks the non-existence of the condition state of both For passed in input.
     *
     * @param forUser
     * @param forApplication
     * @return boolean
     */
    private static boolean checkNotExistenceForCompare(ForStmt forUser, ForStmt forApplication) {
        return !checkIsPresentForCompare(forUser) && !checkIsPresentForCompare(forApplication);
    }

    /**
     * Returns the update status of the For.
     *
     * @param forStmt
     * @return NodeList<Expression>
     */
    private static NodeList<Expression> retrieveForUpdate(ForStmt forStmt) {
        return forStmt.getUpdate();
    }

    /**
     * Check if one For has condition status and the other does not.
     *
     * @param forUser
     * @param forApplication
     * @return boolean
     */
    private static boolean checkIsPresentAndNotForCompare(ForStmt forUser, ForStmt forApplication) {
        return !checkIsPresentForCompare(forUser) && checkIsPresentForCompare(forApplication);
    }

    /**
     * Checks if all states in the For do not exist.
     *
     * @param pair
     * @return boolean
     */
    private static boolean checkForEmpty(Pair<ForStmt, ForStmt> pair) {
        return checkEmptyLists(retrieveForInitialization(pair.getKey()), retrieveForInitialization(pair.getValue())) &&
                checkNotExistenceForCompare(pair.getKey(), pair.getValue()) &&
                checkEmptyLists(retrieveForUpdate(pair.getKey()), retrieveForUpdate(pair.getValue()));
    }

    /**
     * Verify that the same For state is present in one and not the other.
     *
     * @param pair
     * @return boolean
     */
    private static boolean checkForDifferentStatement(Pair<ForStmt, ForStmt> pair) {
        return checkEmptyAndNotLists(retrieveForInitialization(pair.getKey()), retrieveForInitialization(pair.getValue())) ||
                checkEmptyAndNotLists(retrieveForInitialization(pair.getValue()), retrieveForInitialization(pair.getKey())) ||
                checkIsPresentAndNotForCompare(pair.getKey(), pair.getValue()) || checkIsPresentAndNotForCompare(pair.getValue(), pair.getKey()) ||
                checkEmptyAndNotLists(retrieveForUpdate(pair.getKey()), retrieveForUpdate(pair.getValue())) ||
                checkEmptyAndNotLists(retrieveForUpdate(pair.getValue()), retrieveForUpdate(pair.getKey()));
    }

    /**
     * Checks the presence of the For condition status and compares its elements.
     *
     * @param user
     * @param recursive
     * @param pair
     * @return boolean
     */
    private static boolean checkForCompareElement(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return checkIsPresentForCompare(pair.getKey()) &&
                !compareConditionsElements(user, recursive, pair.getKey().getCompare().get().toString(), pair.getValue().getCompare().get().toString());
    }

    /**
     * Count the number of iterative variables initialized in the For.
     *
     * @param initialization
     * @return int
     */
    private static int countForInitialization(String initialization) {
        return countStringMatches(initialization, ",");
    }

    /**
     * Returns the iterative variables initialized in the For.
     *
     * @param list
     * @return String
     */
    private static String retrieveForContentInitialization(NodeList<Expression> list) {
        return list.get(0).toString();
    }

    /**
     * Compares the contents of the iterative initialized For variables of the two lists passed in as input.
     *
     * @param user
     * @param recursive
     * @param userList
     * @param recursiveList
     * @return boolean
     */
    private static boolean compareForInitializationElement(MethodDeclaration user, MethodDeclaration recursive, List<String> userList, List<String> recursiveList) {
        return iterativeListsFlow(userList.stream(), recursiveList.stream())
                .anyMatch(pair -> Boolean.TRUE ==
                        !compareElementContent(user, recursive, retrieveInitializationValue(pair.getKey()), retrieveInitializationValue(pair.getValue())));
    }

    /**
     * Checks the number of iterative variables initialized in the For and their contents in both For.
     *
     * @param user
     * @param recursive
     * @param pair
     * @return boolean
     */
    private static boolean checkForInitializationElements(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        String userContent = retrieveForContentInitialization(retrieveForInitialization(pair.getKey()));
        String recursiveContent = retrieveForContentInitialization(retrieveForInitialization(pair.getValue()));
        int countUser = countForInitialization(userContent);
        int countApplication = countForInitialization(recursiveContent);

        if (countUser != countApplication) {
            return true;
        }
        if (countForInitialization(userContent) != 0) {
            return compareForInitializationElement(user, recursive, Arrays.asList(splitWithLimit(userContent, ",", countUser + 1)),
                    Arrays.asList(splitWithLimit(recursiveContent, ",", countApplication + 1)));
        }
        return compareForInitializationElement(user, recursive, Collections.singletonList(userContent), Collections.singletonList(recursiveContent));
    }

    /**
     * Checks the presence of the For initialization status and compares its elements.
     *
     * @param user
     * @param recursive
     * @param pair
     * @return boolean
     */
    private static boolean checkForInitialization(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return retrieveForInitialization(pair.getKey()).isNonEmpty() && checkForInitializationElements(user, recursive, pair);
    }

    /**
     * Verify that the elements of the update state, of type UnaryExpr, are equal.
     *
     * @param user
     * @param recursive
     * @param userExpression
     * @param recursiveExpression
     * @return boolean
     */
    private static boolean checkForUpdateUnaryExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        UnaryExpr userUnary = retrieveUnaryExpression(userExpression);
        UnaryExpr recursiveUnary = retrieveUnaryExpression(recursiveExpression);
        if (compareUnaryOperator(userUnary, recursiveUnary)) {
            return true;
        }
        return !(compareElementContent(user, recursive,
                userUnary.getExpression().toString(), recursiveUnary.getExpression().toString()));
    }

    /**
     * Returns the contents of an expression of type AssignExpr.
     *
     * @param expression
     * @return Expression
     */
    private static Expression retrieveAssignExpressionValue(Expression expression) {
        return retrieveAssignExpression(expression).getValue();
    }

    /**
     * Checks the content of expressions of type BinaryExpr in the For update state.
     *
     * @param user
     * @param recursive
     * @param userExpression
     * @param recursiveExpression
     * @return boolean
     */
    private static boolean checkForUpdateBinaryExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        BinaryExpr userBinary = retrieveBinaryExpression(userExpression);
        BinaryExpr recursiveBinary = retrieveBinaryExpression(recursiveExpression);

        if (compareBinaryOperator(userBinary, recursiveBinary)) {
            return true;
        }
        if (!compareElementContent(user, recursive,
                userBinary.getLeft().toString(), recursiveBinary.getLeft().toString())) {
            return true;
        }
        return !(compareElementContent(user, recursive,
                userBinary.getRight().toString(), recursiveBinary.getRight().toString()));
    }

    /**
     * Checks the content of items in the For update state.
     *
     * @param user
     * @param recursive
     * @param userExpression
     * @param recursiveExpression
     * @return boolean
     */
    private static boolean checkForUpdateElementContent(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        String userElement = retrieveStringExpression(retrieveAssignExpressionValue(userExpression));
        String recursiveElement = retrieveStringExpression(retrieveAssignExpressionValue(recursiveExpression));

        if (retrieveAssignExpressionValue(userExpression).isNameExpr()) {
            return !(compareElementContent(user, recursive, userElement, recursiveElement));
        }
        return !StringUtils.equals(userElement, recursiveElement);
    }

    /**
     * Verify that the elements of the update state, of type AssignExpr, are equal.
     *
     * @param user
     * @param recursive
     * @param userExpression
     * @param recursiveExpression
     * @return boolean
     */
    private static boolean checkForUpdateAssignExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        AssignExpr userAssign = retrieveAssignExpression(userExpression);
        AssignExpr recursiveAssign = retrieveAssignExpression(recursiveExpression);

        if (compareAssignOperator(userAssign, recursiveAssign)) {
            return true;
        }
        if (!compareElementContent(user, recursive, retrieveStringExpression(userAssign.getTarget()),
                retrieveStringExpression(recursiveAssign.getTarget()))) {
            return true;
        }
        if (checkDifferentMetaModel(userAssign.getValue(), recursiveAssign.getValue())) {
            return true;
        }
        if (userAssign.getValue().isBinaryExpr()) {
            return checkForUpdateBinaryExpression(user, recursive, userAssign.getValue(), recursiveAssign.getValue());
        }
        return checkForUpdateElementContent(user, recursive, userExpression, recursiveExpression);
    }

    /**
     * Checks the types of expressions possible in the For update state.
     *
     * @param user
     * @param recursive
     * @param userExpression
     * @param recursiveExpression
     * @return boolean
     */
    private static boolean verifyCaseManagementForUpdate(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        if (userExpression.isUnaryExpr() && recursiveExpression.isUnaryExpr()) {
            return checkForUpdateUnaryExpression(user, recursive, userExpression, recursiveExpression);
        }
        return checkForUpdateAssignExpression(user, recursive, userExpression, recursiveExpression);
    }

    /**
     * Check the elements of the For update status.
     *
     * @param user
     * @param recursive
     * @param userList
     * @param recursiveList
     * @return boolean
     */
    private static boolean checkForUpdateElement(MethodDeclaration user, MethodDeclaration recursive, NodeList<Expression> userList, NodeList<Expression> recursiveList) {
        return iterativeListsFlow(userList.stream(), recursiveList.stream())
                .filter(pair -> !checkDifferentMetaModel(retrieveParseExpression(pair.getKey().toString()), retrieveParseExpression(pair.getValue().toString())))
                .anyMatch(pair -> Boolean.TRUE == !verifyCaseManagementForUpdate(user, recursive,
                        retrieveParseExpression(pair.getKey().toString()), retrieveParseExpression(pair.getValue().toString())));
    }

    /**
     * Checks the number of iterative variables in the update state and their content in both For.
     *
     * @param user
     * @param recursive
     * @param pair
     * @return boolean
     */
    private static boolean checkForUpdateListSize(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        NodeList<Expression> userList = retrieveForUpdate(pair.getKey());
        NodeList<Expression> recursiveList = retrieveForUpdate(pair.getValue());

        if (!checkEqualSizeLists(userList, recursiveList)) {
            return true;
        }
        return checkForUpdateElement(user, recursive, userList, recursiveList);
    }

    /**
     * Check that the forUpdate lists are not empty and compares the conditions of the forUpdate.
     *
     * @param user
     * @param recursive
     * @param pair
     * @return boolean
     */
    private static boolean checkForUpdate(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return retrieveForUpdate(pair.getKey()).isNonEmpty() && checkForUpdateListSize(user, recursive, pair);
    }

    /**
     * Compares the contents of the For conditions of the two lists passed as input and checks for a match.
     *
     * @param user
     * @param recursive
     * @param list1
     * @param list2
     * @return
     */
    private static List<Pair<ForStmt, ForStmt>> checkForCondition(MethodDeclaration user, MethodDeclaration recursive, List<ForStmt> list1, List<ForStmt> list2) {
        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !checkForEmpty(pair))
                .filter(pair -> !checkForDifferentStatement(pair))
                .filter(pair -> !checkForCompareElement(user, recursive, pair))
                .filter(pair -> !checkForInitialization(user, recursive, pair))
                .collect(Collectors.toList());
    }

    /**
     * Check that all conditions of the two methods passed in as input are equal.
     *
     * @param user
     * @param recursive
     * @return boolean
     * @throws ErrorException
     */
    private static boolean checkAllForCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<ForStmt, ForStmt>> result = checkForCondition(user, recursive,
                retrieveStatementsList(user, ForStmt.class), retrieveStatementsList(recursive, ForStmt.class));

        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == !checkForUpdate(user, recursive, pair));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the for construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return
     * @throws ErrorException
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, ForStmt.class) && checkAllForCondition(user, recursive);
    }

    /**
     * Returns the expression passed in as input in the form of UnaryExpr.
     *
     * @param expression
     * @return UnaryExpr
     */
    private static UnaryExpr retrieveUnaryExpression(Expression expression) {
        return expression.asUnaryExpr();
    }

    /**
     * Compare UnaryExpr operators.
     *
     * @param userUnary
     * @param recursiveUnary
     * @return boolean
     */
    private static boolean compareUnaryOperator(UnaryExpr userUnary, UnaryExpr recursiveUnary) {
        return userUnary.getOperator() != recursiveUnary.getOperator();
    }

    /**
     * Returns the expression passed in as input in the form of AssignExpr.
     *
     * @param expression
     * @return AssignExpr
     */
    private static AssignExpr retrieveAssignExpression(Expression expression) {
        return expression.asAssignExpr();
    }

    /**
     * Compare BinaryExpr operators.
     *
     * @param userBinary
     * @param recursiveBinary
     * @return boolean
     */
    private static boolean compareBinaryOperator(BinaryExpr userBinary, BinaryExpr recursiveBinary) {
        return userBinary.getOperator() != recursiveBinary.getOperator();
    }

    /**
     * Compare AssignExpr operators.
     *
     * @param userAssign
     * @param recursiveAssign
     * @return boolean
     */
    private static boolean compareAssignOperator(AssignExpr userAssign, AssignExpr recursiveAssign) {
        return userAssign.getOperator() != recursiveAssign.getOperator();
    }

    /**
     * Returns the string passed as input in the form of an expression.
     *
     * @param expression
     * @return Expression
     */
    private static Expression retrieveParseExpression(String expression) {
        return StaticJavaParser.parseExpression(expression);
    }

}
