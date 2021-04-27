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
 *
 * This class analyzes everything about the iterative For construct,
 * providing methods that check the three states (Initialization, Condition, Update).
 */
public class AnalysisForConstruct extends AnalysisMethod {

    private static NodeList<Expression> retrieveForInitialization(ForStmt forStmt) {
        return forStmt.getInitialization();
    }

    private static boolean checkIsPresentForCompare(ForStmt forStmt) {
        return forStmt.getCompare().isPresent();
    }

    private static boolean retrieveForCompare(ForStmt forUser, ForStmt forApplication) {
        return !checkIsPresentForCompare(forUser) && !checkIsPresentForCompare(forApplication);
    }

    private static NodeList<Expression> retrieveForUpdate(ForStmt forStmt) {
        return forStmt.getUpdate();
    }

    private static boolean checkIsPresentAndNotList(ForStmt forUser, ForStmt forApplication) {
        return !checkIsPresentForCompare(forUser) && checkIsPresentForCompare(forApplication);
    }

    private static boolean checkForEmpty(Pair<ForStmt, ForStmt> pair) {
        return checkEmptyList(retrieveForInitialization(pair.getKey()), retrieveForInitialization(pair.getValue())) &&
                retrieveForCompare(pair.getKey(), pair.getValue()) &&
                checkEmptyList(retrieveForUpdate(pair.getKey()), retrieveForUpdate(pair.getValue()));
    }

    private static boolean checkForDifferentStatement(Pair<ForStmt, ForStmt> pair) {
        return checkEmptyAndNotList(retrieveForInitialization(pair.getKey()), retrieveForInitialization(pair.getValue())) ||
                checkEmptyAndNotList(retrieveForInitialization(pair.getValue()), retrieveForInitialization(pair.getKey())) ||
                checkIsPresentAndNotList(pair.getKey(), pair.getValue()) || checkIsPresentAndNotList(pair.getValue(), pair.getKey()) ||
                checkEmptyAndNotList(retrieveForUpdate(pair.getKey()), retrieveForUpdate(pair.getValue())) ||
                checkEmptyAndNotList(retrieveForUpdate(pair.getValue()), retrieveForUpdate(pair.getKey()));
    }

    private static boolean checkForCompareElement(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return checkIsPresentForCompare(pair.getKey()) &&
                !compareConditionsElements(user, recursive, pair.getKey().getCompare().get().toString(), pair.getValue().getCompare().get().toString());
    }

    private static int countForInitialization(String initialization) {
        return countStringMatches(initialization, ",");
    }

    private static String retrieveForContentInitialization(NodeList<Expression> list) {
        return list.get(0).toString();
    }

    private static boolean compareForInitializationElement(MethodDeclaration user, MethodDeclaration recursive, List<String> userList, List<String> recursiveList) {
        return iterativeListsFlow(userList.stream(), recursiveList.stream())
                .anyMatch(pair -> Boolean.TRUE ==
                        !compareElementContent(user, recursive, retrieveInitializationValue(pair.getKey()), retrieveInitializationValue(pair.getValue())));
    }

    private static boolean checkForInitializationNumber(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
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

    private static boolean checkForInitialization(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return retrieveForInitialization(pair.getKey()).isNonEmpty() && checkForInitializationNumber(user, recursive, pair);
    }

    private static boolean checkForUpdateUnaryExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        UnaryExpr userUnary = retrieveUnaryExpression(userExpression);
        UnaryExpr recursiveUnary = retrieveUnaryExpression(recursiveExpression);
        if (retrieveUnaryOperator(userUnary, recursiveUnary)) {
            return true;
        }
        return !(compareElementContent(user, recursive,
                userUnary.getExpression().toString(), recursiveUnary.getExpression().toString()));
    }

    private static Expression retrieveAssignExpressionValue(Expression expression) {
        return retrieveAssignExpression(expression).getValue();
    }

    private static boolean checkForUpdateBinaryExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        BinaryExpr userBinary = retrieveBinaryExpression(userExpression);
        BinaryExpr recursiveBinary = retrieveBinaryExpression(recursiveExpression);

        if (retrieveBinaryOperator(userBinary, recursiveBinary)) {
            return true;
        }
        if (!compareElementContent(user, recursive,
                userBinary.getLeft().toString(), recursiveBinary.getLeft().toString())) {
            return true;
        }
        return !(compareElementContent(user, recursive,
                userBinary.getRight().toString(), recursiveBinary.getRight().toString()));
    }

    private static boolean checkForUpdateElementContent(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        String userElement = retrieveStringExpression(retrieveAssignExpressionValue(userExpression));
        String recursiveElement = retrieveStringExpression(retrieveAssignExpressionValue(recursiveExpression));

        if (retrieveAssignExpressionValue(userExpression).isNameExpr()) {
            return !(compareElementContent(user, recursive, userElement, recursiveElement));
        }
        return !StringUtils.equals(userElement, recursiveElement);
    }

    private static boolean checkForUpdateAssignExpression(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        AssignExpr userAssign = retrieveAssignExpression(userExpression);
        AssignExpr recursiveAssign = retrieveAssignExpression(recursiveExpression);

        if (retrieveAssignOperator(userAssign, recursiveAssign)) {
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

    private static boolean verifyCaseManagementForUpdate(MethodDeclaration user, MethodDeclaration recursive, Expression userExpression, Expression recursiveExpression) {
        if (userExpression.isUnaryExpr() && recursiveExpression.isUnaryExpr()) {
            return checkForUpdateUnaryExpression(user, recursive, userExpression, recursiveExpression);
        }
        return checkForUpdateAssignExpression(user, recursive, userExpression, recursiveExpression);
    }

    private static boolean checkForUpdateElement(MethodDeclaration user, MethodDeclaration recursive, NodeList<Expression> userList, NodeList<Expression> recursiveList) {
        return iterativeListsFlow(userList.stream(), recursiveList.stream())
                .filter(pair -> !checkDifferentMetaModel(retrieveParseExpression(pair.getKey().toString()), retrieveParseExpression(pair.getValue().toString())))
                .anyMatch(pair -> Boolean.TRUE == !verifyCaseManagementForUpdate(user, recursive,
                        retrieveParseExpression(pair.getKey().toString()), retrieveParseExpression(pair.getValue().toString())));
    }

    private static boolean checkForUpdateListSize(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        NodeList<Expression> userList = retrieveForUpdate(pair.getKey());
        NodeList<Expression> recursiveList = retrieveForUpdate(pair.getValue());

        if (!checkEqualSizeList(userList, recursiveList)) {
            return true;
        }
        return checkForUpdateElement(user, recursive, userList, recursiveList);
    }

    private static boolean checkForUpdate(MethodDeclaration user, MethodDeclaration recursive, Pair<ForStmt, ForStmt> pair) {
        return retrieveForUpdate(pair.getKey()).isNonEmpty() && checkForUpdateListSize(user, recursive, pair);
    }

    private static List<Pair<ForStmt, ForStmt>> checkForCondition(MethodDeclaration user, MethodDeclaration recursive, List<ForStmt> list1, List<ForStmt> list2) {
        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !checkForEmpty(pair))
                .filter(pair -> !checkForDifferentStatement(pair))
                .filter(pair -> !checkForCompareElement(user, recursive, pair))
                .filter(pair -> !checkForInitialization(user, recursive, pair))
                .collect(Collectors.toList());
    }

    private static boolean checkAllForCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<ForStmt, ForStmt>> result = checkForCondition(user, recursive,
                retrieveStatementList(user, ForStmt.class), retrieveStatementList(recursive, ForStmt.class));

        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == !checkForUpdate(user, recursive, pair));
    }

    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, ForStmt.class) && checkAllForCondition(user, recursive);
    }

    private static UnaryExpr retrieveUnaryExpression(Expression expression) {
        return expression.asUnaryExpr();
    }

    private static boolean retrieveUnaryOperator(UnaryExpr userUnary, UnaryExpr recursiveUnary) {
        return userUnary.getOperator() != recursiveUnary.getOperator();
    }

    private static AssignExpr retrieveAssignExpression(Expression expression) {
        return expression.asAssignExpr();
    }

    private static boolean retrieveBinaryOperator(BinaryExpr userBinary, BinaryExpr recursiveBinary) {
        return userBinary.getOperator() != recursiveBinary.getOperator();
    }

    private static boolean retrieveAssignOperator(AssignExpr userAssign, AssignExpr recursiveAssign) {
        return userAssign.getOperator() != recursiveAssign.getOperator();
    }

    private static Expression retrieveParseExpression(String expression) {
        return StaticJavaParser.parseExpression(expression);
    }

}
