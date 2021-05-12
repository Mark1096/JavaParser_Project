package org.parser.analysis.creator;

import org.parser.analysis.AnalysisDoWhileConstruct;
import org.parser.analysis.AnalysisMethod;

/**
 * <h1> CreatorDoWhileConstruct </h1>
 * <p>
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorDoWhileConstruct implements ConstructCreator {

    /**
     * This method returns an instance of the AnalysisMethod class, namely AnalysisDoWhileConstruct.
     *
     * @return analysisMethod
     */
    public AnalysisMethod getConstruct() {
        return new AnalysisDoWhileConstruct();
    }

}
