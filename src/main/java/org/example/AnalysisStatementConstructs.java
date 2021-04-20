package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.*;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.App.compareConditionsElements;
import static org.example.App.compareElementContent;
import static org.example.ErrorCode.generateErrorException;

public class AnalysisStatementConstructs extends AnalysisMethod {

    public static boolean checkMethodSignature(MethodDeclaration user, MethodDeclaration recursive) {
        return checkEqualSizeList(user.getParameters(), recursive.getParameters()) &&
                user.getSignature().getParameterTypes().equals(recursive.getSignature().getParameterTypes()) &&
                user.getType().equals(recursive.getType());
    }

    private static boolean checkEqualSizeList(NodeList list1, NodeList list2) {
        return (list1 != null && list2 != null) && list1.size() == list2.size();
    }

    private static BlockStmt retrieveBodyMethod(MethodDeclaration method) throws ErrorException {
        return Optional.ofNullable(method.findFirst(BlockStmt.class))
                .get()
                .orElseThrow(() -> generateErrorException(ErrorCode.ABSENCE_BODY_METHOD));
    }

    private static List retrieveStatementList(MethodDeclaration method, Class classStmt) throws ErrorException {
        return retrieveBodyMethod(method).getChildNodesByType(classStmt);
    }

    private static boolean compareClassLists(MethodDeclaration user, MethodDeclaration recursive, Class classStmt) throws ErrorException {
        return retrieveStatementList(user, classStmt).size() != retrieveStatementList(recursive, classStmt).size();
    }

    public static boolean compareSizeLists(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return compareClassLists(user, recursive, IfStmt.class) || compareClassLists(user, recursive, ForStmt.class) ||
                compareClassLists(user, recursive, ForEachStmt.class) || compareClassLists(user, recursive, WhileStmt.class) ||
                compareClassLists(user, recursive, SwitchStmt.class) || compareClassLists(user, recursive, BreakStmt.class) ||
                compareClassLists(user, recursive, ContinueStmt.class);
    }

    private static boolean checkNotEmptyList(MethodDeclaration user, MethodDeclaration recursive, Class classStmt) throws ErrorException {
        return CollectionUtils.isNotEmpty(retrieveStatementList(user, classStmt)) &&
                CollectionUtils.isNotEmpty(retrieveStatementList(recursive, classStmt));
    }

    private static <A, B> Stream<Pair<A, B>> iterativeListsFlow(Stream<A> as, Stream<B> bs) {
        Iterator<A> i1 = as.iterator();
        Iterator<B> i2 = bs.iterator();
        Iterable<Pair<A, B>> i = () -> new Iterator<Pair<A, B>>() {
            public boolean hasNext() {
                return i1.hasNext() && i2.hasNext();
            }

            public Pair<A, B> next() {
                return new Pair<A, B>(i1.next(), i2.next());
            }
        };
        return StreamSupport.stream(i.spliterator(), false);
    }

    private static boolean checkConditionsElements(MethodDeclaration user, MethodDeclaration recursive, Expression exp1, Expression exp2) {
        return Boolean.TRUE == !compareConditionsElements(user, recursive, exp1.toString(), exp2.toString());
    }

    private static boolean checkElementContent(MethodDeclaration user, MethodDeclaration recursive, Expression exp1, Expression exp2) {
        return Boolean.TRUE == !compareElementContent(user, recursive, exp1.toString(), exp2.toString());
    }

    // ZONE - IfStmt
    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di IfStmt
    private static boolean checkIfCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<IfStmt> list1 = retrieveStatementList(user, IfStmt.class);
        List<IfStmt> list2 = retrieveStatementList(recursive, IfStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli IfStmt
    public static boolean checkIfList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, IfStmt.class) && checkIfCondition(user, recursive);
    }

    // ZONE - WhileStmt
    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di WhileStmt
    private static boolean checkWhileCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<WhileStmt> list1 = retrieveStatementList(user, WhileStmt.class);
        List<WhileStmt> list2 = retrieveStatementList(recursive, WhileStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli WhileStmt
    public static boolean checkWhileList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, WhileStmt.class) && checkWhileCondition(user, recursive);
    }

    private static String retrieveForEachTypeVariable(ForEachStmt forEachStmt) {
        return forEachStmt.getVariable().asVariableDeclarationExpr().getVariable(0).getType().toString();
    }

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di ForEachStmt
    private static boolean checkForEachCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ForEachStmt> list1 = retrieveStatementList(user, ForEachStmt.class);
        List<ForEachStmt> list2 = retrieveStatementList(recursive, ForEachStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !StringUtils.equals(retrieveForEachTypeVariable(pair.getKey()), retrieveForEachTypeVariable(pair.getValue())))
                .anyMatch(pair -> checkElementContent(user, recursive, pair.getKey().getIterable(), pair.getValue().getIterable()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli ForEachStmt
    public static boolean checkForEachList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, ForEachStmt.class) && checkForEachCondition(user, recursive);
    }

    // ZONE - SwitchStmt
    private static int retrieveSwitchNumberEntries(SwitchStmt switchStmt) {
        return switchStmt.getEntries().size();
    }

    private static NodeList<Expression> retrieveSwitchLabel(SwitchStmt switchStmt) {
        return switchStmt.getEntry(retrieveSwitchNumberEntries(switchStmt) - 1).getLabels();
    }

    private static boolean checkSwitchLabelEmpty(SwitchStmt switch1, SwitchStmt switch2) {
        return checkEmptyAndNotList(retrieveSwitchLabel(switch1), retrieveSwitchLabel(switch2)) ||
                checkEmptyAndNotList(retrieveSwitchLabel(switch2), retrieveSwitchLabel(switch1));
    }

    private static List<Pair<SwitchStmt, SwitchStmt>> checkSwitchProperties(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<SwitchStmt> list1 = retrieveStatementList(user, SwitchStmt.class);
        List<SwitchStmt> list2 = retrieveStatementList(recursive, SwitchStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !checkElementContent(user, recursive, pair.getKey().getSelector(), pair.getValue().getSelector()))
                .filter(pair -> retrieveSwitchNumberEntries(pair.getKey()) == retrieveSwitchNumberEntries(pair.getValue()))
                .filter(pair -> !checkSwitchLabelEmpty(pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
    }

    private static String retrieveSwitchCaseName(SwitchStmt switchStmt, int index) {
        return switchStmt.getEntry(index).getLabels().get(0).toString();
    }

    private static int retrieveSwitchNumberCase(Pair<SwitchStmt, SwitchStmt> pair) {
        return checkEmptyList(retrieveSwitchLabel(pair.getKey()), retrieveSwitchLabel(pair.getValue())) ?
                retrieveSwitchNumberEntries(pair.getKey()) - 1 : retrieveSwitchNumberEntries(pair.getKey());
    }

    private static boolean checkSwitchCaseContent(MethodDeclaration user, MethodDeclaration recursive, Pair<SwitchStmt, SwitchStmt> pair) {
        for (int i = 0; i < retrieveSwitchNumberCase(pair); i++) {
            if (!compareElementContent(user, recursive,
                    retrieveSwitchCaseName(pair.getKey(), i), retrieveSwitchCaseName(pair.getValue(), i))) {
                return true;
            }
        }
        return false;
    }

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di SwitchStmt
    private static boolean checkSwitchCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<SwitchStmt, SwitchStmt>> result = checkSwitchProperties(user, recursive);
        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == checkSwitchCaseContent(user, recursive, pair));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli SwitchStmt
    public static boolean checkSwitchList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, SwitchStmt.class) && checkSwitchCondition(user, recursive);
    }

    // ZONE - ForStmt
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

    private static boolean checkEmptyList(NodeList list1, NodeList list2) {
        return CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2);
    }

    private static boolean checkEmptyAndNotList(NodeList list1, NodeList list2) {
        return CollectionUtils.isEmpty(list1) && CollectionUtils.isNotEmpty(list2);
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
        return StringUtils.countMatches(initialization, ",");
    }

    private static String[] splitWithLimit(String string, String separator, int count) {
        return StringUtils.split(string, separator, count);
    }

    private static String retrieveInitializationValue(String string) {
        return StringUtils.split(string, "=")[1].trim();
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

    private static boolean checkDifferentMetaModel(Expression user, Expression recursive) {
        return user.getMetaModel() != recursive.getMetaModel();
    }

    private static Expression retrieveParseExpression(String expression) {
        return StaticJavaParser.parseExpression(expression);
    }

    private static UnaryExpr retrieveUnaryExpression(Expression expression) {
        return expression.asUnaryExpr();
    }

    private static AssignExpr retrieveAssignExpression(Expression expression) {
        return expression.asAssignExpr();
    }

    private static boolean retrieveUnaryOperator(UnaryExpr userUnary, UnaryExpr recursiveUnary) {
        return userUnary.getOperator() != recursiveUnary.getOperator();
    }

    private static boolean retrieveAssignOperator(AssignExpr userAssign, AssignExpr recursiveAssign) {
        return userAssign.getOperator() != recursiveAssign.getOperator();
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

    private static String retrieveStringExpression(Expression expression) {
        return expression.toString().trim();
    }

    private static BinaryExpr retrieveBinaryExpression(Expression expression) {
        return expression.asBinaryExpr();
    }

    private static boolean retrieveBinaryOperator(BinaryExpr userBinary, BinaryExpr recursiveBinary) {
        return userBinary.getOperator() != recursiveBinary.getOperator();
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

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli ForStmt
    public static boolean checkForList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, ForStmt.class) && checkAllForCondition(user, recursive);
    }

}

