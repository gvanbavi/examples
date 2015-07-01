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

import java.io.File;

import cern.lhc.betabeating.Tools.Date;

/**
 * Transports the Data from BeamSelection Frame.
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class BeamSelectionData {
    private String accelerator;
    private String inputPath;
    private String outputPath;
    private String optics;
    private String programLocation; //was: bbdir
    private String date = Date.getCurrentDateAsString();
    
    public BeamSelectionData(String accelerator, String inputPath, String outputPath, String optics, String programLocation) {
        this.accelerator = accelerator;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.optics = optics;
        this.programLocation = programLocation;
    }
    
    public String getAccelerator() {
        return accelerator;
    }
    public void setAccelerator(String accelerator) {
        this.accelerator = accelerator;
    }
    public String getInputPath() {
        return inputPath;
    }
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
    public String getOutputPath() {
        return outputPath;
    }
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    public String getOptics() {
        return optics;
    }
    public void setOptics(String optics) {
        this.optics = optics;
    }
    public String getProgramLocation() {
        return programLocation;
    }
    public void setProgramLocation(String programLocation) {
        this.programLocation = programLocation;
    }
    public String getDate() {
        return date;
    }
    
    /**
     * Checks if can read and write to inputPath and outputPath.
     * @return errormessage. Empty if no error.
     */
    public String checkIfValid()
    {
        StringBuilder errorMessage = new StringBuilder();
        if (!new File(inputPath).canRead())
            errorMessage.append("Error inputPath. Cannot read: ").append(inputPath).append(".\n");
        if (!new File(outputPath).canWrite())
            errorMessage.append("Error outputPath. Cannot write: ").append(outputPath).append(".\n");
        if (!new File(programLocation).canRead())
            errorMessage.append("Error programLocationPath. Cannot read: ").append(programLocation).append(".\n");
        return errorMessage.toString();
        
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BeamSelectionData [accelerator=");
        builder.append(accelerator);
        builder.append(", inputPath=");
        builder.append(inputPath);
        builder.append(", outputPath=");
        builder.append(outputPath);
        builder.append(", optics=");
        builder.append(optics);
        builder.append(", programLocation=");
        builder.append(programLocation);
        builder.append(", date=");
        builder.append(date);
        builder.append("]");
        return builder.toString();
    }
}