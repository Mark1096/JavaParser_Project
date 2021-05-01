package org.parser.analysis.creator;

import org.parser.analysis.AnalysisForConstruct;
import org.parser.analysis.AnalysisMethod;

/**
 * <h1> CreatorForConstruct </h1>
 * <p>
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorForConstruct implements ConstructCreator {

    /**
     * This method returns an instance of the AnalysisMethod class, namely AnalysisForConstruct.
     *
     * @return AnalysisMethod
     */
    public AnalysisMethod getConstruct() {
        return new AnalysisForConstruct();
    }

}
