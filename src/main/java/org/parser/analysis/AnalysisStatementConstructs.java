package org.parser.analysis;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;
import org.parser.analysis.creator.*;
import org.parser.error.ErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> AnalysisStatementConstructs </h1>
 * <p>
 * This class is used to analyze all constructs used within the body of each method. It interacts directly with the main App class,
 * providing it with the instances needed to call the methods that will handle the analysis of the user class.
 */
public class AnalysisStatementConstructs {

    /**
     * It checks the correspondence between the constructs of the two methods passed in as input.
     *
     * @param user      the user
     * @param recursive the recursive
     * @return boolean
     * @throws ErrorException the error exception
     */
    public static boolean checkAllConstruct(MethodDeclaration user, MethodDeclaration recursive) throws ErrorException {
        List<ConstructCreator> creator = addConstructCreator();
        List<AnalysisMethod> analysisMethods = setAllConstruct(creator);

        return iterateAllConstruct(user, recursive, analysisMethods);
    }

    /**
     * Returns a list containing all instances needed to construct objects representing the constructs.
     *
     * @return list construct creator
     */
    private static List<ConstructCreator> addConstructCreator() {
        return Arrays.asList(
                new CreatorIfConstruct(),
                new CreatorWhileConstruct(),
                new CreatorDoWhileConstruct(),
                new CreatorForConstruct(),
                new CreatorForEachConstruct(),
                new CreatorSwitchConstruct()
        );
    }

    /**
     * Returns a list containing all instances of the constructs to be parsed.
     *
     * @param creatorList the creator list
     * @return list analysis method
     */
    private static List<AnalysisMethod> setAllConstruct(List<ConstructCreator> creatorList) {
        return CollectionUtils.emptyIfNull(creatorList)
                .stream()
                .map(ConstructCreator::getConstruct)
                .collect(Collectors.toList());
    }

    /**
     * Analyze all required constructs.
     *
     * @param user the user
     * @param recursive the recursive
     * @param analysisMethods the analysis methods
     * @return boolean
     * @throws ErrorException the error exception
     */
    private static boolean iterateAllConstruct(MethodDeclaration user, MethodDeclaration recursive, List<AnalysisMethod> analysisMethods) throws ErrorException {
        for (AnalysisMethod method : analysisMethods) {
            if (method.checkStatementList(user, recursive)) {
                return true;
            }
        }
        return false;
    }

}

