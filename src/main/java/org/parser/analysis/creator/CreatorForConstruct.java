package org.parser.analysis.creator;

import org.parser.analysis.AnalysisForConstruct;
import org.parser.analysis.AnalysisMethod;

public class CreatorForConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisForConstruct();
    }

}
