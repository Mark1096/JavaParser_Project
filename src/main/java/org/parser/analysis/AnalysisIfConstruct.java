package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import org.parser.error.ErrorException;

import java.util.List;

public class AnalysisIfConstruct extends AnalysisMethod {
    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di IfStmt
    private static boolean checkIfCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<IfStmt> list1 = retrieveStatementList(user, IfStmt.class);
        List<IfStmt> list2 = retrieveStatementList(recursive, IfStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli IfStmt
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, IfStmt.class) && checkIfCondition(user, recursive);
    }
}
