package org.parser.analysis.creator;

import org.parser.analysis.AnalysisMethod;
import org.parser.analysis.AnalysisWhileConstruct;

public class CreatorWhileConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisWhileConstruct();
    }

}