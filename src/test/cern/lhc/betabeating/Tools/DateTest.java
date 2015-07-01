package cern.lhc.betabeating.Tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateTest {

    @Test
    public void testGetCurrentDateAsString() {
        assertNotNull("has date", Date.getCurrentDateAsString());
        assertTrue("has date", Date.getCurrentDateAsString().length() > 0);
    }
    
    @Test
    public void testCreateInstance() {
        Date instance = new Date();
        assertNotNull("instance not null", instance);
    }
}
