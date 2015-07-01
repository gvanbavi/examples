package cern.lhc.betabeating.datahandler;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.sdds.core.SDDSFile;
import cern.lhc.betabeating.Tools.FileIO;
import cern.lhc.betabeating.Tools.MathTools;

public class Converter {
    private static final Logger log = Logger.getLogger(Converter.class);

	/**
	 *  Glenn Vanbavinckhove
	 *  class for converting sdds file into our tbt data
	 */
	private SDDSFile sddsfile = null;
	
	public Converter(){
	}
	
	/*
	 *  reading sddsfile
	 */
	
    public void readfile(File file, String outputWithFileName) throws IOException {
        File workingCopy = new File(outputWithFileName);
        FileIO.copyFile(file, workingCopy);
        FileIO.writeContentToFile("file copied from:\n" + file.getAbsolutePath(), new File(workingCopy + "_origin"));
        sddsfile = new SDDSFile(workingCopy);
        sddsfile.readFile();
        dataHandler(outputWithFileName);
    }
		
	/*
	 * Reading twiss for positions
	 */
	
    private HashMap<String, Double> postwiss = new HashMap<String, Double>();

    public void readtwiss(String twissname) {
        TFSReader tfsReader = new TFSReader();
        tfsReader.loadTable(twissname);
        String[] names = tfsReader.getStringData("NAME");
        double[] positions = tfsReader.getDoubleData("S");

        for (int i = 0; i < names.length; i++)
            postwiss.put(names[i], positions[i]);
    }
	
	/*
	 * Datahandling
	 */
	
	public String[] bpmNames;
	private static final String MONITOR_NAMES = "bpmNames";
	private static final String TURN_NUMBER_KEY = "nbOfCapTurns";
	private static final String BUNCH_NUMBER_KEY = "nbOfCapBunches";
	private static final String TURN_POS_H_KEY = "horPositionsConcentratedAndSorted";
	private static final String TURN_POS_V_KEY = "verPositionsConcentratedAndSorted";
	private static final String BUNCH_IDS = "horBunchId";
	public int[] bunchIds;
	public int numberOfBunches = 0;
	public int numberOfTurns = 0;
	public HashMap<Integer,float[][]> dataperbunchH= new HashMap<Integer, float[][]>();
	public HashMap<Integer,float[][]> dataperbunchV= new HashMap<Integer, float[][]>();
	public List<Integer> validBunchIds;
	
    private void dataHandler(String output) {
        if (sddsfile == null)
            throw new IllegalStateException("No sddsfile loaded");
        log.info(output);

        bpmNames = sddsfile.getArray(MONITOR_NAMES).getStringValues();
        int numberOfMonitors = sddsfile.getArray(MONITOR_NAMES).getLength();


        float[] allPositionsH = sddsfile.getArray(TURN_POS_H_KEY).getFloatValues();
        float[] allPositionsV = sddsfile.getArray(TURN_POS_V_KEY).getFloatValues();
//        System.out.println(sddsfile.getArray(TURN_POS_H_KEY).getType()); // => 2
//        public final static int FLOAT = 2; // SDDSUtil.SDDS_FLOAT;
//        System.out.println(Arrays.toString(sddsfile.getArrayNames()));
        //[horPositionsConcentratedAndSorted, verPositionsConcentratedAndSorted, bpmNames, horBunchId, horBunchIdFailsInTurn, verBunchId, verBunchIdFailsInTurn]
        

        bunchIds = sddsfile.getArray(BUNCH_IDS).getIntValues();

        numberOfBunches = sddsfile.getParameter(BUNCH_NUMBER_KEY).getIntValue();
        numberOfTurns = sddsfile.getParameter(TURN_NUMBER_KEY).getIntValue();
        if (log.isInfoEnabled())
            log.info("-- numberOfBunches: " + numberOfBunches + ", numberOfMonitors: " + numberOfMonitors + ", numberOfTurns: " + numberOfTurns);
        int nrTurnsAndBunches = numberOfBunches * numberOfTurns;
        validBunchIds = new ArrayList<Integer>();
        
        //variables used in the loops
        String bpmName;
        StringBuilder tbtdatah;
        StringBuilder tbtdatav;
        float turnValueH;
        float turnValueV;

        // i ... number of monitors
        // j ... number of bunches
        // k ... number of turns
        for (int bunchNumber = 0; bunchNumber < numberOfBunches; bunchNumber++) {
            float[][] hdata = new float[numberOfMonitors][numberOfTurns];
            float[][] vdata = new float[numberOfMonitors][numberOfTurns];
            List<String> linesToWrite = new ArrayList<String>();
            boolean isNullBunch = true;
            
            linesToWrite.add("# bunchid :" + bunchIds[bunchNumber]);
            linesToWrite.add("# number of turns :" + numberOfTurns);
            linesToWrite.add("# number of monitors :" + numberOfMonitors);

            for (int monitorNumber = 0; monitorNumber < numberOfMonitors; monitorNumber++) {
                bpmName = bpmNames[monitorNumber];
                if (!postwiss.containsKey(bpmName))
                {
                    MessageManager.getConsoleLogger().warn("Skipped this one. There is no twiss position for BPM: " + bpmName);
                    continue;
                }
                
                tbtdatah = new StringBuilder();
                tbtdatav = new StringBuilder();
                tbtdatah.append("0 ").append(bpmName).append(" ").append(postwiss.get(bpmName)).append(" ");
                tbtdatav.append("1 ").append(bpmName).append(" ").append(postwiss.get(bpmName)).append(" ");

                for (int turnNumber = 0; turnNumber < numberOfTurns; turnNumber++) {
                    // write down all turn values
                    turnValueH = allPositionsH[monitorNumber * nrTurnsAndBunches + bunchNumber * numberOfTurns + turnNumber];
                    turnValueV = allPositionsV[monitorNumber * nrTurnsAndBunches + bunchNumber * numberOfTurns + turnNumber];
                    
                    // if all values are only zeroes, then it is a null bunch and should be skipped because there is no use for this testdata
                    if (isNullBunch && (Math.abs(turnValueH) > MathTools.delta || Math.abs(turnValueH) > MathTools.delta)) //if one value is not zero, then its not a null bunch
                        isNullBunch = false;
                    
                    // for file writing
                    tbtdatah.append(turnValueH).append(" ");
                    tbtdatav.append(turnValueV).append(" ");

                    // for GUI and matrix
                    hdata[monitorNumber][turnNumber] = turnValueH;
                    vdata[monitorNumber][turnNumber] = turnValueV;

                }
                linesToWrite.add(tbtdatah.toString());
                linesToWrite.add(tbtdatav.toString());
            }
            if (isNullBunch)
            {
                log.info("-- NullBunch detected (all turn values equals zero, ignoring this bunch");
                continue;
            }
            validBunchIds.add(bunchNumber);
            String fileName = output.replace(".sdds", "") + "_" + bunchIds[bunchNumber] + ".sdds";
            FileIO.writeCollectionToFile(linesToWrite, new File(fileName));
            logData(hdata, vdata);
            
            dataperbunchH.put(bunchNumber, hdata);
            dataperbunchV.put(bunchNumber, vdata);
        }
        log.info("Data in matrixes");
    }

    private void logData(float[][] hdata, float[][] vdata) {
        if (!log.isInfoEnabled())
            return;
        int arraySizeH = 0;
        for (int i = 0; i < hdata.length; ++i)
            arraySizeH += Math.max(1, hdata[i].length);
        int arraySizeV = 0;
        for (int i = 0; i < vdata.length; ++i)
            arraySizeV += Math.max(1, vdata[i].length);
        log.info("size of hdata: " + arraySizeH  + ", size of vdata: " + arraySizeV);
    }
}
