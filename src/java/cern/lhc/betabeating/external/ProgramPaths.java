package cern.lhc.betabeating.external;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.lhc.betabeating.Tools.PropertyHandler;
import cern.lhc.betabeating.external.interfaces.Program;
import cern.lhc.betabeating.external.programs.SvdClean;
import cern.lhc.betabeating.external.programs.WAnalysis;

public class ProgramPaths {
    private static final Logger log = Logger.getLogger(ProgramPaths.class);
    public static final String defaultBetaBeatingPath = "/afs/cern.ch/eng/sl/lintrack/Beta-Beat.src/";
    private static final String defaultProgramVersionsPath = "CoreFiles/ProgramVersions.properties";
    
    private String betaBeatingPath;
    private Properties pathsData;
    private String pythonPath;
    
    /**
     * This constructor will use the default BetaBeating path ({@link #defaultBetaBeatingPath}).<br>
     * {@link #ProgramPaths(String)} should be preferred. */
    public ProgramPaths() {
        this(defaultBetaBeatingPath);
    }
    
    /** The absolute path to the beta-beating directory is needed here, because this will be the entry point for all other programs */
    public ProgramPaths(String betaBeatingPath) {
        this.betaBeatingPath = betaBeatingPath;
        this.pathsData = PropertyHandler.getProperties(betaBeatingPath + defaultProgramVersionsPath);
        this.pythonPath = getPathForKey("python");
    }
    
    /**
     * Returns the execution command for the given class.<br>
     * Like for example, given: "matrix.class"<br>
     * Result: /usr/bin/python /afs/cern.ch/eng/sl/lintrack/Beta-Beat.src/matrix.py
     */
    public String getExecutionCommandForClass(Class<? extends Program> clazz)
    {
        String programePath = null;
        if (clazz.equals(WAnalysis.class))
            programePath = "###/afs/cern.ch/eng/sl/lintrack/Beta-Beat.src/GetLLM/getsuper.py";
        else if (clazz.equals(SvdClean.class))
            programePath = betaBeatingPath + getPathForKey("svdcleanpro");
        else
            throw new IllegalArgumentException("No programpath found for program/class: " + clazz);
        
        checkPath(programePath);
        return pythonPath + " " + programePath;
    }
    
    private void checkPath(String programAbsolutePath) {
        if (isProgramPath(programAbsolutePath) && !new File(programAbsolutePath).exists())
        {
            log.error("Program/path does not exist: " + programAbsolutePath);
            MessageManager.getConsoleLogger().error("Program/path does not exist: " + programAbsolutePath);
        }
        
    }
    
    private boolean isProgramPath(String path)
    {
        if ((path.contains(".py") || path.contains("GetCoupl") || path.contains("GetSex") || path.contains("Drive_God_lin") || path.contains("BPMtranslator.tfs"))) //TODO should be changed after external program refactoring (tbach)
            return true;
        else
            return false;
    }

    public String getPathForKey(String key)
    {
        String result = pathsData.getProperty(key);
        if (isProgramPath(result) && !key.equals("svdcleanpro")) //TODO should be changed after external program refactoring (tbach)
        {
            log.info("isProgram: " + result);
            result = betaBeatingPath + result;
            log.info("translated to: " + result);
        }
        return result;
    }
    
    public void setKeyWithPathData(String key, String value)
    {
        pathsData.setProperty(key, value);
    }
    
    public boolean keyForPathDataExists(String key)
    {
        return pathsData.containsKey(key);
    }
    
    public String getBetaBeatingPath() {
        return betaBeatingPath;
    }
    
    public String getPythonPath() {
        return pythonPath;
    }
}