package org.parser.analysis;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.parser.error.ErrorException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AnalysisRecursiveMethod extends AnalysisMethod {

    public static File retrieveRecursiveFile(List<File> files) throws ErrorException {
        return retrieveMethodFile(files, "Recursive");
    }

    // Restituisce tutti e soli i metodi ricorsivi che vengono trovati all'interno del corpo del metodo passato in input
    public static MethodCallExpr getRecursiveMethodCall(MethodDeclaration methodDeclaration) {
        // Estrapola tutte le chiamate a metodi che riesce a trovare nel corpo del metodo
        List<MethodCallExpr> listMethodCall = methodDeclaration.findAll(MethodCallExpr.class);
        // Ciclo che itera per ogni chiamata a metodo trovato
        for (MethodCallExpr method : listMethodCall) {
            NameExpr nameExpr = method.getNameAsExpression();   // nome della chiamata a metodo corrente
            NodeList arguments = method.getArguments(); // numero di argomenti della chiamata a metodo corrente

            // Verifica che il nome della chiamata a metodo trovato sia uguale a quello della firma del metodo.
            // Inoltre controlla anche che il numero di parametri passati ai metodi sia uguale
            if (methodDeclaration.getNameAsExpression().equals(nameExpr) &&
                    methodDeclaration.getParameters().size() == arguments.size()) {

                List parametersType = methodDeclaration.getSignature().getParameterTypes(); // Lista dei tipi dei parametri della firma del metodo
                List argumentsType = retrieveArgumentsType(arguments, methodDeclaration, parametersType);
                boolean sameParameters = true;

                // Verifica se i tipi ricavati dagli argomenti della chiamata sono uguali a quelli passati nella firma del metodo (viene considerato anche l'ordine con il quale sono passati)
                for (int i = 0; i < parametersType.size(); i++) {
                    if (!(StringUtils.equals(parametersType.get(i).toString(), argumentsType.get(i).toString()))) {
                        sameParameters = false;
                        break;
                    }
                }

                // Se tutti i controlli hanno dato esito significa che tutti i tipi degli argomenti passati
                // alla chiamata del metodo sono corretti e ordinati allo stesso modo dei parametri passati
                // nella firma del metodo, quindi il metodo è effettivamente ricorsivo.
                if (sameParameters) {
                    return method;
                }
            }
        }
        return null;
    }

    // Estrapolazione del tipo dell'argomento passato alla chiamata del metodo, cercando la dichiarazione della variabile (se esiste) avente lo stesso nome dell'argomento.
    // Se il nome dell'argomento non si trova tra le variabili locali, apparterrà sicuramente ad uno dei parametri passati nella firma del metodo
    private static List<VariableDeclarationExpr> retrieveMethodArgumentsList(List<VariableDeclarationExpr> methodVariables, String argument) {
        return CollectionUtils.emptyIfNull(methodVariables)
                .stream()
                .filter(variable -> StringUtils.equals(variable.getVariable(0).getNameAsString(), argument))
                .collect(Collectors.toList());
    }

    // TODO: sistemare questo metodo, considerando il caso nel quale venga passata una costante come argomento della chiamata (al momento fa crashare il programma)
    private static List retrieveArgumentsType(NodeList arguments, MethodDeclaration methodDeclaration, List parametersType) {
        List argumentsType = new ArrayList();
        CollectionUtils.emptyIfNull(arguments)
                .forEach(arg -> {
                    String argument = isBinary(arg.toString());
                    List<VariableDeclarationExpr> methodVariables =
                            methodDeclaration.getBody().get().findAll(VariableDeclarationExpr.class);

                    CollectionUtils.emptyIfNull(retrieveMethodArgumentsList(methodVariables, argument))
                            .stream()
                            .findFirst()
                            .map(element -> argumentsType.add(element.getVariable(0).getType()));

                    // Verifica se l'argomento corrente sia già stato riconosciuto come variabile oppure no.
                    // Nel primo caso il controllo sottostante darà esito negativo e non verrà controllato
                    // se effettivamente l'argomento corrente possa essere un parametro perché già è stato
                    // riconosciuto come variabile e preso il suo tipo. Nel secondo caso bisogna andare a controllare
                    // se l'argomento corrente (non essendo una variabile locale) sia un parametro della firma del metodo
                    // e in caso positivo prelevare il suo tipo.
                    // Solo nel secondo caso si entra nella condizione e si verifica il corpo.
                    if (argumentsType.size() == arguments.indexOf(arg)) {
                        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                            if (methodDeclaration.getParameter(i).getNameAsString().equals(argument)) {
                                argumentsType.add(parametersType.get(i));
                            }
                        }
                    }
                });
        return argumentsType;
    }

}
