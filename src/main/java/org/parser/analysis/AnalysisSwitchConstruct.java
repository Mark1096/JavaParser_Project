package org.parser.analysis;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.SwitchStmt;
import javafx.util.Pair;
import org.parser.error.ErrorException;

import java.util.List;
import java.util.stream.Collectors;

public class AnalysisSwitchConstruct extends AnalysisMethod {

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

    private static boolean checkSwitchCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<SwitchStmt, SwitchStmt>> result = checkSwitchProperties(user, recursive);
        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == checkSwitchCaseContent(user, recursive, pair));
    }

    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, SwitchStmt.class) && checkSwitchCondition(user, recursive);
    }

}
