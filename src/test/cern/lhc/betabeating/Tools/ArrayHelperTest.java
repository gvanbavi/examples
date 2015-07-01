package cern.lhc.betabeating.Tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArrayHelperTest {
    
    @Test
    public void testConvertFloatArrayToDoubleArray() {
        float[] floatArray = new float[]{1.0F, -12345.12345F, 53453.45234F};
        double[] doubleArray = ArrayHelper.convertFloatArrayToDoubleArray(floatArray);
        boolean isSame = true;
        for (int i = 0; i < floatArray.length; i++)
            isSame &= Math.abs(floatArray[i] - doubleArray[i]) < MathTools.delta;
        
        assertTrue("new double array should be the same", isSame);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testConvertFloatArrayToDoubleArrayFail() {
        ArrayHelper.convertFloatArrayToDoubleArray(null);
    }
    
    @Test
    public void testCreateInstance() {
        ArrayHelper instance = new ArrayHelper();
        assertNotNull("instance not null", instance);
    }
}