package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.AnalysisIterativeMethod.replaceRecursiveWithIterativeMethod;
import static org.example.AnalysisRecursiveMethod.getRecursiveMethodCall;
import static org.example.AnalysisRecursiveMethod.retrieveRecursiveFile;
import static org.example.AnalysisStatementConstructs.*;
import static org.example.FileParserUtils.*;

public class App {

    // Restituisce l'indice del parametro passato in input a questo metodo dalla lista dei parametri estrapolata dalla firma del metodo.
    // Restituisce -1 nel caso in cui il parametro passato in input non sia effettivamente un parametro della firma del metodo che si sta analizzando.
    public static int getIndexParameter(MethodDeclaration methodDeclaration, String variable) {
        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
            if (methodDeclaration.getParameter(i).getNameAsString().equals(variable))    // confronto tra il nome del parametro i-esimo e quello passato in input
                return i;   // ritorna l'indice del parametro della lista se il suo nome corrisponde a quello del parametro passato in input
        }
        return -1;
    }

    // Restituisce l'intera dichiarazione della variabile locale al metodo se viene trovata all'interno di quest'ultimo, altrimenti restituisce null.
    public static VariableDeclarator findVariable(MethodDeclaration methodDeclaration, String variable) {
        VariableDeclarator ris = null;
        List<VariableDeclarator> listVariable = methodDeclaration.findAll(VariableDeclarator.class); // Estrapolo tutte le dichiarazioni di variabili dal metodo per fare i confronti

        for (VariableDeclarator var : listVariable) {
            if (var.getNameAsString().equals(variable)) {
                ris = var;  // settaggio della variabile da restituire nel caso in cui ci sia una corrispondenza di nomi tra una variabile della lista e quella passata in input
            }
        }

        return ris;
    }

    // Verifica degli array passati in input, confrontando sia l'array in sè (prendendone il nome e cercando la sua dichiarazione all'interno del metodo)
    // sia gli indici passati
    // Verrà ritornato false se le dichiarazioni dei due array o gli indici passati ad essi sono differenti.
    public static boolean verifyArrayContent(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                             Expression userElement, Expression element) {
        boolean ris = true;

        // Estrapolazione dei nomi dei due array da confrontare
        String nameVecUser = userElement.asArrayAccessExpr().getName().toString();
        String nameVec = element.asArrayAccessExpr().getName().toString();

        // Vengono passati i nomi dei due array per fare una ricerca (per nome) della loro dichiarazione (che dovrà essere uguale) all'interno del metodo.
        if (!(compareMethodsElements(userMethod, recursive_method, nameVecUser, nameVec))) {
            System.out.println("Error to two NameExpr!");
            return false;
        }

        // Qui vengono estrapolati gli indici passati ad entrambi gli array
        String userVecIndex = userElement.asArrayAccessExpr().getIndex().toString();
        String vecIndex = element.asArrayAccessExpr().getIndex().toString();

        // Passaggio degli indici (che possono essere una singola variabile o un'intera espressione formata da valori e altre variabili) alla chiamata sottostante
        if (!compareElementContent(userMethod, recursive_method, userVecIndex, vecIndex)) {
            System.out.println("Errore negli indici passati agli array!");
            ris = false;
        }

        return ris;
    }

    // Verifica nel caso in cui gli elementi sia del tipo: "array.length", quindi controllare sia l'elemento che richiama il campo sia quest'ultimo
    // Restituisce false nel caso in cui uno dei due sia diverso tra un metodo e l'altro
    public static boolean verifyFieldAccessContent(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                                   Expression userElement, Expression element) {
        boolean ris = true;

        // Estrapolazione degli elementi che richiamano il campo (per esempio length)
        String nameUserElement = userElement.asFieldAccessExpr().getScope().toString();
        String nameElement = element.asFieldAccessExpr().getScope().toString();

        if (!compareMethodsElements(userMethod, recursive_method, nameUserElement, nameElement)) {
            System.out.println("Elementi di FieldAccessExpr diversi!");
            return false;
        }

        // Estrapolazione dei nomi dei campi per verificare che sia richiamato lo stesso campo in entrambi gli elementi
        String nameUserField = userElement.asFieldAccessExpr().getNameAsString();
        String nameField = element.asFieldAccessExpr().getNameAsString();

        if (!compareMethodsElements(userMethod, recursive_method, nameUserField, nameField)) {
            System.out.println("FieldAccessExpr diversi!");
            ris = false;
        }

        return ris;
    }

    // Metodo principale per il confronto degli elementi da analizzare.
    public static boolean compareElementContent(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                                String userVariableValue, String variableValue) {
        boolean ris = true;

        // Conversione dei parametri passati in input sottoforma di stringa in espressioni. Viene fatto per poter
        // controllare il tipo di elementi che vengono passati (se sono espressioni, singole variabili, valori, parametri, ecc...)
        Expression userElement = StaticJavaParser.parseExpression(userVariableValue);
        Expression element = StaticJavaParser.parseExpression(variableValue);

        // Se ciò che viene prelevato dall'utente è di tipo diverso da quello che viene preso dal mio metodo corrente, ritorna false ed esce dal metodo
        if (userElement.getMetaModel() != element.getMetaModel()) {
            System.out.println("Error MetaModel!");
            return false;
        }

        // Se entrambi gli elementi passati in input in questo metodo non sono espressioni binarie (quindi non contengono somma, sottrazione, ecc..., oppure operatori come il minore, maggiore, uguale, e via dicendo)
        if (!(userElement.isBinaryExpr()) && !(element.isBinaryExpr())) {
            boolean isValue = true;

            // se entrambi contengono solo un nome (di una variabile o di un parametro della firma del metodo)
            if (userElement.isNameExpr() && element.isNameExpr()) {
                if (!(compareMethodsElements(userMethod, recursive_method, userElement.toString(), element.toString()))) {
                    System.out.println("Error to two NameExpr!");
                    return false;
                }
                isValue = false;
            }

            // se entrambi sono array
            if (userElement.isArrayAccessExpr() && element.isArrayAccessExpr()) {
                if (!verifyArrayContent(userMethod, recursive_method, userElement, element)) return false;
                isValue = false;
            }

            // se entrambi sono della forma: "elemento.campo" (tipo: array.length)
            if (userElement.isFieldAccessExpr() && element.isFieldAccessExpr()) {
                // controllo delle dichiarazioni dei due array e degli indici passati
                if (!verifyFieldAccessContent(userMethod, recursive_method, userElement, element))
                    return false;
                isValue = false;
            }

            // se entrambi contengono chiamate a metodi
            if (userElement.isMethodCallExpr() && element.isMethodCallExpr()) {

                // Controllo importante per verificare se prima della chiamata al metodo esiste un elemento che invoca o no,
                // tipo: list.size(), dove list è il nome di una lista e size() è la chiamata al metodo che permette di conoscere la dimensione della lista.
                // Restituisce false nel caso in cui uno dei due abbia un elemento che invoca il metodo e l'altro no.
                if (userElement.asMethodCallExpr().getScope().isPresent() != element.asMethodCallExpr().getScope().isPresent()) {
                    System.out.println("Scope non presente in entrambi i MethodCall!");
                    return false;
                }

                // Nel caso in cui entrambi abbiano un elemento che invoca il metodo
                if (userElement.asMethodCallExpr().getScope().isPresent() && element.asMethodCallExpr().getScope().isPresent()) {
                    // Estrapolazione degli elementi che invocano i metodo
                    String userScope = userElement.asMethodCallExpr().getScope().get().toString();
                    String scope = element.asMethodCallExpr().getScope().get().toString();

                    // Passaggio di questi due elementi al metodo per confrontarli
                    if (!compareElementContent(userMethod, recursive_method, userScope, scope)) {
                        System.out.println("Scope diversi nel confronto tra MethodCallExpr!");
                        return false;
                    }
                }

                // Controllo del nome dei due metodi richiamati, che deve essere lo stesso, altrimenti restituisce false.
                if (!(userElement.asMethodCallExpr().getNameAsString().equals(element.asMethodCallExpr().getNameAsString()))) {
                    System.out.println("Nomi di metodi diversi nelle MethodCallExpr!");
                    return false;
                }

                // Estrapolazione del numero dei possibili argomenti passati in entrambi i metodi, in modo da poterli confrontare
                int userNumArgs = userElement.asMethodCallExpr().getArguments().size();
                int numArgs = element.asMethodCallExpr().getArguments().size();

                // Se il numero è diverso si ritorna false e si esce
                if (userNumArgs != numArgs) {
                    System.out.println("Numero diverso di argomenti nelle MethodCallExpr!");
                    return false;
                }

                // In questo punto è stato appurato che il numero degli argomenti è lo stesso in entrambi i metodi.
                // Adesso bisogna capire se entrambi non contengono proprio argomenti (e in quel caso non ci sarebbe niente da confrontare) oppure sì.
                if (userNumArgs != 0) {
                    String userArg;
                    String arg;
                    // Ciclo che permette d'iterare la lista di argomenti in entrambi i metodi e confrontare argomento per argomento
                    for (int i = 0; i < userNumArgs; i++) {
                        userArg = userElement.asMethodCallExpr().getArgument(i).toString();
                        arg = element.asMethodCallExpr().getArgument(i).toString();
                        if (!compareElementContent(userMethod, recursive_method, userArg, arg)) {
                            System.out.println("Argomenti diversi tra MethodCallExpr!");
                            return false;
                        }
                    }
                }
                isValue = false;
            }

            // La variabile "isValue" rimane true, a questo punto, se non corrisponde a nessuno dei tipi precedenti
            if (isValue) {
                // Non essendo nessuno dei tipi precedenti saranno sicuramente due valori, quindi confronta direttamente i valori e resituisce false nel caso in cui siano diversi
                if (!(userVariableValue.equals(variableValue))) {
                    System.out.println("Compare value variable!");
                    ris = false;
                } else return true;
            }
        } else {
            // Siamo nel caso nel quale entrambi sono espressioni, quindi innanzitutto si confrontano gli operatori utilizzati
            if (userElement.asBinaryExpr().getOperator() != element.asBinaryExpr().getOperator()) {
                System.out.println("Different operator");
                return false;
            }

            // Se gli operatori sono gli stessi si passa all'estrapolazione degli elementi a sinistra e a destra di quest'ultimi, per poi confrontarli
            String leftUserExpression = userElement.asBinaryExpr().getLeft().toString();
            String leftExpression = element.asBinaryExpr().getLeft().toString();
            String rightUserExpression = userElement.asBinaryExpr().getRight().toString();
            String rightExpression = element.asBinaryExpr().getRight().toString();

            // Qui si controllano gli elementi passati alla sinistra dell'operatore
            if (!compareElementContent(userMethod, recursive_method, leftUserExpression, leftExpression)) {
                ris = false;
            }
            // Qui gli elementi passati alla destra dell'operatore
            if (!compareElementContent(userMethod, recursive_method, rightUserExpression, rightExpression)) {
                ris = false;
            }
        }
        return ris;
    }

    // Verifica il tipo e il contenuto delle variabili dei metodi a confronto
    public static boolean verifyVariableContent(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                                VariableDeclarator userVariable, VariableDeclarator variable) {
        boolean result = true;

        // Verifica che il tipo delle due variabili sia lo stesso
        if (!userVariable.getType().toString().equals(variable.getType().toString())) {
            System.out.println("Variabili di tipo differente!");
            return false;
        }

        // Estrapolazione contenuto variabili nell'inizializzazione
        String userVariableValue = userVariable.toString().split("=")[1].trim();
        String variableValue = variable.toString().split("=")[1].trim();

        // Confronto contenuto variabili
        if (!compareElementContent(userMethod, recursive_method, userVariableValue, variableValue))
            result = false;

        return result;
    }

    // Verifica se gli elementi passati siano: parametri della firma del metodo, variabili locali o semplicemente valori.
    public static boolean compareMethodsElements(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                                 String userElement, String element) {
        boolean result = true;
        int userIndex = getIndexParameter(userMethod, userElement);
        int index = getIndexParameter(recursive_method, element);
        VariableDeclarator userVariable = findVariable(userMethod, userElement);
        VariableDeclarator variable = findVariable(recursive_method, element);

        // Se gli elementi sono parametri della firma del metodo ma diversi, cioè uno è per esempio il primo parametro della firma e l'altro il secondo, ritorna false
        // Se uno è un parametro e l'altro no ritorna pure false
        // Se uno è una variabile locale del metodo e l'altro no ritorna false
        if (index != userIndex || (variable == null && userVariable != null) || (variable != null && userVariable == null)) {
            System.out.println("Error getIndex or not both variable!");
            return false;
        }
        // Se gli elementi non sono nè parametri nè variabili si confrontano direttamente poichè sono valori di qualche tipo
        if ((index == -1 && userIndex == -1) && (variable == null && userVariable == null) && (!(userElement.equals(element)))) {
            System.out.println("Error recognize nature both variable");
            return false;
        }
        // Se gli elementi sono entrambi variabili locali si richiama un altro metodo che ne confronta il tipo e il contenuto
        if (variable != null && userVariable != null) {
            if (!verifyVariableContent(userMethod, recursive_method, userVariable, variable)) {
                System.out.println("Error different variable content!");
                result = false;
            }
        }

        return result;
    }

    // Serve a gestire le condizioni singole o multiple (nel caso in cui ci siano degli operatori logici)
    public static boolean compareConditionsElements(MethodDeclaration userMethod, MethodDeclaration recursive_method,
                                                    String userCondition, String condition) {
        // liste che conterranno tutte le condizioni passate in entrambi i metodi
        List<String> totalUserCondition = new ArrayList();
        List<String> totalCondition = new ArrayList();

        // Conteggio del numero di operatori logici, sia AND che OR
        int userCountAND = StringUtils.countMatches(userCondition, "&&");
        int userCountOR = StringUtils.countMatches(userCondition, "||");
        int countAND = StringUtils.countMatches(condition, "&&");
        int countOR = StringUtils.countMatches(condition, "||");

        // Se il numero di AND o il numero di OR non corrisponde, ritorna false
        if ((userCountAND != countAND) || (userCountOR != countOR))
            return false;

        // Se il numero di AND e OR corrisponde ed è diverso da 0 si splitta l'intera stringa, in modo tale da inserire tutte le condizioni all'interno delle liste
        if (userCountAND != 0 || userCountOR != 0) {
            int numLogicalOperator = userCountAND + (userCountOR * 2) + 1;
            String[] splitUserCondition = userCondition.split("&&|\\|", numLogicalOperator);
            String[] splitCondition = condition.split("&&|\\|", numLogicalOperator);
            totalUserCondition = Arrays.asList(Arrays.stream(splitUserCondition).filter(x -> StringUtils.isNotBlank(x)).toArray(String[]::new));
            totalCondition = Arrays.asList(Arrays.stream(splitCondition).filter(x -> StringUtils.isNotBlank(x)).toArray(String[]::new));
        } else {
            // Se non ci sono operatori logici in entrambe le stringhe significa che ci sarà un'unica condizione, quindi s'inserisce semplicemente quella nelle liste
            totalUserCondition.add(userCondition);
            totalCondition.add(condition);
        }

        boolean checkCondition = true;

        // Ciclo che serve ad iterare e controllare ogni singola condizione delle liste
        for (int index = 0; index < totalUserCondition.size(); index++) {

            String userConditionExpr = totalUserCondition.get(index);
            String conditionExpr = totalCondition.get(index);

            if (!compareElementContent(userMethod, recursive_method, userConditionExpr, conditionExpr)) {
                System.out.println("Le condizioni correnti " + userConditionExpr + " e " + conditionExpr + " sono differenti!");
                checkCondition = false;
                break;
            }
        }
        return checkCondition;
    }


    public static void main(String[] args) throws Exception {

        for (File userFile : retrieveUserFileList()) {
            List<MethodDeclaration> listUserRecursiveMethods = FileParserUtils.getRecursiveUserMethodList(userFile);
            File[] algorithmList = retrieveAlgorithmsToExaminedList();


            // Ciclo che itera tutti i metodi ricorsivi trovati nel file dell'utente e restituisce la versione iterativa
            // di tutti e soli ricorsivi dei quali si trova una corrispondenza con le versioni ricorsive disponibili nella directory "Algoritmi".
            for (MethodDeclaration userMethod : listUserRecursiveMethods) {

                // Ciclo che itera ogni directory, contenente l'algoritmo ricorsivo da confrontare con quello dell'utente
                for (File file : algorithmList) {
                    List<File> files = new ArrayList(Arrays.asList(file.listFiles()));  // lista dei file all'interno della directory contenente l'algoritmo corrente

                    // Prelevamento file contenente solo la versione ricorsiva dell'algoritmo da andare a confrontare
                    // Analisi e salvataggio del contenuto del file
                    // prelevamento del primo metodo della classe che sarà proprio quello da confrontare
                    MethodDeclaration recursiveMethod = retrieveApplicationMethods(retrieveRecursiveFile(files));

                    // Prima ancora di verificare il corpo dei metodi ricorsivi viene verificata la firma, in modo tale da
                    // rigettare un metodo già in partenza qualora non dovesse risultare con la stessa firma, evitando così di controllare l'intero corpo inutilmente.
                    // Controllo dell'intestazione dei metodi
                    if (checkMethodSignature(userMethod, recursiveMethod)) {

                        // Verifica che in entrambi i metodi sia utilizzata la stessa quantità di costrutti
                        if (compareSizeLists(userMethod, recursiveMethod)) {
                            System.out.println("Numero di costrutti iterativi o condizionali diverso!");
                            continue;
                        }

                        if (checkIfList(userMethod, recursiveMethod)) {
                            System.out.println("La versione iterativa del seguente metodo ricorsivo non è disponibile: " + recursiveMethod);
                            continue;
                        }

                        if (checkForList(userMethod, recursiveMethod)) {
                            System.out.println("La versione iterativa del seguente metodo ricorsivo non è disponibile: " + recursiveMethod);
                            continue;
                        }

                        if (checkForEachList(userMethod, recursiveMethod)) {
                            System.out.println("La versione iterativa del seguente metodo ricorsivo non è disponibile: " + recursiveMethod);
                            continue;
                        }

                        if (checkWhileList(userMethod, recursiveMethod)) {
                            System.out.println("La versione iterativa del seguente metodo ricorsivo non è disponibile: " + recursiveMethod);
                            continue;
                        }

                        if (checkSwitchList(userMethod, recursiveMethod)) {
                            System.out.println("La versione iterativa del seguente metodo ricorsivo non è disponibile: " + recursiveMethod);
                            continue;
                        }

                        // Se tutti i controlli precedenti sono andati a buon fine, verrà eseguito il codice sottostante, che effettuerà
                        // un ulteriore controllo nel corpo dei metodi.
                        // Estrapolazione delle due chiamate ricorsive dai metodi messi a confronto
                        MethodCallExpr userMethodCall = getRecursiveMethodCall(userMethod);
                        MethodCallExpr methodCall = getRecursiveMethodCall(recursiveMethod);

                        // Estrapolazione degli argomenti da confrontare nelle due chiamate ricorsive
                        NodeList userCallArguments = userMethodCall.getArguments();
                        NodeList callArguments = methodCall.getArguments();

                        boolean sameParameters = true;

                        // Cicla per ogni argomento trovato
                        for (int index = 0; index < userCallArguments.size(); index++) {
                            // Estrapolazione degli argomenti correnti da confrontare
                            String userArgument = userCallArguments.get(index).toString();
                            String argument = callArguments.get(index).toString();

                            // Passaggio degli argomenti da confrontare
                            if (!compareElementContent(userMethod, recursiveMethod, userArgument, argument)) {
                                sameParameters = false;
                                break;
                            }
                        }

                        // Se uno dei confronti tra argomenti dà esito negativo, si ignora tutto il codice sottostante e si passa al prossimo metodo da confrontare
                        if (!sameParameters) {
                            continue;
                        }

                        System.out.println("Stessi argomenti nella chiamata ricorsiva al metodo!");

                        replaceRecursiveWithIterativeMethod(files, userMethod);
                        break;  // Consente di non controllare altri algoritmi disponibili nella directory "Algoritmi", poiché è già stato trovato
                        // quello corrispondente al metodo dell'utente ed è avvenuta con successo la sostituzione del metodo ricorsivo (dell'utente).
                    }
                }
            }

            // TODO: far eseguire il punto 4 se e solo se il punto 3 restituisce un esito positivo (in caso far restituire un booleano)
            // Aggiornamento del vecchio contenuto del file utente con quello nuovo.
            FileParserUtils.updateUserFile();

        }
    }
}