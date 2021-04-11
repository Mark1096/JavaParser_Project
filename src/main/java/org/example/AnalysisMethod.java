package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;

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
}
