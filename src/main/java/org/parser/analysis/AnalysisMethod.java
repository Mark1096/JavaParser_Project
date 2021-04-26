package org.parser.analysis;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorCode;
import org.parser.error.ErrorException;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.parser.analysis.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.parser.error.ErrorCode.generateErrorException;

public abstract class AnalysisMethod {

    protected AnalysisMethod() {
    }

    /**
     * Method that ensures that the lists are not empty and compares the conditions of the constructs.
     *
     * @param user
     * @param recursive
     * @return boolean
     * @throws ErrorException
     */
    protected abstract boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException;

    private static Expression retrieveExpression(String argument) {
        return StaticJavaParser.parseExpression(argument);
    }

    protected static String retrieveSingleArgument(Expression expression) {
        if (!expression.asBinaryExpr().getLeft().isNameExpr()) {
            return expression.asBinaryExpr().getRight().toString().trim();
        }
        return expression.asBinaryExpr().getLeft().toString().trim();
    }

    /**
     * The method checks whether the formal parameter is of type BinaryExpr.
     * If not, it returns the parameter supplied as input.
     * In positive case it extrapolates from the parameter a single element, based on the value passed in the second parameter in input.
     *
     * @param argument
     * @param isLeft
     * @return String
     */
    protected static String isBinary(String argument, Boolean isLeft) {
        Expression expression = retrieveExpression(argument);
        if (expression.isBinaryExpr()) {
            if (isLeft == null) {
                return retrieveSingleArgument(expression);
            }
            if (isLeft) {
                return expression.asBinaryExpr().getLeft().toString().trim();
            }
            return expression.asBinaryExpr().getRight().toString().trim();
        }
        return argument;
    }

    /**
     * If all the previous controls have given positive result, it is possible to proceed to the extraction of the iterative version
     * corresponding to the recursive method equal to the user's method.
     *
     * @param list
     * @param version
     * @return File
     * @throws ErrorException
     */
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

    private static BlockStmt retrieveBodyMethod(MethodDeclaration method) throws ErrorException {
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

    private static boolean compareClassLists(MethodDeclaration user, MethodDeclaration recursive, Class classStmt) throws ErrorException {
        return retrieveStatementList(user, classStmt).size() != retrieveStatementList(recursive, classStmt).size();
    }

    /**
     * Check that the same number of constructs is used in both methods.
     *
     * @param user
     * @param recursive
     * @return boolean
     * @throws ErrorException
     */
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

    /**
     * Before checking the body of recursive methods, the signature is checked,
     * so that a method can be rejected if it does not have the same signature,
     * thus avoiding unnecessary checking of the whole body (method header check).
     *
     * @param user
     * @param recursive
     * @return boolean
     */
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

    protected static String retrieveStringExpression(Expression expression) {
        return expression.toString().trim();
    }

    protected static BinaryExpr retrieveBinaryExpression(Expression expression) {
        return expression.asBinaryExpr();
    }

    // ZONE - Recursive call arguments
    public static boolean checkRecursiveCallArguments(MethodDeclaration user, MethodDeclaration recursive) {
        NodeList<Expression> userArgumentsList = getRecursiveMethodCall(user).getArguments();
        NodeList<Expression> recursiveArgumentsList = getRecursiveMethodCall(recursive).getArguments();

        return iterativeListsFlow(userArgumentsList.stream(), recursiveArgumentsList.stream())
                .anyMatch(pair -> Boolean.TRUE == !compareElementContent(user, recursive, pair.getKey().toString(), pair.getValue().toString()));
    }

    protected static int countStringMatches(String initialization, String matcher) {
        return StringUtils.countMatches(initialization, matcher);
    }

    private static List<String> retrieveConditionsList(String condition, int countAND, int countOR) {
        int count = countAND + (countOR * 2) + 1;
        return Arrays.asList(splitWithLimit(condition, "&&|\\|", count));
    }

    /**
     * It is used to handle single or multiple conditions (in case there are logical operators).
     *
     * @param user
     * @param recursive
     * @param userCondition
     * @param recursiveCondition
     * @return boolean
     */
    protected static boolean compareConditionsElements(MethodDeclaration user, MethodDeclaration recursive, String userCondition, String recursiveCondition) {
        int userCountAND = countStringMatches(userCondition, "&&");
        int userCountOR = countStringMatches(userCondition, "||");

        if (userCountAND != countStringMatches(recursiveCondition, "&&") ||
                userCountOR != countStringMatches(recursiveCondition, "||")) {
            return false;
        }

        if (userCountAND != 0 || userCountOR != 0) {
            List<String> userList = retrieveConditionsList(userCondition, userCountAND, userCountOR);
            List<String> recursiveList = retrieveConditionsList(recursiveCondition, userCountAND, userCountOR);

            return iterativeListsFlow(userList.stream(), recursiveList.stream())
                    .filter(pair -> StringUtils.isNotBlank(pair.getKey()) && StringUtils.isNotBlank(pair.getValue()))
                    .anyMatch(pair -> Boolean.TRUE == compareElementContent(user, recursive, pair.getKey(), pair.getValue()));
        }
        return compareElementContent(user, recursive, userCondition, recursiveCondition);
    }

    /**
     * Verify that the elements passed are: method signature parameters, local variables, or simply values.
     *
     * @param user
     * @param recursive
     * @param userElement
     * @param recursiveElement
     * @return boolean
     */
    private static boolean compareMethodsElements(MethodDeclaration user, MethodDeclaration recursive, String userElement, String recursiveElement) {
        int recursiveIndex = getIndexParameter(recursive, recursiveElement);
        VariableDeclarator userVariable = findVariable(user, userElement);
        VariableDeclarator recursiveVariable = findVariable(recursive, recursiveElement);

        if (recursiveIndex != getIndexParameter(user, userElement) ||
                (recursiveVariable == null && userVariable != null) ||
                (recursiveVariable != null && userVariable == null)) {
            System.out.println("Error getIndex or not both variable!");
            return false;
        }

        if (recursiveIndex == -1 && recursiveVariable == null && !StringUtils.equals(userElement, recursiveElement)) {
            System.out.println("Error recognize nature both variable");
            return false;
        }

        if (recursiveVariable != null && !verifyVariableContent(user, recursive, userVariable, recursiveVariable)) {
            System.out.println("Error different variable content!");
            return false;
        }
        return true;
    }

    protected static String retrieveInitializationValue(String string) {
        return StringUtils.split(string, "=")[1].trim();
    }

    /**
     * Checks the type and content of the variables of the compared methods.
     *
     * @param user
     * @param recursive
     * @param userVariable
     * @param recursiveVariable
     * @return boolean
     */
    private static boolean verifyVariableContent(MethodDeclaration user, MethodDeclaration recursive, VariableDeclarator userVariable, VariableDeclarator recursiveVariable) {
        if (!StringUtils.equals(retrieveVariableType(userVariable), retrieveVariableType(recursiveVariable))) {
            return false;
        }

        return compareElementContent(user, recursive, retrieveInitializationValue(userVariable.toString()),
                retrieveInitializationValue(recursiveVariable.toString()));
    }

    protected static String retrieveVariableType(VariableDeclarator variable) {
        return variable.getType().toString();
    }

    /**
     * Returns the index of the parameter passed as input to this method from the parameter list extracted from the method signature.
     * Returns -1 in case the parameter passed in is not actually a parameter in the signature of the method being parsed.
     *
     * @param method
     * @param element
     * @return int
     */
    private static int getIndexParameter(MethodDeclaration method, String element) {
        for (int i = 0; i < method.getParameters().size(); i++) {
            if (method.getParameter(i).getNameAsString().equals(element))
                return i;
        }
        return -1;
    }

    /**
     * Returns the entire declaration of the local variable to the method if it is found within the method, otherwise returns null.
     *
     * @param method
     * @param element
     * @return VariableDeclarator
     */
    private static VariableDeclarator findVariable(MethodDeclaration method, String element) {
        return CollectionUtils.emptyIfNull(method.findAll(VariableDeclarator.class))
                .stream()
                .filter(variable -> StringUtils.equals(variable.getNameAsString(), element))
                .findFirst()
                .orElse(null);
    }

    private static ArrayAccessExpr retrieveArrayAccessExpression(Expression expression) {
        return expression.asArrayAccessExpr();
    }

    /**
     * Checks the arrays passed in, comparing both the array itself (by taking its name and looking for its declaration within the method) and the indices passed in.
     * It will return false if the declarations of the two arrays or the indices passed to them are different.
     *
     * @param user
     * @param recursive
     * @param userElement
     * @param recursiveElement
     * @return boolean
     */
    private static boolean verifyArrayContent(MethodDeclaration user, MethodDeclaration recursive, Expression userElement, Expression recursiveElement) {
        ArrayAccessExpr userAccess = retrieveArrayAccessExpression(userElement);
        ArrayAccessExpr recursiveAccess = retrieveArrayAccessExpression(recursiveElement);
        if (!(compareMethodsElements(user, recursive,
                userAccess.getName().toString(), recursiveAccess.getName().toString()))) {
            System.out.println("Error to two NameExpr!");
            return false;
        }
        if (!(compareElementContent(user, recursive,
                userAccess.getIndex().toString(), recursiveAccess.getIndex().toString()))) {
            System.out.println("Error in indexes passed to arrays!");
            return false;
        }

        return true;
    }

    private static FieldAccessExpr retrieveFieldAccessExpression(Expression expression) {
        return expression.asFieldAccessExpr();
    }

    /**
     * Checks in case the elements are of type: "array.length", then checks both the element calling the field and the field itself.
     * Returns false in case one of the two is different between methods.
     *
     * @param user
     * @param recursive
     * @param userElement
     * @param recursiveElement
     * @return boolean
     */
    private static boolean verifyFieldAccessContent(MethodDeclaration user, MethodDeclaration recursive,
                                                    Expression userElement, Expression recursiveElement) {
        FieldAccessExpr userAccess = retrieveFieldAccessExpression(userElement);
        FieldAccessExpr recursiveAccess = retrieveFieldAccessExpression(recursiveElement);
        if (!(compareMethodsElements(user, recursive,
                userAccess.getScope().toString(), recursiveAccess.getScope().toString()))) {
            System.out.println("Different FieldAccessExpr elements!");
            return false;
        }
        if (!(compareMethodsElements(user, recursive,
                userAccess.getNameAsString(), recursiveAccess.getNameAsString()))) {
            System.out.println("Different FieldAccessExpr!");
            return false;
        }
        return true;
    }

    private static MethodCallExpr retrieveMethodCallExpr(Expression expression) {
        return expression.asMethodCallExpr();
    }

    /**
     * Checks whether there is an element that invokes it or not before the method call, such as: list.size(),
     * where list is the name of a list and size() is the method call that lets you know the size of the list.
     * Returns false if one of them has an element that invokes the method and the other does not.
     *
     * @param expression
     * @return boolean
     */
    private static boolean checkIsPresentScope(Expression expression) {
        return retrieveMethodCallExpr(expression).getScope().isPresent();
    }

    private static NodeList<Expression> retrieveMethodCallArguments(Expression expression) {
        return retrieveMethodCallExpr(expression).getArguments();
    }

    private static String retrieveScope(Expression expression) {
        return retrieveMethodCallExpr(expression).getScope().get().toString();
    }

    private static boolean checkMethodCallCases(MethodDeclaration user, MethodDeclaration recursive,
                                                Expression userExpression, Expression recursiveExpression) {
        if (checkIsPresentScope(userExpression) != checkIsPresentScope(recursiveExpression)) {
            System.out.println("Scope not present in both MethodCalls!");
            return false;
        }

        if (checkIsPresentScope(userExpression) && checkIsPresentScope(recursiveExpression)) {
            if (!compareElementContent(user, recursive, retrieveScope(userExpression), retrieveScope(recursiveExpression))) {
                System.out.println("Different scopes when comparing MethodCallExpr!");
                return false;
            }
        }

        if (!StringUtils.equals(retrieveMethodCallExpr(userExpression).getNameAsString(),
                retrieveMethodCallExpr(recursiveExpression).getNameAsString())) {
            System.out.println("Different method names in MethodCallExpr!");
            return false;
        }

        NodeList<Expression> userList = retrieveMethodCallArguments(userExpression);
        NodeList<Expression> recursiveList = retrieveMethodCallArguments(recursiveExpression);

        if (userList.size() != recursiveList.size()) {
            System.out.println("Different number of arguments in MethodCallExpr!");
            return false;
        }

        if (userList.isEmpty()) {
            return true;
        }
        return iterativeListsFlow(userList.stream(), recursiveList.stream())
                .anyMatch(pair -> Boolean.TRUE == compareElementContent(user, recursive,
                        pair.getKey().toString(), pair.getValue().toString()));
    }

    private static boolean checkNotBinaryCases(MethodDeclaration user, MethodDeclaration recursive,
                                               String userVariable, String recursiveVariable) {
        Expression userExpression = retrieveExpression(userVariable);
        Expression recursiveExpression = retrieveExpression(recursiveVariable);

        if (userExpression.isNameExpr() && recursiveExpression.isNameExpr()) {
            if (!compareMethodsElements(user, recursive, retrieveStringExpression(userExpression),
                    retrieveStringExpression(recursiveExpression))) {
                System.out.println("Error to two NameExpr!");
                return false;
            }
            return true;
        }

        if (userExpression.isArrayAccessExpr() && recursiveExpression.isArrayAccessExpr()) {
            return verifyArrayContent(user, recursive, userExpression, recursiveExpression);
        }

        if (userExpression.isFieldAccessExpr() && recursiveExpression.isFieldAccessExpr()) {
            return verifyFieldAccessContent(user, recursive, userExpression, recursiveExpression);
        }

        if (userExpression.isMethodCallExpr() && recursiveExpression.isMethodCallExpr()) {
            return checkMethodCallCases(user, recursive, userExpression, recursiveExpression);
        }

        if (!StringUtils.equals(userVariable, recursiveVariable)) {
            System.out.println("Error in the comparison between the values of the variables");
            return false;
        }
        return true;
    }

    private static boolean checkBinaryCases(MethodDeclaration user, MethodDeclaration recursive,
                                            Expression userExpression, Expression recursiveExpression) {
        if (retrieveBinaryExpression(userExpression).getOperator() !=
                retrieveBinaryExpression(recursiveExpression).getOperator()) {
            System.out.println("Different operator!");
            return false;
        }

        if (!compareElementContent(user, recursive, isBinary(userExpression.toString(), true),
                isBinary(recursiveExpression.toString(), true))) {
            return false;
        }

        return compareElementContent(user, recursive, isBinary(userExpression.toString(), false),
                isBinary(recursiveExpression.toString(), false));
    }

    /**
     * Fundamental method for comparing the elements to be analyzed.
     *
     * @param user
     * @param recursive
     * @param userVariable
     * @param recursiveVariable
     * @return boolean
     */
    protected static boolean compareElementContent(MethodDeclaration user, MethodDeclaration recursive,
                                                   String userVariable, String recursiveVariable) {
        Expression userExpression = retrieveExpression(userVariable);
        Expression recursiveExpression = retrieveExpression(recursiveVariable);

        if (checkDifferentMetaModel(userExpression, recursiveExpression)) {
            System.out.println("Error MetaModel!");
            return false;
        }

        if (!userExpression.isBinaryExpr() && !recursiveExpression.isBinaryExpr()) {
            return checkNotBinaryCases(user, recursive, userVariable, recursiveVariable);
        }
        return checkBinaryCases(user, recursive, userExpression, recursiveExpression);
    }

}
