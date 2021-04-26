package org.parser.analysis.creator;

import org.parser.analysis.AnalysisIfConstruct;
import org.parser.analysis.AnalysisMethod;

public class CreatorIfConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisIfConstruct();
    }

}
