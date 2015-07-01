/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.lhc.betabeating.model;

import java.util.Arrays;

import cern.lhc.betabeating.frames.interior.BPMpanel;

/**
 * Handles the data for a JList entry (SDDS filename) for the {@link BPMpanel}
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class SddsJListData {
    private final String[] bpmNamesHorizontal;
    private final String[] bpmNamesVertical;
    private final float[][] positionsHorizontal;
    private final float[][] positionsVertical;
    private final int numberOfTurns;
    
    public SddsJListData(String[] bpmNamesHorizontal, String[] bpmNamesVertical,
            float[][] positionsHorizontal, float[][] positionsVertical, int numberOfTurns) {
        super();
        this.bpmNamesHorizontal = bpmNamesHorizontal;
        this.bpmNamesVertical = bpmNamesVertical;
        this.positionsHorizontal = positionsHorizontal;
        this.positionsVertical = positionsVertical;
        this.numberOfTurns = numberOfTurns;
    }
    public String[] getBpmNamesHorizontal() {
        return bpmNamesHorizontal;
    }
    public String[] getBpmNamesVertical() {
        return bpmNamesVertical;
    }
    public float[][] getPositionsHorizontal() {
        return positionsHorizontal;
    }
    public float[][] getPositionsVertical() {
        return positionsVertical;
    }
    public int getNumberOfTurns() {
        return numberOfTurns;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SddsJListData [bpmNamesHorizontal=");
        builder.append(Arrays.toString(bpmNamesHorizontal));
        builder.append(", bpmNamesVertical=");
        builder.append(Arrays.toString(bpmNamesVertical));
        builder.append(", positionsHorizontal=");
        builder.append(Arrays.toString(positionsHorizontal));
        builder.append(", positionsVertical=");
        builder.append(Arrays.toString(positionsVertical));
        builder.append(", numberOfTurns=");
        builder.append(numberOfTurns);
        builder.append("]");
        return builder.toString();
    }
}