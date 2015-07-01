package cern.lhc.betabeating.Tools;

public class ArrayHelper {
    public static double[] convertFloatArrayToDoubleArray(float[] floatArray)
    {
        if (floatArray == null)
            throw new IllegalArgumentException("input null");
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++)
            doubleArray[i] = floatArray[i];
        return doubleArray;
    }
}
