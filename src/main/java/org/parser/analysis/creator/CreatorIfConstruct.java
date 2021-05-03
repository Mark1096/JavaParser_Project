package org.parser.analysis.creator;

import org.parser.analysis.AnalysisIfConstruct;
import org.parser.analysis.AnalysisMethod;

/**
 * <h1> CreatorIfConstruct </h1>
 * <p>
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorIfConstruct implements ConstructCreator {

    /**
     * This method returns an instance of the AnalysisMethod class, namely AnalysisIfConstruct.
     *
     * @return analysisMethod
     */
    public AnalysisMethod getConstruct() {
        return new AnalysisIfConstruct();
    }

}
