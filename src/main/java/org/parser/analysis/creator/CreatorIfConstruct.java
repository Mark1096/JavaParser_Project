package org.parser.analysis.creator;

import org.parser.analysis.AnalysisIfConstruct;
import org.parser.analysis.AnalysisMethod;

/**
 * <h1> CreatorIfConstruct </h1>
 *
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorIfConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisIfConstruct();
    }

}
