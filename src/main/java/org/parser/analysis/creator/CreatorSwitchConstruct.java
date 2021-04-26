package org.parser.analysis.creator;

import org.parser.analysis.AnalysisMethod;
import org.parser.analysis.AnalysisSwitchConstruct;

public class CreatorSwitchConstruct implements ConstructCreator {

    public AnalysisMethod getConstruct() {
        return new AnalysisSwitchConstruct();
    }

}
