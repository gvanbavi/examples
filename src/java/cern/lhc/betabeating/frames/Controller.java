/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.lhc.betabeating.frames;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import cern.lhc.betabeating.external.ProgramPaths;
import cern.lhc.betabeating.frames.exterior.BeamSelection;
import cern.lhc.betabeating.frames.interior.MainWindow;
import cern.lhc.betabeating.model.BeamSelectionData;
import cern.lhc.betabeating.model.FileSystemStructure;

/**
 * Manages the JFrames.
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class Controller {
    private static final Logger log = Logger.getLogger(Controller.class);
    private BeamSelection beamSelection = null;
    private BeamSelectionData beamSelectionData = null;
    private ProgramPaths programPaths = null;
    private MainWindow mainWindow = null;
    
    private static int counter = 0;
    private int id;
    public Controller() {
        this.id = counter++;
    }
    
    public void showBeamSelection()
    {
        invoke(Window.BEAMSELECTION);
    }
    
    public void destroyBeamSelection()
    {
        log.info("<< bye bye beamSelection");
        beamSelection.setVisible(false);
        beamSelection.dispose();
        beamSelection = null;
    }
    
    public void showMainProgram(@SuppressWarnings("hiding") final BeamSelectionData beamSelectionData)
    {
        this.beamSelectionData = beamSelectionData;
        this.programPaths = new ProgramPaths(beamSelectionData.getProgramLocation());

        setKeyWithPathData("accel", beamSelectionData.getAccelerator());
        setKeyWithPathData("input", beamSelectionData.getInputPath());
        setKeyWithPathData("output", beamSelectionData.getOutputPath());
        setKeyWithPathData("optics", beamSelectionData.getOptics());
        setKeyWithPathData("date", beamSelectionData.getDate());
        setKeyWithPathData("bbdir", beamSelectionData.getProgramLocation());

        FileSystemStructure fileSystemStructure = new FileSystemStructure(beamSelectionData);
        fileSystemStructure.createStructure();
        setKeyWithPathData("opticspath", fileSystemStructure.getOpticsfilename());
        setKeyWithPathData("modelpath", fileSystemStructure.getPathWithOutputDateModelsAccelerator());
        
        invoke(Window.MAINWINDOW);
    }
    
    public String getPathDataForKey(String key)
    {
        return programPaths.getPathForKey(key);
    }
    
    public void setKeyWithPathData(String key, String value)
    {
        programPaths.setKeyWithPathData(key, value);
    }
    
    public boolean keyForPathDataexist(String key)
    {
        return programPaths.keyForPathDataExists(key);
    }
    
    public ProgramPaths getProgramPaths() {
        return programPaths;
    }
    
    public BeamSelectionData getBeamSelectionData() {
        return beamSelectionData;
    }

    private static enum Window
    {
        BEAMSELECTION, MAINWINDOW;
    }
    
    private void invoke(final Window window)
    {
        if (log.isInfoEnabled())
            log.info(">> hello " + window);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (window) {
                case BEAMSELECTION:
                    beamSelection = new BeamSelection(Controller.this);
                    beamSelection.createAndShowGUI();
                    break;
                case MAINWINDOW:
                    mainWindow = new MainWindow(Controller.this);
                    mainWindow.TriggerGUI();
                    String[] env = new String[1];
                    if (new File("/afs/cern.ch/eng/sl/lintrack/").exists())
                        env[0] = "PYTHONPATH= bbb:/afs/cern.ch/eng/sl/lintrack/Numeric-23_p2.3/lib/python2.3/site-packages/Numeric:/afs/cern.ch/eng/sl/lintrack/lib/python2.3/site-packages/:/afs/cern.ch/eng/sl/lintrack/Python_Classes4MAD/";
                    else
                        env[0] = "PYTHONPATH= bbb:" + beamSelectionData.getProgramLocation() + "/Python_Classes4MAD/";
                    mainWindow.setenv(env);
                    break;
                default:
                    throw new IllegalStateException("Should not happen, cannot create window, window not supported.");
                }
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Controller [id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}