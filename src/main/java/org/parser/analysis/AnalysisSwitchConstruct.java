package org.parser.analysis;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.SwitchStmt;
import javafx.util.Pair;
import org.parser.error.ErrorException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> AnalysisSwitchConstruct </h1>
 * <p>
 * This class parses everything about the Switch conditional construct,
 * providing methods that check for all cases within the construct and the provided selector.
 */
public class AnalysisSwitchConstruct extends AnalysisMethod {

    /**
     * Returns the number of cases within the Switch.
     *
     * @param switchStmt the switch stmt
     * @return int
     */
    private static int retrieveSwitchNumberEntries(SwitchStmt switchStmt) {
        return switchStmt.getEntries().size();
    }

    /**
     * Returns the name of the last case of the Switch passed in as input.
     *
     * @param switchStmt the switch stmt
     * @return node list expression
     */
    private static NodeList<Expression> retrieveSwitchLabel(SwitchStmt switchStmt) {
        return switchStmt.getEntry(retrieveSwitchNumberEntries(switchStmt) - 1).getLabels();
    }

    /**
     * It checks if one of the two Switches passed in input contains the case "default" and the other does not.
     *
     * @param switch1 the switch 1
     * @param switch2 the switch 2
     * @return boolean
     */
    private static boolean checkSwitchLabelEmpty(SwitchStmt switch1, SwitchStmt switch2) {
        return checkEmptyAndNotLists(retrieveSwitchLabel(switch1), retrieveSwitchLabel(switch2)) ||
                checkEmptyAndNotLists(retrieveSwitchLabel(switch2), retrieveSwitchLabel(switch1));
    }

    /**
     * Check that the conditions and Switch cases of both methods are the same.
     *
     * @param user the user
     * @param recursive the recursive
     * @return list
     * @throws ErrorException the error exception
     */
    private static List<Pair<SwitchStmt, SwitchStmt>> checkSwitchProperties(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<SwitchStmt> list1 = retrieveStatementsList(user, SwitchStmt.class);
        List<SwitchStmt> list2 = retrieveStatementsList(recursive, SwitchStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .filter(pair -> !checkElementContent(user, recursive, pair.getKey().getSelector(), pair.getValue().getSelector()))
                .filter(pair -> retrieveSwitchNumberEntries(pair.getKey()) == retrieveSwitchNumberEntries(pair.getValue()))
                .filter(pair -> !checkSwitchLabelEmpty(pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the name of the case indicated by the index of the Switch passed in input.
     *
     * @param switchStmt the switch stmt
     * @param index the index
     * @return string
     */
    private static String retrieveSwitchCaseName(SwitchStmt switchStmt, int index) {
        return switchStmt.getEntry(index).getLabels().get(0).toString();
    }

    /**
     * Returns the number of cases within the Switch, excluding the "default" case, if exists.
     *
     * @param pair the pair
     * @return int
     */
    private static int retrieveSwitchNumberCase(Pair<SwitchStmt, SwitchStmt> pair) {
        return checkEmptyLists(retrieveSwitchLabel(pair.getKey()), retrieveSwitchLabel(pair.getValue())) ?
                retrieveSwitchNumberEntries(pair.getKey()) - 1 : retrieveSwitchNumberEntries(pair.getKey());
    }

    /**
     * Checks that all elements representing the Switch cases are the same in both methods passed in as input.
     *
     * @param user the user
     * @param recursive the recursive
     * @param pair the pair
     * @return boolean
     */
    private static boolean checkSwitchCaseContent(MethodDeclaration user, MethodDeclaration recursive, Pair<SwitchStmt, SwitchStmt> pair) {
        for (int i = 0; i < retrieveSwitchNumberCase(pair); i++) {
            if (!compareElementContent(user, recursive,
                    retrieveSwitchCaseName(pair.getKey(), i), retrieveSwitchCaseName(pair.getValue(), i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares the contents of the Switch conditions of the two methods passed as input and checks for a match.
     *
     * @param user the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean checkSwitchCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<Pair<SwitchStmt, SwitchStmt>> result = checkSwitchProperties(user, recursive);
        if (result.isEmpty()) {
            return true;
        }
        return result.stream()
                .anyMatch(pair -> Boolean.TRUE == checkSwitchCaseContent(user, recursive, pair));
    }

    /**
     * Check that the lists are not empty and compares the conditions of the switch construct.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyLists(user, recursive, SwitchStmt.class) && checkSwitchCondition(user, recursive);
    }

}
