package org.parser.analysis.creator;

import org.parser.analysis.AnalysisMethod;
import org.parser.analysis.AnalysisWhileConstruct;

/**
 * <h1> CreatorWhileConstruct </h1>
 *
 * This class implements the method made available by the ConstructCreator interface, providing an instance of the AnalysisMethod class.
 */
public class CreatorWhileConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisWhileConstruct();
    }

}