package cern.lhc.betabeating.Tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathToolsTest {

    @Test
    public void testArithmeticMean() {
        double[] values = new double[] {1, 10, 9, 10, 13, 17};
        double result = MathTools.arithmeticMean(values);
        assertTrue("should be 10, is: " + result, Math.abs(result - 10) < MathTools.delta);
    }

    @Test
    public void testRootMeanSquare() {
        double[] values = new double[] {1, 10, 3, 2, 6, 0};
        double result = MathTools.rootMeanSquare(values);
        assertTrue("should be 5, is: " + result, Math.abs(result - 5) < MathTools.delta);
    }

    @Test
    public void testStandardDeviation() {
        double[] values = new double[] {2, 4, 4, 4, 5, 5, 7, 9};
        double result = MathTools.standardDeviation(values);
        assertTrue("should be 2, is: " + result, Math.abs(result - 2) < MathTools.delta);
    }
    
    @Test
    public void testCreateInstance() {
        MathTools instance = new MathTools();
        assertNotNull("instance not null", instance);
    }
    
    @Test
    public void testDelta() {
        assertTrue("should be the same with a good delta", Math.abs(7 - 7.0d) < MathTools.delta);
    }
}
