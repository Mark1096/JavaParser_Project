package org.parser.analysis.creator;

import org.parser.analysis.AnalysisForEachConstruct;
import org.parser.analysis.AnalysisMethod;

/**
 * <h1> CreatorForEachConstruct </h1>
 *
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorForEachConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisForEachConstruct();
    }

}
