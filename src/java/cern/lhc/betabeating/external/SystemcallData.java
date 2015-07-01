package cern.lhc.betabeating.external;

import java.util.Arrays;

/**
 * Handles data for a systemcall.
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class SystemcallData {
    private final String command;
    private final String applicationName;
    private final String[] environment;
    private final boolean logCommand;
    private final String pathForLogCommand;
    
    /** Contains only the command, default application name, default environment and no logging */
    public SystemcallData(String command) {
        this(command, "notNamed", null, false, null);
    }
    
    /** Contains only the command and application name, default environment and no logging */
    public SystemcallData(String command, String applicatioName) {
        this(command, applicatioName, null, false, null);
    }
    
    /** Contains only the command and application name, environment and no logging */
    public SystemcallData(String command, String applicatioName, String[] environment) {
        this(command, applicatioName, environment, false, null);
    }

    public SystemcallData(String command, String applicationName, String[] environment, boolean logCommand, String pathForLogCommand) {
        super();
        this.command = command;
        this.applicationName = applicationName;
        this.environment = environment;
        this.logCommand = logCommand;
        this.pathForLogCommand = pathForLogCommand;
    }

    public String getCommand() {
        return command;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String[] getEnvironment() {
        return environment;
    }

    public boolean getLogCommand() {
        return logCommand;
    }

    public String getPathForLogCommand() {
        return pathForLogCommand;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SystemcallData [command=");
        builder.append(command);
        builder.append(", applicationName=");
        builder.append(applicationName);
        builder.append(", environment=");
        builder.append(Arrays.toString(environment));
        builder.append(", logCommand=");
        builder.append(logCommand);
        builder.append(", pathForLogCommand=");
        builder.append(pathForLogCommand);
        builder.append("]");
        return builder.toString();
    }
}
