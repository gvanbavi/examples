package cern.lhc.betabeating.Tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;

/**
 * Methods to handle properties files
 * 
 * @author tbach
 */
public class PropertyHandler {
    private static final Logger log = Logger.getLogger(PropertyHandler.class);
    
    public static Properties getProperties(String path)
    {
        return getProperties(new File(path));
    }
    
    public static Properties getProperties(File file)
    {
        if (log.isInfoEnabled())
            log.info(">> " + file);
        if (!file.exists())
        {
            log.error("<< File does not exists: " + file);
            MessageManager.getConsoleLogger().error("<< File does not exists: " + file);
            return null;
        }
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("-- Error loading properties file: " + file, e);
            MessageManager.getConsoleLogger().error("Error loading properties file: " + file, e);
        } finally {
            FileIO.tryToCloseCloseable(inputStream);
        }
        log.info("<< ");
        return properties;
    }
}
