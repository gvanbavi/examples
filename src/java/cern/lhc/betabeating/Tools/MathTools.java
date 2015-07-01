package cern.lhc.betabeating.Tools;

/**
 * Some needed math tools, which are not included in java.lang.Math
 *
 * @author tbach
 */
public class MathTools {

    /** Delta to compare doubles. Value: {@value} */
    public static final double delta = 0.000001D;
    
    /** Standard arithmetic mean, like 1/n * sum from i=1 to n over x_i */
    public static double arithmeticMean(final double[] data) {
        double sum = 0.0d;
        for (double dataItem : data)
            sum += dataItem;
        return sum / data.length;
    }

    /** Root mean square, like sqrt(1/n * sum from i=1 to n over (x_i)^2) */
    public static double rootMeanSquare(final double[] data) {
        double sum = 0.0d;
        for (double dataItem : data)
            sum += dataItem * dataItem;
        return Math.sqrt(sum / data.length);
    }

    /** Standard Deviation, like sqrt(1/n * sum from i=1 to n over (x_i - AM)^2) , with AM = arithmetic mean */
    public static double standardDeviation(final double[] data) {
        double sum = 0.0d;
        double arithmeticMean = arithmeticMean(data);
        for (double dataItem : data)
        {
            double innerSum = dataItem - arithmeticMean;
            sum += innerSum * innerSum;
        }
        return Math.sqrt(sum / data.length);
    }
}
