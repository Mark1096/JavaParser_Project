package org.parser.analysis.creator;

import org.parser.analysis.AnalysisForEachConstruct;
import org.parser.analysis.AnalysisMethod;

public class CreatorForEachConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisForEachConstruct();
    }

}
