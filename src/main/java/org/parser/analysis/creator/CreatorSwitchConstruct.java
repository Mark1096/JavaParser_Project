package org.parser.analysis.creator;

import org.parser.analysis.AnalysisMethod;
import org.parser.analysis.AnalysisSwitchConstruct;

/**
 * <h1> CreatorSwitchConstruct </h1>
 * <p>
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorSwitchConstruct implements ConstructCreator {

    /**
     * This method returns an instance of the AnalysisMethod class, namely AnalysisSwitchConstruct.
     *
     * @return analysisMethod
     */
    public AnalysisMethod getConstruct() {
        return new AnalysisSwitchConstruct();
    }

}
