package org.example;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.example.App.compareConditionsElements;
import static org.example.App.compareElementContent;
import static org.example.ErrorCode.generateErrorException;

public class AnalysisStatementConstructs extends AnalysisMethod {

    public static boolean checkMethodSignature(MethodDeclaration user, MethodDeclaration recursive) {
        return checkSizeList(user.getParameters(), recursive.getParameters()) &&
                user.getSignature().getParameterTypes().equals(recursive.getSignature().getParameterTypes()) &&
                user.getType().equals(recursive.getType());
    }

    private static boolean checkSizeList(NodeList<Parameter> list1, NodeList<Parameter> list2) {
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

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di IfStmt
    private static boolean checkIfStatementCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<IfStmt> list1 = retrieveStatementList(user, IfStmt.class);
        List<IfStmt> list2 = retrieveStatementList(recursive, IfStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli IfStmt
    public static boolean checkIfStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, IfStmt.class) && checkIfStatementCondition(user, recursive);
    }

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di WhileStmt
    private static boolean checkWhileStatementCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<WhileStmt> list1 = retrieveStatementList(user, WhileStmt.class);
        List<WhileStmt> list2 = retrieveStatementList(recursive, WhileStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli WhileStmt
    public static boolean checkWhileStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, WhileStmt.class) && checkWhileStatementCondition(user, recursive);
    }

    private static String retrieveForEachTypeVariable(ForEachStmt forEachStmt) {
        return forEachStmt.getVariable().asVariableDeclarationExpr().getVariable(0).getType().toString();
    }

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di ForEachStmt
    private static boolean checkForEachStatementCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ForEachStmt> list1 = retrieveStatementList(user, ForEachStmt.class);
        List<ForEachStmt> list2 = retrieveStatementList(recursive, ForEachStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !StringUtils.equals(retrieveForEachTypeVariable(pair.getKey()), retrieveForEachTypeVariable(pair.getValue())))
                .anyMatch(pair -> checkElementContent(user, recursive, pair.getKey().getIterable(), pair.getValue().getIterable()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli ForEachStmt
    public static boolean checkForEachStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, ForEachStmt.class) && checkForEachStatementCondition(user, recursive);
    }

    private static int retrieveSwitchNumberEntries(SwitchStmt switchStmt) {
        return switchStmt.getEntries().size();
    }

    private static NodeList<Expression> retrieveSwitchLabel(SwitchStmt switchStmt) {
        return switchStmt.getEntry(retrieveSwitchNumberEntries(switchStmt) - 1).getLabels();
    }

    private static boolean checkSwitchLabelEmpty(SwitchStmt switch1, SwitchStmt switch2) {
        return retrieveSwitchLabel(switch1).isEmpty() && retrieveSwitchLabel(switch2).isNonEmpty() ||
                retrieveSwitchLabel(switch1).isNonEmpty() && retrieveSwitchLabel(switch2).isEmpty();
    }

    private static List<Pair<SwitchStmt, SwitchStmt>> checkSwitchStatementProperties(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<SwitchStmt> list1 = retrieveStatementList(user, SwitchStmt.class);
        List<SwitchStmt> list2 = retrieveStatementList(recursive, SwitchStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !checkElementContent(user, recursive, pair.getKey().getSelector(), pair.getValue().getSelector()))
                .filter(pair -> retrieveSwitchNumberEntries(pair.getKey()) == retrieveSwitchNumberEntries(pair.getValue()))
                .filter(pair -> !checkSwitchLabelEmpty(pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
    }

    private static String retrieveSwitchStatementCaseName(SwitchStmt switchStmt, int index) {
        return switchStmt.getEntry(index).getLabels().get(0).toString();
    }

    private static int retrieveSwitchStatementNumberCase(Pair<SwitchStmt, SwitchStmt> pair) {
        return retrieveSwitchLabel(pair.getKey()).isEmpty() && retrieveSwitchLabel(pair.getValue()).isEmpty() ?
                retrieveSwitchNumberEntries(pair.getKey()) - 1 : retrieveSwitchNumberEntries(pair.getKey());
    }

    private static boolean checkSwitchStatementCaseContent(MethodDeclaration user, MethodDeclaration recursive, Pair<SwitchStmt, SwitchStmt> pair) {
        for (int i = 0; i < retrieveSwitchStatementNumberCase(pair); i++) {
            if (!compareElementContent(user, recursive,
                    retrieveSwitchStatementCaseName(pair.getKey(), i), retrieveSwitchStatementCaseName(pair.getValue(), i))) {
                return true;
            }
        }
        return false;
    }

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di SwitchStmt
    private static boolean checkSwitchStatementCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<SwitchStmt, SwitchStmt>> result = checkSwitchStatementProperties(user, recursive);
        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == checkSwitchStatementCaseContent(user, recursive, pair));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli SwitchStmt
    public static boolean checkSwitchStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, SwitchStmt.class) && checkSwitchStatementCondition(user, recursive);
    }

}

