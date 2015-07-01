package cern.lhc.betabeating.external;

import cern.lhc.betabeating.external.programs.SvdClean;
import cern.lhc.betabeating.external.programs.SvdCleanData;
import cern.lhc.betabeating.external.programs.WAnalysis;
import cern.lhc.betabeating.external.programs.WAnalysisData;

public class ExternalPrograms {
    private ProgramPaths programPaths = null;
    
    private ExternalPrograms(ProgramPaths programPaths) {
        this.programPaths = programPaths;
    }
    
    public static ExternalPrograms getNewInstance(ProgramPaths programPaths)
    {
        return new ExternalPrograms(programPaths);
    }
    
    public int executeWAnalysis(WAnalysisData wAnalysisData)
    {
        return new WAnalysis(programPaths).execute(wAnalysisData);
    }
    
    public int executeSvdClean(SvdCleanData svdCleanData)
    {
        return new SvdClean(programPaths).execute(svdCleanData);
    }
}