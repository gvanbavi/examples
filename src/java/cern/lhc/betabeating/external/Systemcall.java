package cern.lhc.betabeating.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.lhc.betabeating.Tools.FileIO;

/**
 * Class that initiate a system call. Replaces the "custom" made system calls
 * 
 * @author Glenn Vanbavinckhove, tbach
 */
public abstract class Systemcall {
    private static final Logger log = Logger.getLogger(Systemcall.class);
    
    /** Runs a system call and cares about stuff happening */
    public static int execute(final SystemcallData systemcallData)
    {
        return execute(systemcallData.getCommand(), systemcallData.getApplicationName(), systemcallData.getEnvironment(), systemcallData.getPathForLogCommand(), systemcallData.getLogCommand());
    }

    /**
     * Runs a system call and cares about stuff happening
     * 
     * @param command command to run
     * @param applicationName name to display for window logger
     * @param environment
     * @param pathForLogCommand for writing command log file
     * @param logCommand should write command log file?
     * @return resultState
     */
    public static int execute(final String command, final String applicationName, final String[] environment, final String pathForLogCommand, final boolean logCommand) {
        log.info(">> " + applicationName + " ### System call started with:" + command);
        MessageManager.getConsoleLogger().info(applicationName + ": System call started with:" + command);
        final Process process = doExecute(command, environment);
        if (process == null) {
            MessageManager.getConsoleLogger().error("process failed, is null");
            return -1;
        }

        // prepare stream handlers
        final StreamHandler errorHandler = new StreamHandler(process.getErrorStream(), StreamType.STDERR, applicationName);
        final StreamHandler stdoutHandler = new StreamHandler(process.getInputStream(), StreamType.STDOUT, applicationName);
        // connect stream handlers
        errorHandler.start();
        stdoutHandler.start();

        final int resultStatus = getResultStatus(process, applicationName);

        if (errorHandler.getResult().length() > 2)
            MessageManager.getConsoleLogger().error("errors happened, read previous lines");

        if (logCommand)
            logCommand(pathForLogCommand, command, errorHandler.getResult());
        return resultStatus;
    }

    private static Process doExecute(String command, String[] environment) {
        Process process = null;

        try {
            log.info("-- execute process");
            if (environment == null || environment.length <= 0)
                process = Runtime.getRuntime().exec(command);
            else
                process = Runtime.getRuntime().exec(command, environment);
            log.info("-- process executed");
        } catch (IOException e) {
            log.warn("execute failed. ", e);
            MessageManager.getConsoleLogger().warn("execute failed. ", e);
        }

        return process;
    }

    private static int getResultStatus(Process process, String applicationName) {
        log.info(">> getResultStatus for process: " + process);
        if (process == null)
            return -1;
        int resultStatus = -1;
        try {
            log.info("-- process.waitFor()...");
            final long start = System.currentTimeMillis();
            resultStatus = process.waitFor();
            final long end = System.currentTimeMillis();
            if (log.isInfoEnabled())
                log.info("-- process.waitFor() finished, duration: " + (end - start) + "ms, resultStatus: " + resultStatus);
            MessageManager.getStatusLine().info(applicationName + ": Finished, duration: " + (end - start) + "ms, resultStatus: " + resultStatus);
        } catch (InterruptedException e) {
            log.warn("process.waitFor() failed. ", e);
            MessageManager.getConsoleLogger().warn("process.waitFor() failed. ", e);
        }
        log.info("<< getResultStatus");
        return resultStatus;
    }

    private static void logCommand(String path, String command, CharSequence error) {
        log.info(">> logCommand");
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path + "/command.run", true));
            bufferedWriter.write("Command => " + command + "\n\n");
            if (error.length() > 2)
                bufferedWriter.append(error);
            FileIO.tryToCloseCloseable(bufferedWriter);
        } catch (IOException e) {
            MessageManager.getConsoleLogger().warn("fileWriting failed. ", e);
        }
        log.info("<< logCommand");
    }

    /**
     * asynchronous, non-blocking thread to handle the process streams
     * 
     * @author ${user}
     * @version $Revision$, $Date$, $Author$
     */
    private static class StreamHandler extends Thread {
        // JavaAPI Doc: Because some native platforms only provide limited buffer size for standard input and output streams,
        // failure to promptly write the input stream or read the output stream of the subprocess may cause the subprocess to block, and even deadlock.
        private final InputStream inputStream;
        private final StreamType streamType;
        private final String applicationName;
        private final StringBuilder stringBuilder;

        public StreamHandler(InputStream inputStream, StreamType streamType, String applicationName) {
            this.inputStream = inputStream;
            this.streamType = streamType;
            this.applicationName = applicationName;
            this.stringBuilder = new StringBuilder();
        }

        @Override
        public void run() {
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String resultLine;
                    while ((resultLine = bufferedReader.readLine()) != null) {
                        streamType.log(applicationName + ": " + resultLine);
                        stringBuilder.append(resultLine).append("\n");
                    }
                } catch (IOException e) {
                    MessageManager.getConsoleLogger().warn("read inputStream failed. ", e);
                }
            } else
                log.warn("inputStream null");
        }

        public StringBuilder getResult() {
            return stringBuilder;
        }
    }

    /**
     * Handles the different logging types (error, stdout, stdin)
     * 
     * @author ${user}
     * @version $Revision$, $Date$, $Author$
     */
    private static enum StreamType {
        STDERR(LoggerWarn.INSTANCE), STDOUT(LoggerInfo.INSTANCE), STDIN(LoggerInfo.INSTANCE);

        private final LoggerObject loggerObject;

        private StreamType(LoggerObject loggerObject) {
            this.loggerObject = loggerObject;
        }

        public void log(String string) {
            loggerObject.log(this + ": " + string);
        }

        private static interface LoggerObject {
            public void log(String string);
        }

        private static class LoggerWarn implements LoggerObject {
            public static final LoggerWarn INSTANCE = new LoggerWarn();

            @Override
            public void log(String string) {
                MessageManager.getStatusLine().warn(string);
                MessageManager.getConsoleLogger().warn(string);
            }
        }

        private static class LoggerInfo implements LoggerObject {
            public static final LoggerInfo INSTANCE = new LoggerInfo();

            @Override
            public void log(String string) {
                MessageManager.getStatusLine().info(string);
                MessageManager.getConsoleLogger().info(string);
            }
        }
    }
}