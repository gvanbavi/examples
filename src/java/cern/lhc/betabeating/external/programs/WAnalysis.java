package cern.lhc.betabeating.external.programs;

import cern.lhc.betabeating.external.ProgramPaths;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.external.SystemcallData;
import cern.lhc.betabeating.external.interfaces.Program;

public class WAnalysis implements Program{
    private static final String applicationName = "WAnalysis";
    private ProgramPaths programPaths = null;
        
    public WAnalysis(ProgramPaths programPaths) {
        this.programPaths = programPaths;
    }
    
    public int execute(WAnalysisData wAnalysisData) {
        String path = programPaths.getExecutionCommandForClass(getClass());
        String arguments = wAnalysisData.getArguments();
        
        String command = path + arguments;
        String[] environment = null;
        String pathForLogCommand = wAnalysisData.getOutputPath();
        boolean logCommand = true;
        SystemcallData systemcallData = new SystemcallData(command, applicationName, environment, logCommand, pathForLogCommand);
        return Systemcall.execute(systemcallData);
    }
}

/*
                   
                    public static int execute(final String command, final String applicationName, final String[] environment, final String pathForLogCommand, final boolean logCommand) {
                    String python = pathsData.get("python");
                    String program = pathsData.get("svdcleanpro");
                    String svdturn = pathsData.get("labelts");
                    String pk2pk = pathsData.get("labelps");
                    String sums = pathsData.get("labelss");
                    String svdval = pathsData.get("labelsis");
                    final String pythonCall = python + " " + program + 
                            " -f " + pathname + 
                            " -t " + svdturn + 
                            " -p " + pk2pk + 
                            " -s " + sums + 
                            " -v " + svdval;
                    log.info("pythonCall: " + pythonCall + "start\noutputpath: " + outputpath);
                    Systemcall.execute(pythonCall, "SVD clean", env, outputpath, true);
                    log.info("-- pythonCall finished");


example:

export PYTHONPATH=$PYTHONPATH:/afs/cern.ch/eng/sl/lintrack/Python_Classes4MAD/
/usr/bin/python /afs/cern.ch/eng/sl/lintrack/Beta-Beat.src/GetLLM/getsuper.py \
 --twiss="/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/models/LHCB1/beam1_dry_run_1_10_1_3_try2/" \
 --output=output -t SUSSIX -a LHCB1 \
 --beam=B1 --qx=0.31 --qy=0.32 \
 --qdx=0.304 --qdy=0.326 \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_14_23_920//Beam1@Turn@2011_08_24@09_14_23_920_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@08_20_00_086//Beam1@Turn@2011_08_24@08_20_00_086_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@08_21_27_607//Beam1@Turn@2011_08_24@08_21_27_607_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_40_52_536//Beam1@Turn@2011_08_24@09_40_52_536_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_13_01_678//Beam1@Turn@2011_08_24@09_13_01_678_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_39_33_625//Beam1@Turn@2011_08_24@09_39_33_625_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_38_13_893//Beam1@Turn@2011_08_24@09_38_13_893_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_11_30_026//Beam1@Turn@2011_08_24@09_11_30_026_0.sdds.new"
 
 */