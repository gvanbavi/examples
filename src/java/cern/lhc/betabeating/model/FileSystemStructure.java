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

import cern.lhc.betabeating.Tools.FileIO;

public class FileSystemStructure {
    private final BeamSelectionData beamSelectionData;
    private String pathWithOutputDateModelsAccelerator;
    private String pathWithOutputDate;
    private String opticsfilename;
    private static final String directorySlash = "/";
    
    public FileSystemStructure(final BeamSelectionData beamSelectionData) {
        this.beamSelectionData = beamSelectionData;
    }
    
    public void createStructure()
    {
        initialize();
        createOptics();
        createMeasurements();
        createResults();
    }
    
    private void initialize()
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(beamSelectionData.getOutputPath()).append(directorySlash)
                        .append(beamSelectionData.getDate()).append(directorySlash);
        //example: /afs/cern.ch/user/t/tbach/temp/13-2-2012/
        pathWithOutputDate = stringBuilder.toString();
                        
        stringBuilder.append("models").append(directorySlash)
                        .append(beamSelectionData.getAccelerator()).append(directorySlash);
        //example: /afs/cern.ch/user/t/tbach/temp/13-2-2012/models/LHCB1/
        pathWithOutputDateModelsAccelerator = stringBuilder.toString();
    }
    
    private void createOptics()
    {
        if ("External".equals(beamSelectionData.getOptics())) //default
        {
            FileIO.createDirectory(pathWithOutputDateModelsAccelerator);
            opticsfilename = pathWithOutputDateModelsAccelerator;
        }
        else //if optics not default, add it
            FileIO.createDirectory(pathWithOutputDateModelsAccelerator + beamSelectionData.getOptics() + directorySlash);
        
        //example: /afs/cern.ch/user/t/tbach/temp/13-2-2012/models/LHCB1/<optics/>
    }
    
    private void createMeasurements()
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pathWithOutputDate).append(beamSelectionData.getAccelerator()).append(directorySlash)
                        .append("Measurements").append(directorySlash);
        FileIO.createDirectory(stringBuilder.toString());
    }
    
    private void createResults()
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pathWithOutputDate).append(beamSelectionData.getAccelerator()).append(directorySlash)
                        .append("Results").append(directorySlash);
        FileIO.createDirectory(stringBuilder.toString());
    }
    
    public String getOpticsfilename() {
        return opticsfilename;
    }
    
    public String getPathWithOutputDateModelsAccelerator() {
        return pathWithOutputDateModelsAccelerator;
    }
}