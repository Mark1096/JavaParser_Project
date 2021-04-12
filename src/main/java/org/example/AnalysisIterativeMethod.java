package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.example.ErrorCode.generateErrorException;
import static org.example.FileParserUtils.getAllUserMethodList;
import static org.example.FileParserUtils.retrieveApplicationMethods;

public class AnalysisIterativeMethod extends AnalysisMethod {

    // Se tutti i controlli precedenti hanno dato esito positivo, si può procedere al prelevamento della versione iterativa
    // corrispondente al metodo ricorsivo uguale al metodo dell'utente.
    public static File retrieveIterativeFile(List<File> list, ErrorCode error) throws ErrorException {
        return CollectionUtils.emptyIfNull(list)
                .stream()
                .filter(item -> item.getName().contains("Iterative"))
                .findAny()
                .orElseThrow(() -> generateErrorException(error));
    }

    public static void replaceRecursiveWithIterativeMethod(List<File> files, MethodDeclaration userMethod) throws ErrorException, FileNotFoundException {
        File iterativePath = retrieveIterativeFile(files, ErrorCode.MALFORMED_FILENAME_APPLICATION);
        MethodDeclaration iterative_method = retrieveApplicationMethods(iterativePath);

        // Prima di sostituire il metodo iterativo con quello ricorsivo (dell'utente), viene richiamato il metodo sottostante,
        // in modo tale da mantenere invariati i nomi dei parametri della versione ricorsiva (dell'utente).
        MethodDeclaration newIterativeMethod = replaceMethodParametersName(iterative_method, userMethod);

        // TODO: Rivedere discorso break nel for originario
        MethodDeclaration method = CollectionUtils.emptyIfNull(getAllUserMethodList())
                .stream()
                .filter(element -> StringUtils.equals(element.getDeclarationAsString(), userMethod.getDeclarationAsString()))
                .findFirst()
                .orElseThrow(() -> generateErrorException(ErrorCode.METHODLESS_CLASS_USER));

        method.setParameters(newIterativeMethod.getParameters());
        method.setBody(newIterativeMethod.getBody().get().asBlockStmt());
    }

    // Fa in modo di sostituire alla versione iterativa (da restituire all'utente) tutti i nomi dei parametri formali
    // in modo tale da mantenere i nomi scelti dall'utente nella sua versione ricorsiva. Chiaramente i nomi vengono sostituiti
    // nella versione iterativa sia nella firma del metodo sia nel corpo del metodo, dove vengono utilizzati.
    public static MethodDeclaration replaceMethodParametersName(MethodDeclaration iterativeMethod, MethodDeclaration userMethod) {
        String bodyMethod = iterativeMethod.toString();

        // Ciclo che permette di prelevare ogni singolo parametro dalla firma del metodo iterativo e sostituirlo con il
        // nome del parametro scelto dall'utente nella sua versione ricorsiva.
        for (int i = 0; i < iterativeMethod.getParameters().size(); i++) {
            Parameter iterativeParameter = iterativeMethod.getParameter(i);
            Parameter userParameter = userMethod.getParameter(i);

            if (StringUtils.equals(iterativeParameter.getType().toString(), userParameter.getType().toString())) {
                bodyMethod = bodyMethod.replaceAll("\\b" + iterativeParameter.getNameAsString() + "\\b", userParameter.getNameAsString());
            }
        }

        return StaticJavaParser.parseMethodDeclaration(bodyMethod);
    }

}
