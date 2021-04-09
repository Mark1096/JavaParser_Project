package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.apache.commons.collections4.CollectionUtils;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileParserUtils {

    // Estrapolazione di tutti i metodi contenuti nella classe passata all'interno del file
    // e salvataggio di tutti e soli i metodi ricorsivi all'interno di una lista
    public static List<MethodDeclaration> getRecursiveUserMethodList() throws FileNotFoundException {
        CompilationUnit cu = Singleton.getInstance();  // Analisi e salvataggio del contenuto del file

        return CollectionUtils.emptyIfNull(cu.findAll(MethodDeclaration.class))
                .stream()
                .filter(element -> getRecursiveMethodCall(element) != null)
                .collect(Collectors.toList());
    }

    // TODO: Da rivedere
    public static List<MethodDeclaration> getAllUserMethodList() throws FileNotFoundException {
        return retrieveCompilationUnit().findAll(MethodDeclaration.class);
    }

    public static CompilationUnit retrieveCompilationUnit() throws FileNotFoundException {
        return Singleton.getInstance();
    }

    // Restituisce tutti e soli i metodi ricorsivi che vengono trovati all'interno del corpo del metodo passato in input
    public static MethodCallExpr getRecursiveMethodCall(MethodDeclaration methodDeclaration) {
        MethodCallExpr result = null;
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
                List argumentsType = new ArrayList();

                // Ciclo che itera per ogni argomento trovato nella chiamata a metodo
                arguments.forEach(arg -> {
                    String argument = arg.toString();
                    Expression element = StaticJavaParser.parseExpression(argument);

                    if (element.isBinaryExpr())
                        argument = element.asBinaryExpr().getLeft().toString().trim();

                    List<VariableDeclarationExpr> methodVariables = methodDeclaration.getBody().get().findAll(VariableDeclarationExpr.class);

                    // Estrapolazione del tipo dell'argomento passato alla chiamata del metodo, cercando la dichiarazione della variabile (se esiste) avente lo stesso nome dell'argomento.
                    // Se il nome dell'argomento non si trova tra le variabili locali, apparterrà sicuramente ad uno dei parametri passati nella firma del metodo
                    for (VariableDeclarationExpr variable : methodVariables) {
                        if (variable.getVariable(0).getNameAsString().equals(argument)) {
                            argumentsType.add(variable.getVariable(0).getType());
                        }
                    }

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

                boolean sameParameters = true;

                // Verifica se i tipi ricavati dagli argomenti della chiamata sono uguali a quelli passati nella firma del metodo (viene considerato anche l'ordine con il quale sono passati)
                for (int i = 0; i < parametersType.size(); i++) {
                    if (!(parametersType.get(i).toString().equals(argumentsType.get(i).toString()))) {
                        sameParameters = false;
                        break;
                    }
                }

                // Se tutti i controlli hanno dato esito significa che tutti i tipi degli argomenti passati
                // alla chiamata del metodo sono corretti e ordinati allo stesso modo dei parametri passati
                // nella firma del metodo, quindi il metodo è effettivamente ricorsivo.
                if (sameParameters) {
                    result = method;
                    break;
                }
            }
        }

        return result;
    }

    public static void updateUserFile() throws IOException {
        // Aggiornamento del vecchio contenuto del file utente con quello nuovo.
        FileWriter fooWriter = new FileWriter(Singleton.getFile(), false);
        fooWriter.write(retrieveCompilationUnit().toString());
        fooWriter.close();
    }
}
