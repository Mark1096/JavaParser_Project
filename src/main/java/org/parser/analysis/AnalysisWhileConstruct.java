package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.parser.error.ErrorException;

import java.util.List;

public class AnalysisWhileConstruct extends AnalysisMethod {

    // Metodo che confronta contemporaneamente le condizioni delle liste nel caso di WhileStmt
    private static boolean checkWhileCondition(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<WhileStmt> list1 = retrieveStatementList(user, WhileStmt.class);
        List<WhileStmt> list2 = retrieveStatementList(recursive, WhileStmt.class);

        return iterativeListsFlow(list1.stream(), list2.stream())
                .anyMatch(pair -> checkConditionsElements(user, recursive, pair.getKey().getCondition(), pair.getValue().getCondition()));
    }

    // Metodo che garantisce che le liste non siano vuote e confronti le condizioni degli WhileStmt
    public boolean checkStatementList(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        return checkNotEmptyList(user, recursive, WhileStmt.class) && checkWhileCondition(user, recursive);
    }

}
