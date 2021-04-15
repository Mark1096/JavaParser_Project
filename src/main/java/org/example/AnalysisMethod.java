package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.List;

import static org.example.ErrorCode.generateErrorException;

public class AnalysisMethod {

    protected AnalysisMethod() {
    }

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

}
