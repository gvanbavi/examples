package cern.lhc.betabeating.Tools;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;

/**
 * Common used File I/O commands. Wrapped to apache.commons.io if possible.
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public abstract class FileIO {
    private static final Logger log = Logger.getLogger(FileIO.class);
    
    public static boolean fileContentStartsWith(File file, String prefix) {
        final StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            int characterReadAsInt;
            int counter = 0;
            while ((characterReadAsInt = bufferedReader.read()) != -1 && counter < prefix.length()) {
                char character = (char) characterReadAsInt;
                stringBuilder.append(character);
                ++counter;
            }
            bufferedReader.close();
        } catch (IOException e) {
            if (MessageManager.getConsoleLogger().isInfoEnabled())
                MessageManager.getConsoleLogger().info("Reading failed for file: " + file, e);
            return false;
        } finally {
            tryToCloseCloseable(bufferedReader);
        }
        return stringBuilder.toString().startsWith(prefix);
    }
    
    /** Closes the closable, shows error if fails. */
    public static void tryToCloseCloseable(Closeable closeable) {
        try {
            closeable.close(); //if null, exception will be thrown and catched
        } catch (Exception e) {
            MessageManager.getConsoleLogger().error("closeable cannot be closed", e);
            log.error("-- closable cannot be closed", e);
        }
    }
    
    /** Creates the directory named by this abstract pathname, including any necessary but nonexistent parent directories.
     * @return true if and only if the directory was created, along with all necessary parent directories; false otherwise
     */
    public static boolean createDirectory(final String path)
    {
        if (log.isInfoEnabled())
            log.info(">> path: " + path);
        File file = new File(path);
        boolean isSuccessful = file.exists();
        int count = 0;
        int maxCount = 5;
        // fix for when dir fails.. seems to happen in threads did mkdir sometimes fails. //tbach: from old version, dont know why needed, but i kept it here
        while(!isSuccessful && (count < maxCount))
        {
            ++count;
            isSuccessful = file.mkdirs();
            if (!isSuccessful && MessageManager.getConsoleLogger().isWarnEnabled())
                MessageManager.getConsoleLogger().warn("(Attempt " + count +  "/" + maxCount + ") Path does not exists / could not be created: " + file);
        }
        if (!isSuccessful && MessageManager.getConsoleLogger().isErrorEnabled())
            MessageManager.getConsoleLogger().error("Path does not exists / could not be created: " + file);
        if (log.isInfoEnabled())
            log.info("<< isSuccessful: " + isSuccessful);
        return isSuccessful;
    }
    
    public static boolean copyFile(final File sourceFile, final File destinationFile) {
        if (log.isInfoEnabled())
            log.info(">> \nsourceFile:      " + sourceFile.getPath() + "\n" +
            		      "destinationFile: " + destinationFile.getPath());
        boolean isSuccessful = true;
        try {
            FileUtils.copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            isSuccessful = false;
            if (MessageManager.getConsoleLogger().isWarnEnabled())
                MessageManager.getConsoleLogger().warn("File copy failed. Source: " + sourceFile + ", destination: " + destinationFile, e);
        }
        if (log.isInfoEnabled())
            log.info("<< isSuccessful: " + isSuccessful);
        return isSuccessful;
    }
    
    public static boolean copyDirectory(final File sourceDirectory , final File destinationDirectory) {
        if (log.isInfoEnabled())
            log.info(">> \nsourceDirectory:      " + sourceDirectory.getPath() + "\n" +
                          "destinationDirectory: " + destinationDirectory.getPath());
        boolean isSuccessful = true;
        try {
            FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
        } catch (IOException e) {
            isSuccessful = false;
            if (MessageManager.getConsoleLogger().isWarnEnabled())
                MessageManager.getConsoleLogger().warn("Directory copy failed. Source: " + sourceDirectory + ", destination: " + destinationDirectory, e);
        }
        if (log.isInfoEnabled())
            log.info("<< isSuccessful: " + isSuccessful);
        return isSuccessful;
    }
    
    public static void writeContentToFile(String content, File file)
    {
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException e) {
            MessageManager.getConsoleLogger().error("writing failed for file: " + file, e);
            log.error("-- writing failed for file: " + file, e);
        }
    }
    
    public static void writeCollectionToFile(Collection<String> collection, File file)
    {
        try {
            FileUtils.writeLines(file, collection);
        } catch (IOException e) {
            MessageManager.getConsoleLogger().error("writing failed for file: " + file, e);
            log.error("-- writing failed for file: " + file, e);
        }
    }
}