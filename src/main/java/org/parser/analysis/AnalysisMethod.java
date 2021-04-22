package org.parser.analysis;

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
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.parser.analysis.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.parser.App.compareConditionsElements;
import static org.parser.App.compareElementContent;
import static org.parser.error.ErrorCode.generateErrorException;

public abstract class AnalysisMethod {

    protected AnalysisMethod() {
    }

    public abstract boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException;

    protected static Expression retrieveExpression(String argument) {
        return StaticJavaParser.parseExpression(argument);
    }

    protected static String isBinary(String argument) {
        Expression expression = retrieveExpression(argument);
        if (expression.isBinaryExpr()) {
            return expression.asBinaryExpr().getLeft().toString().trim();
        }
        return argument;
    }

    // Se tutti i controlli precedenti hanno dato esito positivo, si può procedere al prelevamento della versione iterativa
    // corrispondente al metodo ricorsivo uguale al metodo dell'utente.
    protected static File retrieveMethodFile(List<File> list, String version) throws ErrorException {
        return CollectionUtils.emptyIfNull(list)
                .stream()
                .filter(item -> item.getName().contains(version))
                .findAny()
                .orElseThrow(() -> generateErrorException(ErrorCode.MALFORMED_FILENAME_APPLICATION));
    }

    protected static <A, B> Stream<Pair<A, B>> iterativeListsFlow(Stream<A> as, Stream<B> bs) {
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

    protected static BlockStmt retrieveBodyMethod(MethodDeclaration method) throws ErrorException {
        return Optional.ofNullable(method.findFirst(BlockStmt.class))
                .get()
                .orElseThrow(() -> generateErrorException(ErrorCode.ABSENCE_BODY_METHOD));
    }

    protected static List retrieveStatementList(MethodDeclaration method, Class classStmt) throws ErrorException {
        return retrieveBodyMethod(method).getChildNodesByType(classStmt);
    }

    protected static boolean checkConditionsElements(MethodDeclaration user, MethodDeclaration recursive, Expression exp1, Expression exp2) {
        return Boolean.TRUE == !compareConditionsElements(user, recursive, exp1.toString(), exp2.toString());
    }

    protected static boolean checkElementContent(MethodDeclaration user, MethodDeclaration recursive, Expression exp1, Expression exp2) {
        return Boolean.TRUE == !compareElementContent(user, recursive, exp1.toString(), exp2.toString());
    }

    protected static boolean compareClassLists(MethodDeclaration user, MethodDeclaration recursive, Class classStmt) throws ErrorException {
        return retrieveStatementList(user, classStmt).size() != retrieveStatementList(recursive, classStmt).size();
    }

    public static boolean compareSizeLists(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return compareClassLists(user, recursive, IfStmt.class) || compareClassLists(user, recursive, ForStmt.class) ||
                compareClassLists(user, recursive, ForEachStmt.class) || compareClassLists(user, recursive, WhileStmt.class) ||
                compareClassLists(user, recursive, SwitchStmt.class) || compareClassLists(user, recursive, BreakStmt.class) ||
                compareClassLists(user, recursive, ContinueStmt.class);
    }

    protected static boolean checkEmptyList(NodeList list1, NodeList list2) {
        return CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2);
    }

    protected static boolean checkNotEmptyList(MethodDeclaration user, MethodDeclaration recursive, Class classStmt) throws ErrorException {
        return CollectionUtils.isNotEmpty(retrieveStatementList(user, classStmt)) &&
                CollectionUtils.isNotEmpty(retrieveStatementList(recursive, classStmt));
    }

    public static boolean checkMethodSignature(MethodDeclaration user, MethodDeclaration recursive) {
        return checkEqualSizeList(user.getParameters(), recursive.getParameters()) &&
                user.getSignature().getParameterTypes().equals(recursive.getSignature().getParameterTypes()) &&
                user.getType().equals(recursive.getType());
    }

    protected static boolean checkEqualSizeList(NodeList list1, NodeList list2) {
        return (list1 != null && list2 != null) && list1.size() == list2.size();
    }

    protected static boolean checkEmptyAndNotList(NodeList list1, NodeList list2) {
        return CollectionUtils.isEmpty(list1) && CollectionUtils.isNotEmpty(list2);
    }

    protected static String[] splitWithLimit(String string, String separator, int count) {
        return StringUtils.split(string, separator, count);
    }

    protected static boolean checkDifferentMetaModel(Expression user, Expression recursive) {
        return user.getMetaModel() != recursive.getMetaModel();
    }

    protected static Expression retrieveParseExpression(String expression) {
        return StaticJavaParser.parseExpression(expression);
    }

    protected static String retrieveStringExpression(Expression expression) {
        return expression.toString().trim();
    }

    protected static BinaryExpr retrieveBinaryExpression(Expression expression) {
        return expression.asBinaryExpr();
    }

    protected static boolean retrieveBinaryOperator(BinaryExpr userBinary, BinaryExpr recursiveBinary) {
        return userBinary.getOperator() != recursiveBinary.getOperator();
    }

    protected static UnaryExpr retrieveUnaryExpression(Expression expression) {
        return expression.asUnaryExpr();
    }

    protected static AssignExpr retrieveAssignExpression(Expression expression) {
        return expression.asAssignExpr();
    }

    protected static boolean retrieveUnaryOperator(UnaryExpr userUnary, UnaryExpr recursiveUnary) {
        return userUnary.getOperator() != recursiveUnary.getOperator();
    }

    protected static boolean retrieveAssignOperator(AssignExpr userAssign, AssignExpr recursiveAssign) {
        return userAssign.getOperator() != recursiveAssign.getOperator();
    }

    // TODO: Definire meglio l'utilizzo dei metodi sopra nella classe App e riflettere su dove collocare il metodo sottostante
    // ZONE - Recursive call arguments
    public static boolean checkRecursiveCallArguments(MethodDeclaration user, MethodDeclaration recursive) {
        NodeList<Expression> userArgumentsList = getRecursiveMethodCall(user).getArguments();
        NodeList<Expression> recursiveArgumentsList = getRecursiveMethodCall(recursive).getArguments();

        return iterativeListsFlow(userArgumentsList.stream(), recursiveArgumentsList.stream())
                .anyMatch(pair -> Boolean.TRUE == !compareElementContent(user, recursive, pair.getKey().toString(), pair.getValue().toString()));
    }

}
