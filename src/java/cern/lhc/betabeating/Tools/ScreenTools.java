package cern.lhc.betabeating.Tools;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * Project wide needed Methods for Screen handling.
 * 
 * @author tbach
 */
public class ScreenTools {
    private static GraphicsDevice graphicsDevice = null;
    
    /** Screen width for default display */
    public static int getScreenWidth()
    {
        initializeGraphicsDevice();
        return graphicsDevice.getDisplayMode().getWidth();
    }
    
    /** Screen height for default display */
    public static int getScreenHeight()
    {
        initializeGraphicsDevice();
        return graphicsDevice.getDisplayMode().getHeight();
    }
    
    private static void initializeGraphicsDevice()
    {
        if (graphicsDevice == null) { //can "broke" with multiple threads, doesnt matter (tbach)
            graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        }
    }
    
    /** Get the current screen resolution as a Dimension object */
    public static Dimension getDimension()
    {
        return new Dimension(getScreenWidth(), getScreenHeight());
    }
}