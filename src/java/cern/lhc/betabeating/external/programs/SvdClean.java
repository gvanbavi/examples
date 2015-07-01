package cern.lhc.betabeating.external.programs;

import cern.lhc.betabeating.external.ProgramPaths;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.external.SystemcallData;
import cern.lhc.betabeating.external.interfaces.Program;

public class SvdClean implements Program{
    private static final String applicationName = "SvdClean";
    private ProgramPaths programPaths = null;
    
    public SvdClean(ProgramPaths programPaths) {
        this.programPaths = programPaths;
    }
    public int execute(SvdCleanData svdClean) {
        String path = programPaths.getExecutionCommandForClass(getClass());
        String arguments = svdClean.getArguments();
        
        String command = path + arguments;
        String[] environment = null;
        String pathForLogCommand = svdClean.getOutputPath();
        boolean logCommand = true;
        SystemcallData systemcallData = new SystemcallData(command, applicationName, environment, logCommand, pathForLogCommand);
        return Systemcall.execute(systemcallData);
    }
}