package cern.lhc.betabeating.Tools;

import static org.junit.Assert.*;

import java.awt.Dimension;

import org.junit.Test;

public class ScreenToolsTest {

    @Test
    public void testGetScreenWidth() {
        int value = ScreenTools.getScreenHeight();
        assertTrue("value should be > 0. Value: " + value, value > 0);
    }

    @Test
    public void testGetScreenHeight() {
        int value = ScreenTools.getScreenWidth();
        assertTrue("value should be > 0. Value: " + value, value > 0);
    }

    @Test
    public void testGetDimension() {
        Dimension dimension = ScreenTools.getDimension();
        assertNotNull(dimension);
    }
    
    @Test
    public void testCreateInstance() {
        ScreenTools instance = new ScreenTools();
        assertNotNull("instance not null", instance);
    }

}
