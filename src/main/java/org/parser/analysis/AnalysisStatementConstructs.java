package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;
import org.parser.analysis.creator.*;
import org.parser.error.ErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisStatementConstructs {

    public static boolean checkAllConstruct(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ConstructCreator> creator = addConstructCreator();
        List<AnalysisMethod> analysisMethods = setAllConstruct(creator);

        return iterateAllConstruct(user, recursive, analysisMethods);
    }

    private static List<ConstructCreator> addConstructCreator() {
        return Arrays.asList(
                new CreatorIfConstruct(),
                new CreatorWhileConstruct(),
                new CreatorForConstruct(),
                new CreatorForEachConstruct(),
                new CreatorSwitchConstruct()
        );
    }

    private static List<AnalysisMethod> setAllConstruct(List<ConstructCreator> creatorList) {
        return CollectionUtils.emptyIfNull(creatorList)
                .stream()
                .map(ConstructCreator::getConstruct)
                .collect(Collectors.toList());
    }

    private static boolean iterateAllConstruct(MethodDeclaration user, MethodDeclaration recursive, List<AnalysisMethod> analysisMethods) throws ErrorException {
        for (AnalysisMethod method : analysisMethods) {
            if (method.checkStatementList(user, recursive)) {
                return true;
            }
        }
        return false;
    }

}

