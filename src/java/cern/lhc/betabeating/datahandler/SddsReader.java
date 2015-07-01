package cern.lhc.betabeating.datahandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;

public class SddsReader {
    private static final Logger log = Logger.getLogger(SddsReader.class);

    /**
     * Glenn Vanbavinckhove Local class for reading sdds files
     */
    public SddsReader() {

    }

    /*
     * Reading section
     */
    private ArrayList<String> bpmsHorizontal = new ArrayList<String>();
    private ArrayList<String> bpmsVertical = new ArrayList<String>();

    public float[][] datahh = null;
    public float[][] datavv = null;

    private HashMap<String, float[]> datah = new HashMap<String, float[]>();
    private HashMap<String, float[]> datav = new HashMap<String, float[]>();

    public int noturn = 0;

    public void loadtable(String file) {
        LineIterator lineIterator = null;
        try {
            lineIterator = FileUtils.lineIterator(new File(file), "UTF-8");
            while (lineIterator.hasNext()) {
                String line = lineIterator.nextLine();
                // / do something with line
                String[] splits = line.split("\\s+"); //TODO check if this can be done without the split, should be easy per character (tbach)
                String bpmName = new String(splits[1]); //i want an exact copy of the string, because the split[1] keeps a strong reference to the array filled with all the data which wastes a huge amount of memory (tbach)
                if (!line.contains("#") && splits[0].equals("0") && splits.length > 3) { // horizontal plane
                    bpmsHorizontal.add(bpmName);
                    float[] data = new float[splits.length - 3];
                    for (int i = 3; i < splits.length; i++) {
                        data[i - 3] = Float.parseFloat(splits[i]);
                    }
                    datah.put(bpmName, data);

                } else if (!line.contains("#") && splits[0].equals("1") && splits.length > 3) {
                    bpmsVertical.add(bpmName);
                    float[] data = new float[splits.length - 3];
                    for (int i = 3; i < splits.length; i++) {
                        data[i - 3] = Float.parseFloat(splits[i]);
                    }
                    datav.put(bpmName, data);

                    // back-up solution for nturns
                    if (noturn == 0) {
                        noturn = data.length;
                    }

                } else if (line.contains("#NTURNS")) { //no bpm name then, its a number (tbach)
                    noturn = Integer.parseInt(bpmName);
                }
            }

        } catch (IOException e) {
            log.error("Error rading file: " + file, e);
            MessageManager.getConsoleLogger().error("Error rading file: " + file, e);
            MessageManager.getStatusLine().error("Error reading file: " + file);
        } finally {
            LineIterator.closeQuietly(lineIterator);
        }

        // write in array format
        // [i][j] => i bpms , j nturns

        datahh = new float[bpmsHorizontal.size()][datah.get(bpmsHorizontal.get(0)).length];
        datavv = new float[bpmsVertical.size()][datav.get(bpmsVertical.get(0)).length];

        for (int i = 0; i < bpmsHorizontal.size(); i++) {
            float[] data = datah.get(bpmsHorizontal.get(i));
            for (int j = 0; j < data.length; j++) {
                datahh[i][j] = data[j];
            }
        }

        for (int i = 0; i < bpmsVertical.size(); i++) {
            float[] data = datav.get(bpmsVertical.get(i));
            for (int j = 0; j < data.length; j++) {
                datavv[i][j] = data[j];
            }
        }
    }

    public ArrayList<String> getBpmsHorizontal() {
        return bpmsHorizontal;
    }

    public ArrayList<String> getBpmsVertical() {
        return bpmsVertical;
    }
    
    public String[] getBpmsHorizontalAsArray() {
        return bpmsHorizontal.toArray(new String[] {});
    }
    
    public String[] getBpmsVerticalAsArray() {
        return bpmsVertical.toArray(new String[] {});
    }
}
