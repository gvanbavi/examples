package cern.lhc.betabeating.frames.interior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.ExternalFrame;
import cern.accsoft.gui.frame.FrameManager;
import cern.accsoft.gui.frame.MessageManager;
import cern.lhc.betabeating.Tools.ScreenTools;
import cern.lhc.betabeating.constants.Analysis_const;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.exterior.Send4Optics;
import cern.lhc.betabeating.modelcreator.ModelCreator;

public class MainWindow{
    private static final Logger log = Logger.getLogger(MainWindow.class);
    private Controller controller = null;
    private BPMpanel bpmpanel = null;
    private AnalysisPanel analpane = null;
    private OpticsPanel oppanel = null;
    private CorrectionPanel cppanel = null;
    private Send4Optics s4o = null;
    
    public MainWindow(Controller controller){
        this.controller = controller;
//        getPrograms();
        this.bpmpanel = new BPMpanel(controller);
        this.analpane = new AnalysisPanel(controller);
        this.oppanel = new OpticsPanel(controller);
        this.cppanel = new CorrectionPanel(controller);
        this.s4o = new Send4Optics(controller, bpmpanel,oppanel);
    }
    
    /*
     * graphical component
     */
    private ExternalFrame framemain;
    private JPanel mainpanel = new JPanel();
    private JToolBar toolBar = new JToolBar("Toolbar beta-beat");
    private JTabbedPane tabs = new JTabbedPane();
 
    private JLabel opticslabel = new JLabel();
    private JLabel labelheap = new JLabel();
    
    private String[] env;
    
    private JButton exitbutton;
    private JButton modelbutton;
    private JButton addbutton;
    private JButton settingbutton;
    
    public void TriggerGUI(){
        String currentAccelerator = controller.getBeamSelectionData().getAccelerator();
        String currentOptics = controller.getBeamSelectionData().getOptics();
        
        setupImages();
        
        
        // main frame
        framemain = FrameManager.getInstance().getMainFrame("GUI for Beta-beating",mainpanel);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int)dim.getWidth();
        if(screenWidth>1400) screenWidth=1400;
        int screenHeight = (int)dim.getHeight()-200;
        
        //checking program
        initsetting();
        getheapspace ghp = new getheapspace();
         Timer timer = new Timer();
         timer.schedule(ghp, 5000,10000);
        opticslabel.setFont(new Font("Serif", Font.BOLD, 24));
        labelheap.setFont(new Font("Serif", Font.BOLD, 24));
        labelheap.setForeground(Color.GREEN);
        
        
        toolBar.add(exitbutton);
        toolBar.add(modelbutton);
        toolBar.add(addbutton);
        toolBar.add(settingbutton);
        toolBar.add(new JSeparator());  
        toolBar.add(opticslabel); 
        toolBar.add(new JSeparator()); 
        toolBar.add(labelheap); 
    
        mainpanel.setLayout(new BorderLayout());
        // adding main panels
        //=> BPMPanel
        bpmpanel.createGUI();
        bpmpanel.setenv(env);
        bpmpanel.setapanel(analpane);
        tabs.addTab("BPM panel", bpmpanel);
        //=> Analyze panel
        
        analpane.createGUI();
        analpane.setenv(env);
        analpane.sets4o(s4o);
        tabs.addTab("Analysis panel", analpane);
        
        //=> Optics panel
        oppanel.setenv(env);
        oppanel.setcorsel(cppanel);
        tabs.addTab("Optics", oppanel);
        
        //=> Correction panel
        //cppanel.CreateGUI();
        cppanel.setenv(env);
        tabs.addTab("Correction", cppanel);     
        
               
        mainpanel.add(toolBar,BorderLayout.NORTH);
        mainpanel.add(tabs,BorderLayout.CENTER);
        
        //setting variables
        framemain.setSize(screenWidth,screenHeight);
        framemain.setVisible(true);
        
        //framemain.setJMenuBar(menuBar);
        framemain.setLocation(0,0);
        framemain.getStatusBar().setConsoleButtonVisible(true);
        framemain.setConsoleVisible(true);
        framemain.setStatusBarVisible(true);
        MessageManager.getConsoleLogger().info("MainWindow => Welcome to beta-beating application");
        MessageManager.getStatusLine().info("MainWindow => Welcome to beta-beating application");
        
        MessageManager.getConsoleLogger().info("MainWindow => Loading finished you can start working now! ");
        
        //model
        initiatemodel();
         if(currentOptics.equals("External") && (currentAccelerator.contains("LHC")  || currentAccelerator.contains("SPS") || currentAccelerator.contains("RHIC"))){
              MessageManager.getConsoleLogger().info("MainWindow => No optics was selected please create optics with the model creator! ");
              MessageManager.info("MainWindow => model creator", null);
              createoptics();
         }else if(currentAccelerator.equals("SOLEIL")){
             JFileChooser  chooser= new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int value=chooser.showOpenDialog(null);
                chooser.setVisible(true);
                if (value==JFileChooser.APPROVE_OPTION){
                    if(new File(chooser.getSelectedFile()+"/twiss.dat").exists()){
                        controller.setKeyWithPathData("opticspath",chooser.getSelectedFile().toString() );
                        MessageManager.getConsoleLogger().info("MainWindow => Loading SOLEIL optics into memory: "+chooser.getSelectedFile().toString());
                    }else{
                        MessageManager.error("MainWindow => Selected optics path does not exist", null, null);
                        MessageManager.getConsoleLogger().error("MainWindow => " +chooser.getSelectedFile()+"/twiss.dat");
                    }
                }
             
          }else{
              opticslabel.setText("Model : "+ currentOptics);
          }
        listeners();
    }

    private void setupImages() {
        String imageFolder = controller.getBeamSelectionData().getProgramLocation() + "/CoreFiles/Images/";
        exitbutton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageFolder + "exit.jpg")));
        exitbutton.setToolTipText("Exit");
        modelbutton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageFolder + "model.JPG")));
        modelbutton.setToolTipText("Model");
        addbutton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageFolder + "icon_add.png")));
        addbutton.setToolTipText("Add files");
        settingbutton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageFolder + "icon_setting.gif")));
        settingbutton.setToolTipText("Settings");
    }
    
    private File[] inputfiles;
    private File inputfile;
    private String[] children;
    private String childname;

    private void listeners(){
        exitbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
            try {
                Thread.sleep(3000);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }});
        
        addbutton.addActionListener(new ActionListener(){public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent){
            JFileChooser jFileChooser= new JFileChooser(controller.getBeamSelectionData().getInputPath());
            jFileChooser.setMultiSelectionEnabled(true);
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jFileChooser.setPreferredSize(new Dimension(ScreenTools.getScreenWidth() / 2, ScreenTools.getScreenHeight() * 2 / 3));
            int value=jFileChooser.showOpenDialog(null);
            boolean optpass=false;
            if (value==JFileChooser.APPROVE_OPTION){

                inputfiles=jFileChooser.getSelectedFiles();
                MessageManager.getConsoleLogger().info("MainWindow => Will import "+inputfiles.length+" directories");
                
                
                for(int i=0;i<inputfiles.length;i++){
                    inputfile=inputfiles[i];
                    children=inputfile.list();
                    
                    for(int j=0;j<children.length;j++){
                        if(children[j].contains("linx") && !children[j].contains("~") && !children[j].contains("old")){
                            childname=children[j].replace("_linx", "");
                             analpane.addFile(inputfile.toString()+"/"+childname, "SUSSIX");
                        }else if(children[j].contains("svdx") && !children[j].contains("~") && !children[j].contains("old")){
                            childname=children[j].replace("_svdx", "");
                            analpane.addFile(inputfile.toString()+"/"+childname, "SVD");
                        }else if(children[j].endsWith(".new")){
                            
                        }else if(children[j].endsWith(".out")){
                            optpass=true;
                        }else if(children[j].endsWith(".tfs")){
                            if(children[j].contains("couple")){
                                cppanel.addFile(inputfile.toString(),"COUPLING");
                            }else{
                                cppanel.addFile(inputfile.toString(),"BETA");
                            }
                        }
                    }
                    if(optpass)
                        oppanel.addfile(inputfile.toString(),"none");
                }
                
            }else{
                MessageManager.getConsoleLogger().warn("MainWindow => Cancel button was pushed nothing will happen");
            }
            
        }});
        
        
        settingbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
            settingsframe();
        }});
        
        buttonsave.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
            controller.setKeyWithPathData("labelts",fieldts.getText());
            controller.setKeyWithPathData("labelps", fieldps.getText());
            controller.setKeyWithPathData("labelss",fieldss.getText() );
            controller.setKeyWithPathData("labelsis", fieldsis.getText());
            //controller.setKeyWithPathData("labelrds",fieldrds.getText());
            controller.setKeyWithPathData("labelmsvd",fieldmsvd.getText());
            controller.setKeyWithPathData("labelug",fieldug.getText());
            controller.setKeyWithPathData("labelcs", fieldcs.getText());
            controller.setKeyWithPathData("labelctune", fieldctune.getText());
            controller.setKeyWithPathData("svdcleanon", comboswitch.getSelectedItem().toString());
            
            frame.setVisible(false);
            frame.dispose();
        }});
        
        buttoncancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
            frame.setVisible(false);
            frame.dispose();            
        }});    
        
        modelbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
            createoptics();
        }});
        
    }
    
    /*
     * Setting env and paths
     */
    public void setenv(String[] envin){
        env=envin;
    }
    
    /*
     * creating optics
     */
    private void createoptics(){
        ModelCreator modelc= new ModelCreator(controller);
        modelc.initiate();
        modelc.setlabel(opticslabel);
        modelc.setenv(env);
        modelc.showGUI();
    }
    
    /*
     * initiate model
     */
    private void initiatemodel(){
        Analysis_const analcon= new Analysis_const();
        if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
            controller.setKeyWithPathData("%PATH", controller.getPathDataForKey("opticspath"));
             if(controller.getBeamSelectionData().getAccelerator().equals("LHCB1")){
                 controller.setKeyWithPathData("%BEAM","B1");
             }else{
                 controller.setKeyWithPathData("%BEAM","B2");
             }
             controller.setKeyWithPathData("%QDY",analcon.LHC().get("tuneyd"));
             controller.setKeyWithPathData("%QY",analcon.LHC().get("tuney"));
             controller.setKeyWithPathData("%QDX",analcon.LHC().get("tunexd"));
             controller.setKeyWithPathData("%QX",analcon.LHC().get("tunex"));
             controller.setKeyWithPathData("%STOP","");
             controller.setKeyWithPathData("%INCLUDE","!");
             controller.setKeyWithPathData("%DPP","0.0");
             controller.setKeyWithPathData("%QMX",analcon.LHC().get("tunex").replace("0.", ""));
             controller.setKeyWithPathData("%QMY",analcon.LHC().get("tuney").replace("0.", ""));
            
        }else{
             controller.setKeyWithPathData("%PATH", controller.getPathDataForKey("opticspath"));
             controller.setKeyWithPathData("%BEAM","");
             controller.setKeyWithPathData("%QDY","");
             controller.setKeyWithPathData("%QY",analcon.SPS().get("tuney"));
             controller.setKeyWithPathData("%QDX","");
             controller.setKeyWithPathData("%QX",analcon.SPS().get("tunex"));
             controller.setKeyWithPathData("%STOP","");
             controller.setKeyWithPathData("%INCLUDE","!");
             controller.setKeyWithPathData("%DPP","0.0");
             controller.setKeyWithPathData("%QMX",analcon.LHC().get("tunex").replace("0.", ""));
             controller.setKeyWithPathData("%QMY",analcon.LHC().get("tuney").replace("0.", ""));
        }
    }
    
    /*
     * fill map with programpaths
     */
    
//    private void getPrograms(){
//        String key = null;
//        String pathvalue = null;
//        String line = null;
//        File file = new File(controller.getBeamSelectionData().getProgramLocation() + ProgramPaths.defaultProgramVersionsPath);
//        log.info("ProgramVersions: " + file);
//        if(!file.canRead()){
//            MessageManager.getConsoleLogger().error("MainWindow => FATAL error, cannot read: " + file);
//            MessageManager.getConsoleLogger().error("MainWindow => Program will not properly run \n Contact expert");
//            return;
//        }
//
//        MessageManager.info("MainWindow => Core file found", null);
//        MessageManager.getConsoleLogger().info("MainWindow => Will start loading programs into memory", null);
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new FileReader(file));
//
//            while ((line = bufferedReader.readLine()) != null) {
//                if (!line.contains("#") && line.contains("KEY")) {
//                    key = line.replace("KEY=", "");
//                } else if (!line.contains("#") && line.contains("VALUE") && !controller.keyForPathDataexist(key)) {
//                    pathvalue = line.replace("VALUE=", "");
//
//                    // handling exceptions
//                    if (key.equals("conpro")) {
//                        pathvalue = "MODEL/" + controller.getBeamSelectionData().getAccelerator() + "/" + pathvalue;
//                        pathvalue = controller.getBeamSelectionData().getProgramLocation() + "/" + pathvalue;
//                    } else if (key.equals("python") || key.equals("madx") || key.equals("server")) {
//
//                    } else {
//                        pathvalue = controller.getBeamSelectionData().getProgramLocation() + "/" + pathvalue;
//                    }
//
//                    if (new File(pathvalue).exists()) {
//                        controller.setKeyWithPathData(key, pathvalue);
//                        MessageManager.getConsoleLogger().info("MainWindow => Loading " + pathvalue + " into memory", null);
//                    } else if (key.equals("server")) {
//                        controller.setKeyWithPathData(key, pathvalue);
//                        MessageManager.getConsoleLogger().info("MainWindow => Loading server into memory " + pathvalue, null);
//                    } else {
//                        MessageManager.getConsoleLogger().warn("MainWindow => Error while checking program");
//                        MessageManager.getConsoleLogger().warn("MainWindow => Does the program exist? " + key + " " + pathvalue);
//                    }
//
//                } else if (!line.contains("#")) {
//                    MessageManager.warn("MainWindow => Error while loading programs into memory", null, null);
//                    MessageManager.getConsoleLogger().warn("MainWindow => Problem with " + key
//                                    + " \n Restart program if problem continues contact expert");
//                }
//            }
//            MessageManager.getConsoleLogger().info("MainWindow => You selected " + controller.getBeamSelectionData().getAccelerator());
//        } catch (IOException e) {
//            MessageManager.getConsoleLogger().error("readError", e);
//        } finally {
//            FileIO.tryToCloseCloseable(bufferedReader);
//        }
//    }
    
    /*
     * 
     */
    private JFrame frame = new JFrame();
    private GridLayout gridlay = new GridLayout(10,2,20,20);
    
    private JLabel labelts = new JLabel("SVD clean start turn :");
    private JLabel labelps = new JLabel("SVD clean  pk-2-pk cut :");    
    private JLabel labelss = new JLabel("SVD clean sum square :");      
    private JLabel labelsis = new JLabel("SVD clean sing val cut :");
    //private JLabel labelrds = new JLabel("SVDC ran dis :");
    private JLabel labelson = new JLabel("SVD clean ON ?");
    private JLabel labelmsvd = new JLabel("SVD modes:");    
    private JLabel labelug = new JLabel("Optics unit :");       
    private JLabel labelcs = new JLabel("SBS cut on error :");  
    
    private JTextField fieldts= new JTextField();
    private JTextField fieldps= new JTextField();
    private JTextField fieldss= new JTextField();
    private JTextField fieldsis= new JTextField();
    //private JTextField fieldrds= new JTextField();
    private JTextField fieldmsvd= new JTextField();
    private JTextField fieldug= new JTextField();
    private JTextField fieldcs= new JTextField();
    private JTextField fieldctune= new JTextField();
    private JComboBox comboswitch= new JComboBox(new Object[]{"Yes","No"});
    
    private JButton buttoncancel = new JButton("Cancel");
    private JButton buttonsave = new JButton("Save");   
    
    private void settingsframe(){
        frame.setTitle("Settings");
        frame.setLayout(gridlay);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.add(labelts);frame.add(fieldts);
        frame.add(labelps);frame.add(fieldps);
        frame.add(labelss);frame.add(fieldss);
        frame.add(labelsis);frame.add(fieldsis);
        //frame.add(labelrds);frame.add(fieldrds);
        frame.add(labelmsvd);frame.add(fieldmsvd);
        frame.add(labelug);frame.add(fieldug);
        frame.add(labelcs);frame.add(fieldcs);
        frame.add(labelcs);frame.add(fieldcs);
        frame.add(labelson);frame.add(comboswitch);
        frame.add(buttonsave);frame.add(buttoncancel);
        
        fieldts.setText(controller.getPathDataForKey("labelts"));
        fieldps.setText(controller.getPathDataForKey("labelps"));
        fieldss.setText(controller.getPathDataForKey("labelss"));
        fieldsis.setText(controller.getPathDataForKey("labelsis"));
        //fieldrds.setText(pathsData.get("labelrds"));
        fieldmsvd.setText(controller.getPathDataForKey("labelmsvd"));
        fieldug.setText(controller.getPathDataForKey("labelug"));
        fieldcs.setText(controller.getPathDataForKey("labelcs"));
        fieldctune.setText(controller.getPathDataForKey("labelctune"));
        
        frame.setSize(new Dimension(500,470));
        frame.setVisible(true);
        
    }
    
    private void initsetting(){
        comboswitch.setSelectedIndex(0);
        controller.setKeyWithPathData("labelts","1" );
        controller.setKeyWithPathData("labelps", "0.00001" );
        controller.setKeyWithPathData("labelss","0.925" );
        controller.setKeyWithPathData("labelsis", "57");
        //controller.setKeyWithPathData("labelrds","0.05" );
        controller.setKeyWithPathData("labelmsvd","1,0,1,0" );
        controller.setKeyWithPathData("labelug","mm" );
        controller.setKeyWithPathData("labelcs", "10" );
        controller.setKeyWithPathData("labelctune", "10" );
        controller.setKeyWithPathData("svdcleanon", "Yes" );
        
    }
    /// memory
    private class getheapspace extends TimerTask{    
        public void run() {
            long heapUsedSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long heapMaxSize = Runtime.getRuntime().maxMemory();
            labelheap.setText("Memory used: " + Math.round(heapUsedSize / 1048576.0) + " Mb / " + Math.round(heapMaxSize / 1048576.0) + " Mb");
//            labelheap.setForeground(Color.GREEN);
//            if(heapMaxSize - heapUsedSize < 50){
//                labelheap.setForeground(Color.RED);
//                MessageManager.warn("Memory control => available memory less then 50 Mb",null, null);
//                MessageManager.warn("Memory control => Will try garbage collector", null, null);
//                System.gc();
//            }
//            log.info("totalMemory in kk: " + Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0);
//            log.info("maxMemory   in kk: " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0);
//            log.info("freeMemory  in kk: " + Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0);
//            log.info("usedMem  in kk: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0);
        }
    }
}