package org.parser.analysis.creator;

import org.parser.analysis.AnalysisMethod;

/**
 * <h1> ConstructCreator </h1>
 * <p>
 * This interface provides a method for creating child instances of the parent class AnalysisMethod.
 */
public interface ConstructCreator {
    /**
     * This method returns an instance of the AnalysisMethod class.
     *
     * @return analysisMethod construct
     */
    AnalysisMethod getConstruct();
}
