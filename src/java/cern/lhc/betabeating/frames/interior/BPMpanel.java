package cern.lhc.betabeating.frames.interior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.ChartRenderer;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.renderer.BarChartRenderer;
import cern.lhc.betabeating.Tools.ArrayHelper;
import cern.lhc.betabeating.Tools.FileIO;
import cern.lhc.betabeating.Tools.ScreenTools;
import cern.lhc.betabeating.datahandler.TFSReader;
import cern.lhc.betabeating.datahandler.Converter;
import cern.lhc.betabeating.datahandler.SddsReader;
import cern.lhc.betabeating.external.ExternalPrograms;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.external.programs.SvdCleanData;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.exterior.SelectionAnal;
import cern.lhc.betabeating.model.SddsFileType;
import cern.lhc.betabeating.model.SddsJListData;

public class BPMpanel extends JPanel {
    private static final long serialVersionUID = 5671779085962670514L;
    private static final Logger log = Logger.getLogger(BPMpanel.class);
    
    private Controller controller = null;
    private SelectionAnal selectionAnal = null;

    public BPMpanel(Controller controller) {
        this.controller = controller;
        selectionAnal = new SelectionAnal(controller);
        listeners();
    }

    /*
     * data handling
     */
    private String twiss;

    private DefaultDataSource dsh = new DefaultDataSource();
    private DefaultDataSource dsv = new DefaultDataSource();

    private Map<String, SddsJListData> dataMap = new HashMap<String, SddsJListData>();
    private Map<String, String> translateMap = new HashMap<String, String>();


    /*
     * Setting env and paths
     */
    private String[] env;

    public void setenv(String[] envin) {
        env = envin;
        selectionAnal.setenv(env);
    }

    public void setapanel(AnalysisPanel analysisPanel) {
        selectionAnal.setAnalysis(analysisPanel);
    }

    private void listeners() {

        // convert
        buttonfile.addActionListener(new ActionListener() {
            public void actionPerformed(@SuppressWarnings("unused") final ActionEvent actionEvent) {
                JFileChooser jFileChooser = new JFileChooser(controller.getBeamSelectionData().getInputPath());
                jFileChooser.setMultiSelectionEnabled(true);
                jFileChooser.setPreferredSize(new Dimension(ScreenTools.getScreenWidth() / 2, ScreenTools.getScreenHeight() * 2 / 3));
                int value = jFileChooser.showOpenDialog(null);

                if (value == JFileChooser.APPROVE_OPTION) {
                    final File[] inputfiles = jFileChooser.getSelectedFiles();
                    MessageManager.getConsoleLogger().info("BPMpanel => You selected " + inputfiles.length + " files");
                    twiss = controller.getPathDataForKey("opticspath") + "/twiss.dat";
                    Task task = new Task() {
                        protected Object construct() {
                            try {
                                for (int i = 0; i < inputfiles.length; i++) {
                                    File choosedFile = inputfiles[i];
                                    MessageManager.getConsoleLogger().info("loading file: " + choosedFile.getAbsolutePath());
                                    if (choosedFile.toString().contains("BunchTurn")) {
                                        int confirmationResult = JOptionPane.showConfirmDialog(null, "BunchTurn loading detected.\n"
                                                + "Wrong BunchTurns can produce massive amounts of wrong BPM\n"
                                                + "data and block the GUI with parsing for some time.\n" + "Do you really want to continue?",
                                                "BunchTurn Detected", JOptionPane.YES_NO_OPTION);
                                        if (confirmationResult == JOptionPane.YES_OPTION)
                                            MessageManager.getConsoleLogger().info("start loading BunchTurn");
                                        else if (confirmationResult == JOptionPane.NO_OPTION)
                                        {
                                            MessageManager.getConsoleLogger().info("skipped: " + choosedFile);
                                            continue;
                                        }
                                        else
                                            MessageManager.getConsoleLogger().warn("should not happen, not clicked yes/no?");
                                    }
                                    if (choosedFile.toString().endsWith("sdds") && (controller.getBeamSelectionData().getAccelerator().contains("LHC"))) {// is sdds format for LHC
                                        MessageManager.getConsoleLogger().info("BPMPanel => reading twiss " + twiss);
                                        MessageManager.getStatusLine().info("BPMpanel => Triggering convert");
                                        // TODO allow import of ascii files
                                        MessageManager.getConsoleLogger().info("info from getConsoleLogger");
                                        SddsFileType sddsFileType = SddsFileType.getTypeFromFile(choosedFile);
                                        System.out.println("fileType: " + sddsFileType);
                                        if (sddsFileType == SddsFileType.ASCII) {
                                            MessageManager.getConsoleLogger().warn(
                                                    "ASCII File detected and skipped, currently not supported. Load binary SDDS files.");
                                            log.info("-- ascii file detected: " + choosedFile);
                                            continue;
                                        }
                                        MessageManager.getConsoleLogger().info("BPMPanel => Running for " + choosedFile.getName());
                                        ConvertAndLoadsdds(choosedFile);

                                    } else if (inputfiles[i].toString().endsWith("zip")) {// zip format (from jorgs application)

                                    } else if (controller.getBeamSelectionData().getAccelerator().contains("SPS")) {
                                        MessageManager.getStatusLine().info("BPMpanel => Triggering convert for SPS");
                                        MessageManager.getConsoleLogger().info("BPMPanel => Running for " + choosedFile.getName());
                                        ConvertAndLoadSPS(choosedFile, i, inputfiles.length);
                                    } else if (controller.getBeamSelectionData().getAccelerator().contains("RHIC")) {
                                        MessageManager.getStatusLine().info("BPMpanel => Triggering convert for RHIC");
                                        MessageManager.getConsoleLogger().info("BPMPanel => Running for " + choosedFile.getName());
                                        ConvertAndLoadRHIC(choosedFile, i, inputfiles.length);
                                    } else if (controller.getBeamSelectionData().getAccelerator().contains("SOLEIL")) {
                                        MessageManager.getStatusLine().info("BPMpanel => Triggering convert for SOLEIL");
                                        MessageManager.getConsoleLogger().info("BPMPanel => Running for " + choosedFile.getName());
                                        ConvertAndLoadSOLEIL(choosedFile, i, inputfiles.length);
                                    } else { // not convert
                                        MessageManager.getStatusLine().warn(
                                                "BPMpanel => Cannot convert for this setup " + controller.getBeamSelectionData().getAccelerator());
                                    }
                                    boxhor.setSelected(false);
                                    boxver.setSelected(false);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                MessageManager.error("BPMPanel => Caused by exception: ", ex, null);
                            }

                            return null;
                        }

                    };
                    task.setName("Converter");
                    task.setCancellable(false);
                    task.start();
                } else {
                    MessageManager.getConsoleLogger().warn("BPMpanel => Cancel button was pushed nothing will happen");
                }
            }
        });

        //
        buttonremove.addActionListener(new ActionListener() {
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent) {
                String key = tablenorth.getValueAt(tablenorth.getSelectedRow(), 0).toString();
                dataMap.remove(key);
                translateMap.remove(key);
                clearBpmDisplayTable();
                tmodel.removeRow(tablenorth.getSelectedRow());
            }
        });
        // list
        listhor.addListSelectionListener(listhlistener = new ListSelectionListener() {
            public void valueChanged(@SuppressWarnings("unused") ListSelectionEvent evt) {
                // if(evt.getValueIsAdjusting()){
                int bpmh = listhor.getSelectedIndex();
                int indx = bpmh + 1;
                if (bpmh == (listhor.getModel().getSize() - 1)) {
                    indx = 0;
                }

                double[] datah = ArrayHelper.convertFloatArrayToDoubleArray(positionsH[bpmh]);
                if (!boxhor.isSelected()) {
                    dsh.setDataSet(0, new DefaultDataSet(listhor.getSelectedValue().toString(), datah));
                    charthor.setRenderingType(ChartRenderer.POLYLINE);
                } else {
                    double[] datax = ArrayHelper.convertFloatArrayToDoubleArray(positionsH[indx]);
                    dsh.setDataSet(0, new DefaultDataSet(listhor.getSelectedValue().toString(), datax, datah));
                    charthor.setRenderingType(ChartRenderer.SCATTER);
                }

                charthor.setDataSource(dsh);
                // charthor.setLegendVisible(true);
                // }
            }
        });
        listver.addListSelectionListener(listvlistener = new ListSelectionListener() {
            public void valueChanged(@SuppressWarnings("unused") ListSelectionEvent evt) {
                int bpmv = listver.getSelectedIndex();
                int indx = bpmv + 1;
                if (bpmv == (listver.getModel().getSize() - 1)) {
                    indx = 0;
                }

                double[] datav = ArrayHelper.convertFloatArrayToDoubleArray(positionsV[bpmv]);
                if (!boxver.isSelected()) {
                    dsv.setDataSet(0, new DefaultDataSet(listver.getSelectedValue().toString(), datav));
                    chartver.setRenderingType(ChartRenderer.POLYLINE);
                } else {
                    double[] datax = ArrayHelper.convertFloatArrayToDoubleArray(positionsV[indx]);
                    dsv.setDataSet(0, new DefaultDataSet(listver.getSelectedValue().toString(), datax, datav));
                    chartver.setRenderingType(ChartRenderer.SCATTER);
                }
                chartver.setDataSource(dsv);
            }
        });

        // / listening to load
        tablenorth.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                log.info("mouse click");
                JTable source = (JTable)mouseEvent.getSource();
                System.out.println(source);
                System.out.println(source.getClass());
                String key = source.getValueAt(tablenorth.getSelectedRow(), 0).toString();
                SddsJListData sddsJListData = dataMap.get(key);
                positionsH = sddsJListData.getPositionsHorizontal();
                positionsV = sddsJListData.getPositionsVertical();

                if (source.getSelectedRowCount() == 1) {
                    MessageManager.getConsoleLogger().info("AnalysisPanel => BPM viewer mode");
                    String[] bpmNamesHorizontal = sddsJListData.getBpmNamesHorizontal();
                    String[] bpmNamesVertical = sddsJListData.getBpmNamesVertical();
                    loadbpmList(bpmNamesHorizontal, bpmNamesVertical);
                } else {
                    MessageManager.getConsoleLogger().info("AnalysisPanel => Analysis mode \n Want to view BPM please select ONE");
                }
            }
        });

        // // tab is changed
        tab.addChangeListener(new ChangeListener() { // This method is called whenever the selected tab changes
            public void stateChanged(@SuppressWarnings("unused") ChangeEvent evt) {
                // log.info("tab changed"); //bpm viever - faultyBpm
                // Get current tab
                if (tab.getSelectedIndex() == 1) {
                    int selections = tablenorth.getRowCount();
                    String[] datafiles = new String[selections];
                    for (int i = 0; i < selections; i++) {
                        datafiles[i] = translateMap.get(tablenorth.getValueAt(i, 0));
                    }
                    faultybpm(datafiles);

                }
            }
        });

        // // save bpms
        savebad.addActionListener(new ActionListener() {
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent) {

                int count = tablenorth.getRowCount();
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter("outfilename"));
                    out.write("@ NFILES %le " + count + "\n");
                    out.write("*NAME    S       PLANE       COUNT       LABEL\n$%s      %le     %s      %le     %s\n");

                    for (int i = 0; i < tablehor.getRowCount(); i++) {

                        String name = tablehor.getValueAt(i, 0).toString();
                        String s = tablehor.getValueAt(i, 1).toString();
                        String plane = "H";
                        String label = tablehor.getValueAt(i, 2).toString();
                        String per = tablehor.getValueAt(i, 3).toString();

                        String total = name + "     " + s + "       " + plane + "       " + per + "     " + label + "\n";

                        out.write(total);
                    }

                    for (int i = 0; i < tablever.getRowCount(); i++) {
                        String name = tablever.getValueAt(i, 0).toString();
                        String s = tablever.getValueAt(i, 1).toString();
                        String plane = "H";
                        String label = tablever.getValueAt(i, 2).toString();
                        String per = tablever.getValueAt(i, 3).toString();

                        String total = name + "     " + s + "   " + plane + "   " + per + "     " + label + "\n";

                        out.write(total);
                    }

                    out.close();

                } catch (IOException e) {
                }
            }
        });

        buttonAnal.addActionListener(new ActionListener() {
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent) {
                MessageManager.info("AnalysisPanel => Opening selection panel", null);

                selectionAnal.showGUI();
                int[] selectedRows = tablenorth.getSelectedRows();
                if (!(selectedRows.length == 0)) {
                    if (selectionAnal.isVisible()) {
                        MessageManager.info("AnalysisPanel => Select correct info and press \"Go \" ", null);

                        final List<String> files2Analyze = new ArrayList<String>();

                        for (int selrowItem : selectedRows)
                            files2Analyze.add(translateMap.get(tablenorth.getValueAt(selrowItem, 0)));
                        selectionAnal.setFiles(files2Analyze);

                    } else {
                        MessageManager.error("AnalysisPanel => Cannot open selection panel  ", null, null);
                        MessageManager.getConsoleLogger().error("AnalysisPanel => Cannot open selection panel \n Contact expert  ", null);
                    }
                } else {
                    MessageManager.getConsoleLogger().warn("AnalysisPanel => Didnt selected any values in the table  ", null);
                }
            }
        });

    }

    void faultybpm(String[] files) {

        Map<String, double[]> badmaph = new HashMap<String, double[]>();
        Map<String, double[]> badmapv = new HashMap<String, double[]>();

        for (int i = 0; i < files.length; i++) {

            TFSReader tfsreaderbad = new TFSReader();
            TFSReader tfsreadersus = new TFSReader();

            // from svd
            if (new File(files[i].replace(".sdds.new", ".sdds.bad")).exists()) {
                tfsreaderbad.loadTable(files[i].replace(".sdds.new", ".sdds.bad"));
                String[] names = tfsreaderbad.getStringData("NAME");
                double[] loc = tfsreaderbad.getDoubleData("S");
                String[] plane = tfsreaderbad.getStringData("PLANE");

                for (int j = 0; j < names.length; j++) {

                    if (plane[j].contains("H")) {
                        if (badmaph.containsKey(names[j])) {
                            double count = badmaph.get(names[j])[1];
                            badmaph.put(names[j], new double[] { loc[j], count + 1, 0 });
                        } else {
                            badmaph.put(names[j], new double[] { loc[j], 1, 0 });
                        }
                    } else {
                        if (badmapv.containsKey(names[j])) {
                            double count = badmapv.get(names[j])[1];
                            badmapv.put(names[j], new double[] { loc[j], count + 1, 0 });
                        } else {
                            badmapv.put(names[j], new double[] { loc[j], 1, 0 });
                        }
                    }
                }
            }
            // from manual cleaner
            if (new File(files[i].replace(".sdds.new", ".sdds.new.badM")).exists()) {
                tfsreadersus.loadTable(files[i].replace(".sdds.new", ".sdds.new.badM"));
                String[] names = tfsreadersus.getStringData("NAME");
                double[] loc = tfsreadersus.getDoubleData("S");
                String[] plane = tfsreadersus.getStringData("PLANE");

                for (int j = 0; j < names.length; j++) {
                    if (plane[j] == "H") {
                        if (badmaph.containsKey(names[j])) {
                            double count = badmaph.get(names[j])[1];
                            badmaph.put(names[j], new double[] { loc[j], count + 1, 1 });
                        } else {
                            badmaph.put(names[j], new double[] { loc[j], 1, 1 });
                        }
                    } else if (plane[j] == "V") {
                        if (badmapv.containsKey(names[j])) {
                            double count = badmapv.get(names[j])[1];
                            badmapv.put(names[j], new double[] { loc[j], count + 1, 1 });
                        } else {
                            badmapv.put(names[j], new double[] { loc[j], 1, 1 });
                        }
                    }
                }
            }
        }

        GraphFaultBPM(badmaph, badmapv, files.length);
    }

    /*
     * convert section
     */

    public HashMap<String, String> datalist = new HashMap<String, String>();
    private HashMap<String, float[][]> dataH = new HashMap<String, float[][]>();
    private HashMap<String, float[][]> dataV = new HashMap<String, float[][]>();
    float[][] positionsH = null;
    float[][] positionsV = null;

    /*
     * convert and load SPS
     */
    private void ConvertAndLoadSPS(final File file, int step, int length) {
        String namefile = null;
        String currentAccelerator = controller.getBeamSelectionData().getAccelerator();
        String currentOutputPath = controller.getBeamSelectionData().getOutputPath();
        String currentDate = controller.getBeamSelectionData().getDate();
        String outputpath = currentOutputPath + "/" + currentDate + "/" + currentAccelerator + "/Measurements/" + file.getName().replace(".sdds", "") + "/";

        boolean isCreateDirectorySuccessFul = FileIO.createDirectory(outputpath);
        if (!isCreateDirectorySuccessFul) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot create dir");
            return;
        }

        boolean isCopyFileSuccessful = FileIO.copyFile(file, new File(outputpath + file.getName()));
        if (!isCopyFileSuccessful) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot copy file");
            return;
        }

        String command = controller.getPathDataForKey("python") + " " + controller.getPathDataForKey("spsconvert") + " " + outputpath + "/" + file.getName()
                + " " + controller.getPathDataForKey("spstranslate");
        MessageManager.getConsoleLogger().info("BPMPanel => Sending the following command to the converter :\n" + command);
        Systemcall.execute(command, "SVD clean SPS", env, outputpath, true);

        namefile = outputpath + "/" + file.getName() + ".sdds.new";

        int systemCallStatus = 0;
        if (controller.getPathDataForKey("svdcleanon").equals("Yes")) {
            String svdturn = controller.getPathDataForKey("labelts");
            String pk2pk = controller.getPathDataForKey("labelps");
            String sumSquare = controller.getPathDataForKey("labelss");
            String svdval = controller.getPathDataForKey("labelsis");
            ExternalPrograms.getNewInstance(controller.getProgramPaths()).executeSvdClean(
                    SvdCleanData.prepareObject().setFile(namefile)
                                                .setTurn(svdturn)
                                                .setP(pk2pk)
                                                .setSumSquare(sumSquare)
                                                .setSing_val(svdval)
                                                .setOutputPath(outputpath).create());
            MessageManager.info("AnalysisPanel => Finished converting and cleaning " + (step + 1) + "/" + length, null);
        } else {
            MessageManager.info("AnalysisPanel => Finished converting " + (step + 1) + "/" + length, null);
        }

        if (systemCallStatus == 0) {
            SddsReader sdds = new SddsReader();
            sdds.loadtable(namefile);

            String[] bpmnamesh = sdds.getBpmsHorizontalAsArray();
            String[] bpmnamesv = sdds.getBpmsVerticalAsArray();
            positionsH = sdds.datahh;
            positionsV = sdds.datavv;
            int nturn = sdds.noturn - 1;
            SddsJListData sddsJListData = new SddsJListData(bpmnamesh, bpmnamesv, positionsH, positionsV, nturn);
            dataMap.put(new File(namefile).getName(), sddsJListData);

            if (!translateMap.containsKey(new File(namefile).getName().toString())) {

                tmodel.addRow(new Object[] { new File(namefile).getName().toString(), "0.0" });
                dataH.put(new File(namefile).getName().toString(), positionsH);
                dataV.put(new File(namefile).getName().toString(), positionsV);
                translateMap.put(new File(namefile).getName().toString(), outputpath + "/" + new File(namefile).getName());

            }

            MessageManager.getConsoleLogger().info(
                    "AnalysisPanel => Finished loading " + bpmnamesh.length + " horizontal BPMs " + +bpmnamesv.length + " vertical BPMs ", null);
        } else {
            MessageManager.error("AnalysisPanel => Problem in converting " + (step + 1) + "/" + length, null, null);
        }
    }

    /*
     * convert and load RHIC
     */
    private void ConvertAndLoadRHIC(final File file, int step, int length) {
        String namefile = null;
        String currentAccelerator = controller.getBeamSelectionData().getAccelerator();
        String currentOutputPath = controller.getBeamSelectionData().getOutputPath();
        String currentDate = controller.getBeamSelectionData().getDate();
        String outputpath = currentOutputPath + "/" + currentDate + "/" + currentAccelerator + "/Measurements/" + file.getName().replace(".sdds", "") + "/";

        boolean isCreateDirectorySuccessFul = FileIO.createDirectory(outputpath);
        if (!isCreateDirectorySuccessFul) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot create dir");
            return;
        }

        boolean isCopyFileSuccessful = FileIO.copyFile(file, new File(outputpath + file.getName() + ".new"));
        if (!isCopyFileSuccessful) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot copy file");
            return;
        }

        namefile = outputpath + "/" + file.getName() + ".new";

        int systemCallStatus = 0;
        if (controller.getPathDataForKey("svdcleanon").equals("Yes")) {
            String svdturn = controller.getPathDataForKey("labelts");
            String pk2pk = controller.getPathDataForKey("labelps");
            String sumSquare = controller.getPathDataForKey("labelss");
            String svdval = controller.getPathDataForKey("labelsis");
            ExternalPrograms.getNewInstance(controller.getProgramPaths()).executeSvdClean(
                    SvdCleanData.prepareObject().setFile(namefile)
                                                .setTurn(svdturn)
                                                .setP(pk2pk)
                                                .setSumSquare(sumSquare)
                                                .setSing_val(svdval)
                                                .setOutputPath(outputpath).create());
            MessageManager.info("AnalysisPanel => Finished converting and cleaning " + (step + 1) + "/" + length, null);
        } else
            MessageManager.info("AnalysisPanel => Finished converting " + (step + 1) + "/" + length, null);

        if (systemCallStatus == 0) {
            SddsReader sdds = new SddsReader();
            sdds.loadtable(namefile);

            String[] bpmnamesh = sdds.getBpmsHorizontalAsArray();
            String[] bpmnamesv = sdds.getBpmsVerticalAsArray();
            positionsH = sdds.datahh;
            positionsV = sdds.datavv;
            int nturn = sdds.noturn - 1;
            SddsJListData sddsJListData = new SddsJListData(bpmnamesh, bpmnamesv, positionsH, positionsV, nturn);
            dataMap.put(new File(namefile).getName(), sddsJListData);

            if (!translateMap.containsKey(new File(namefile).getName().toString())) {

                tmodel.addRow(new Object[] { new File(namefile).getName().toString(), "0.0" });
                dataH.put(new File(namefile).getName().toString(), positionsH);
                dataV.put(new File(namefile).getName().toString(), positionsV);
                translateMap.put(new File(namefile).getName().toString(), outputpath + "/" + new File(namefile).getName());

            }

            MessageManager.getConsoleLogger().info(
                    "AnalysisPanel => Finished loading " + bpmnamesh.length + " horizontal BPMs " + +bpmnamesv.length + " vertical BPMs ", null);
        } else {
            MessageManager.error("AnalysisPanel => Problem in converting " + (step + 1) + "/" + length, null, null);
        }
    }

    /*
     * convert and load SOLEIL
     */
    private void ConvertAndLoadSOLEIL(final File file, int step, int length) {
        String namefile = null;
        String currentAccelerator = controller.getBeamSelectionData().getAccelerator();
        String currentOutputPath = controller.getBeamSelectionData().getOutputPath();
        String currentDate = controller.getBeamSelectionData().getDate();
        String outputpath = currentOutputPath + "/" + currentDate + "/" + currentAccelerator + "/Measurements/" + file.getName().replace(".sdds", "") + "/";

        boolean isCreateDirectorySuccessFul = FileIO.createDirectory(outputpath);
        if (!isCreateDirectorySuccessFul) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot create dir");
            return;
        }

        boolean isCopyFileSuccessful = FileIO.copyFile(file, new File(outputpath + file.getName() + ".new"));
        if (!isCopyFileSuccessful) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot copy file");
            return;
        }

        namefile = outputpath + "/" + file.getName() + ".new";

        int systemCallStatus = 0;
        if (controller.getPathDataForKey("svdcleanon").equals("Yes")) {
            String svdturn = controller.getPathDataForKey("labelts");
            String pk2pk = controller.getPathDataForKey("labelps");
            String sumSquare = controller.getPathDataForKey("labelss");
            String svdval = controller.getPathDataForKey("labelsis");
            ExternalPrograms.getNewInstance(controller.getProgramPaths()).executeSvdClean(
                    SvdCleanData.prepareObject().setFile(namefile)
                                                .setTurn(svdturn)
                                                .setP(pk2pk)
                                                .setSumSquare(sumSquare)
                                                .setSing_val(svdval)
                                                .setOutputPath(outputpath).create());
            MessageManager.info("AnalysisPanel => Finished converting and cleaning " + (step + 1) + "/" + length, null);
        } else {
            MessageManager.info("AnalysisPanel => Finished converting " + (step + 1) + "/" + length, null);
        }

        if (systemCallStatus == 0) {
            SddsReader sdds = new SddsReader();
            sdds.loadtable(namefile);

            String[] bpmnamesh = sdds.getBpmsHorizontalAsArray();
            String[] bpmnamesv = sdds.getBpmsVerticalAsArray();
            positionsH = sdds.datahh;
            positionsV = sdds.datavv;
            int nturn = sdds.noturn - 1;
            SddsJListData sddsJListData = new SddsJListData(bpmnamesh, bpmnamesv, positionsH, positionsV, nturn);
            dataMap.put(new File(namefile).getName(), sddsJListData);

            if (!translateMap.containsKey(new File(namefile).getName().toString())) {

                tmodel.addRow(new Object[] { new File(namefile).getName(), "0.0" });
                dataH.put(new File(namefile).getName(), positionsH);
                dataV.put(new File(namefile).getName(), positionsV);
                translateMap.put(new File(namefile).getName(), outputpath + "/" + new File(namefile).getName());

            }

            MessageManager.getConsoleLogger().info(
                    "AnalysisPanel => Finished loading " + bpmnamesh.length + " horizontal BPMs " + +bpmnamesv.length + " vertical BPMs ", null);
        } else {
            MessageManager.error("AnalysisPanel => Problem in converting " + (step + 1) + "/" + length, null, null);
        }
    }

    /*
     * convert and load LHC
     */
    void ConvertAndLoadsdds(final File file) {
        log.info(">> ");
        String currentAccelerator = controller.getBeamSelectionData().getAccelerator();
        String currentOutputPath = controller.getBeamSelectionData().getOutputPath();
        String currentDate = controller.getBeamSelectionData().getDate();
        String outputpath = currentOutputPath + "/" + currentDate + "/" + currentAccelerator + "/Measurements/" + file.getName().replace(".sdds", "") + "/";
        Converter convert = new Converter();
        convert.readtwiss(twiss);
        log.info("-- convert.readtwiss done");

        boolean isCreateDirectorySuccessFull = FileIO.createDirectory(outputpath);
        if (!isCreateDirectorySuccessFull) {
            MessageManager.getConsoleLogger().warn("BPMPanel => Aborting converter, cannot create dir: " + outputpath);
            return;
        }

        MessageManager.getConsoleLogger().info("BPMPanel => Dir created");

        try {
            convert.readfile(file, outputpath + file.getName());
        } catch (IOException e) {
            MessageManager.getConsoleLogger().warn("BPMPanel => convert.readfile failed.", e);
        }
        log.info("-- convert.readfile done");
        // so, if we have already converted the file and want to load the ascii, we can start here
        /*
         * we dont know without convert:
         * -validBunchIds: needed for loop
         * -bpmnamesh = convert.bpmNames; needed for JList
         * -bpmnamesv = convert.bpmNames; needed for JList
         * -bunchidArray: needed for loop
         * -positionsH = convert.dataperbunchH.get(bunchID); added to list
         * -positionsV = convert.dataperbunchV.get(bunchID); added to list
         */

        int numberOfTurns = convert.numberOfTurns;
        String[] bpmnamesh = convert.bpmNames;
        String[] bpmnamesv = convert.bpmNames;
        int[] bunchidArray = convert.bunchIds;
        List<Integer> validBunchIds = convert.validBunchIds;
        

        for (Integer validBunchIdsItem : validBunchIds) {
            log.info("-- doing for loop, bunchID:" + validBunchIdsItem);
            String fileNameWithBunchId = file.getName().replace(".sdds", "_" + bunchidArray[validBunchIdsItem] + ".sdds");
            String fileNameWithBunchIdNew = fileNameWithBunchId + ".new";
            String pathname = outputpath + fileNameWithBunchIdNew;
            log.info("-- pathname:" + pathname);
            boolean isCopyFileSuccessful = FileIO.copyFile(new File(outputpath + fileNameWithBunchId), new File(pathname));
            if (!isCopyFileSuccessful)
                MessageManager.getConsoleLogger().warn("BPMPanel => copy.copyFile failed.");

            if (controller.getPathDataForKey("svdcleanon").equals("Yes")) {
                log.info("-- in if");

                int systemCallStatus = 0;
                // if (!new File(outputpath + "/command.run").exists()) //not working because different parameters can be used for the same file in one run
                // {
                String svdturn = controller.getPathDataForKey("labelts");
                String pk2pk = controller.getPathDataForKey("labelps");
                String sumSquare = controller.getPathDataForKey("labelss");
                String svdval = controller.getPathDataForKey("labelsis");
                ExternalPrograms.getNewInstance(controller.getProgramPaths()).executeSvdClean(
                        SvdCleanData.prepareObject().setFile(pathname)
                                                    .setTurn(svdturn)
                                                    .setP(pk2pk)
                                                    .setSumSquare(sumSquare)
                                                    .setSing_val(svdval)
                                                    .setOutputPath(outputpath).create());
                // }
                // else
                // log.info("no python call needed, file exists: " + outputpath + "/command.run");

                if (systemCallStatus == 0) {
                    SddsReader sdds = new SddsReader();
                    log.info("-- sdds.loadtable start");
                    sdds.loadtable(pathname);
                    log.info("-- sdds.loadtable finished");
                    bpmnamesh = sdds.getBpmsHorizontalAsArray();
                    bpmnamesv = sdds.getBpmsVerticalAsArray();
                    positionsH = sdds.datahh;
                    positionsV = sdds.datavv;
                }

            } else {
                log.info("-- in else");
                positionsH = convert.dataperbunchH.get(validBunchIdsItem);
                positionsV = convert.dataperbunchV.get(validBunchIdsItem);
            }
            SddsJListData sddsJListData = new SddsJListData(bpmnamesh, bpmnamesv, positionsH, positionsV, numberOfTurns);
            dataMap.put(fileNameWithBunchIdNew, sddsJListData);

            if (!translateMap.containsKey(fileNameWithBunchIdNew)) {
                log.info("-- key not in translate");
                tmodel.addRow(new Object[] { fileNameWithBunchIdNew, "0.0" });
                dataH.put(fileNameWithBunchId, positionsH);
                dataV.put(fileNameWithBunchId, positionsV);
                translateMap.put(fileNameWithBunchIdNew, outputpath + "/" + fileNameWithBunchIdNew);
                log.info(outputpath + "/" + fileNameWithBunchIdNew);
            }
            log.info("-- for loop finished");
        }
        log.info("<< ");
    }

    private ListSelectionListener listhlistener;
    private ListSelectionListener listvlistener;
    
    private void clearBpmDisplayTable() {
        String[] emptyStringArray = new String[0];
        loadbpmList(emptyStringArray, emptyStringArray);
    }

    private void loadbpmList(String[] bpmnameshList, String[] bpmnamesvList) {
        log.info("-- bpmnameshList.length:" + bpmnameshList.length + ", bpmnamesvList.length:" + bpmnamesvList.length);
        updateBPMforJListAndListener(bpmnameshList, listhor, listhlistener);
        updateBPMforJListAndListener(bpmnamesvList, listver, listvlistener);
    }

    private void updateBPMforJListAndListener(String[] bpmList, JList list, ListSelectionListener listener) {
        list.removeListSelectionListener(listener);
        list.setListData(bpmList);
        list.updateUI();
        list.addListSelectionListener(listener);

        if (bpmList.length > 0)
            list.setSelectedIndex(0);
    }

    /*
     * graphics
     */

    private JPanel panelnorth = new JPanel();
    private JTabbedPane tab = new JTabbedPane();

    private JButton buttonfile = new JButton("Open files");
    private JButton buttonremove = new JButton("Remove");
    private JButton buttonAnal = new JButton("Do analysis");

    // bpm view
    private JPanel panelhor = new JPanel();
    private JPanel panelver = new JPanel();
    private JList listhor = new JList();
    private JScrollPane scrollpanehor = new JScrollPane(listhor);
    private JList listver = new JList();
    private JScrollPane scrollpanever = new JScrollPane(listver);
    private Chart charthor = new Chart();
    private Chart chartver = new Chart();
    private JSplitPane splitPane;
    private JCheckBox boxhor = new JCheckBox(" Phase space");
    private JCheckBox boxver = new JCheckBox(" Phase space");

    // main north view
    private DefaultTableModel tmodel = new DefaultTableModel() { //need this inner anonymous class to override the isCellEditable method (tbach)
        private static final long serialVersionUID = 471582167819335455L;

        @Override
        public boolean isCellEditable(@SuppressWarnings("unused") int row, int column) {
            return (column != 0); // if column not null, one can edit
        }
    };
    public JTable tablenorth = new JTable(tmodel); //TODO should be private (tbach)
    private JScrollPane scrollpane1 = new JScrollPane(tablenorth);

    // faulty bpm
    private DefaultTableModel tmodelhor = new DefaultTableModel();
    private JTable tablehor = new JTable(tmodelhor);
    private JScrollPane scrollpanehorf = new JScrollPane(tablehor);
    private Chart charthorf = new Chart();

    private DefaultTableModel tmodelver = new DefaultTableModel();
    private JTable tablever = new JTable(tmodelver);
    private JScrollPane scrollpaneverf = new JScrollPane(tablever);
    private Chart chartverf = new Chart();

    private JButton savebad = new JButton("Save bad BPMs to file");
    private JSplitPane splitPane2;
    private JPanel panelfaulty = new JPanel();
    private JPanel savebadpanel = new JPanel();
    private JPanel panelhorf = new JPanel();
    private JPanel panelverf = new JPanel();
    private BarChartRenderer barrenh = new BarChartRenderer();
    private BarChartRenderer barrenv = new BarChartRenderer();

    private GridBagConstraints gridconstrainer(int top, int left, int bottom, int right, int gridwidth, int gridheight, int gridx, int gridy) {
        GridBagConstraints gridCons = new GridBagConstraints();
        gridCons.insets = new Insets(top, left, bottom, right);
        gridCons.anchor = GridBagConstraints.CENTER;
        gridCons.gridwidth = gridwidth;
        gridCons.gridheight = gridheight;
        gridCons.gridx = gridx;
        gridCons.gridy = gridy;
        gridCons.fill = GridBagConstraints.BOTH;

        return gridCons;
    }

    public void createGUI() {
        // general
        setLayout(new BorderLayout());
        double[] xxx = { 0 };

        // tmodel
        tmodel.addColumn("File");
        tmodel.addColumn("dp/p");
        tablenorth.getColumnModel().getColumn(0).setPreferredWidth(600);
        tablenorth.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablenorth.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablenorth.setEditingColumn(1);
        tablenorth.setDragEnabled(false);
        listhor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listver.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // north
        GridBagLayout gridBag1 = new GridBagLayout();
        panelnorth.setLayout(gridBag1);
        panelnorth.setPreferredSize(new Dimension(550, 200));
        gridBag1.rowWeights = new double[] { 1, 1, 1, 1 };
        gridBag1.rowHeights = new int[] { 70, 70, 100, 70 };
        gridBag1.columnWeights = new double[] { 1, 1 };
        gridBag1.columnWidths = new int[] { 50, 500 };
        gridBag1.setConstraints(buttonfile, gridconstrainer(10, 20, 5, 70, 1, 1, 0, 0));
        gridBag1.setConstraints(buttonremove, gridconstrainer(10, 20, 5, 70, 1, 1, 0, 1));
        gridBag1.setConstraints(buttonAnal, gridconstrainer(5, 20, 10, 70, 1, 1, 0, 3));
        gridBag1.setConstraints(scrollpane1, gridconstrainer(5, 2, 2, 2, 1, 4, 1, 0));
        panelnorth.add(buttonfile);
        buttonremove.setBackground(Color.red);
        panelnorth.add(buttonremove);
        buttonAnal.setBackground(Color.green);
        panelnorth.add(buttonAnal);
        panelnorth.add(scrollpane1);

        // center

        // panelbpmview
        // hor
        GridBagLayout gridBag2 = new GridBagLayout();
        panelhor.setPreferredSize(new Dimension(210, 200));
        panelhor.setLayout(gridBag2);
        gridBag2.rowWeights = new double[] { 1, 0.8 };
        gridBag2.rowHeights = new int[] { 170, 30 };
        gridBag2.columnWeights = new double[] { 0.8, 1 };
        gridBag2.columnWidths = new int[] { 180, 500 };
        gridBag2.setConstraints(scrollpanehor, gridconstrainer(30, 30, 30, 20, 1, 1, 0, 0));
        gridBag2.setConstraints(boxhor, gridconstrainer(5, 30, 5, 20, 1, 1, 0, 1));
        gridBag2.setConstraints(charthor, gridconstrainer(0, 0, 0, 0, 1, 2, 1, 0));
        dsh.addDataSet(0, new DefaultDataSet("l", xxx, xxx));
        charthor.setDataSource(dsh);
        charthor.setLegendVisible(false);
        charthor.setInteractors(ChartInteractor.createEditIteractors());
        panelhor.setBorder(new TitledBorder("Horizontal plane"));
        panelhor.add(scrollpanehor);
        panelhor.add(boxhor);
        panelhor.add(charthor);

        // ver
        GridBagLayout gridBag3 = new GridBagLayout();
        panelver.setPreferredSize(new Dimension(210, 200));
        panelver.setLayout(gridBag3);
        gridBag3.rowWeights = new double[] { 1, 0.8 };
        gridBag3.rowHeights = new int[] { 170, 30 };
        gridBag3.columnWeights = new double[] { 0.8, 1 };
        gridBag3.columnWidths = new int[] { 180, 500 };
        gridBag3.setConstraints(scrollpanever, gridconstrainer(30, 30, 30, 20, 1, 1, 0, 0));
        gridBag3.setConstraints(boxver, gridconstrainer(5, 30, 5, 20, 1, 1, 0, 1));
        gridBag3.setConstraints(chartver, gridconstrainer(0, 0, 0, 0, 1, 2, 1, 0));
        dsv.addDataSet(0, new DefaultDataSet("l", xxx, xxx));
        chartver.setDataSource(dsv);
        chartver.setLegendVisible(false);
        chartver.setInteractors(ChartInteractor.createEditIteractors());
        panelver.setBorder(new TitledBorder("Vertical plane"));
        panelver.add(scrollpanever);
        panelver.add(boxver);
        panelver.add(chartver);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelhor, panelver);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        // panelbpmfaulty

        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelhorf, panelverf);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setResizeWeight(0.5);
        panelfaulty.setLayout(new BorderLayout());
        panelfaulty.add(splitPane2, BorderLayout.CENTER);
        savebadpanel.add(new JLabel("    "));
        savebadpanel.add(savebad);
        savebadpanel.add(new JLabel("    "));
        panelfaulty.add(savebadpanel, BorderLayout.SOUTH);

        // hor
        tmodelhor.addColumn("BPM");
        tmodelhor.addColumn("S");
        tmodelhor.addColumn("LABEL");
        tmodelhor.addColumn("%");
        tablehor.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablehor.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablehor.getColumnModel().getColumn(2).setPreferredWidth(10);
        tablehor.getColumnModel().getColumn(3).setPreferredWidth(10);
        GridBagLayout gridBag4 = new GridBagLayout();
        panelhorf.setPreferredSize(new Dimension(450, 200));
        panelhorf.setLayout(gridBag4);
        gridBag4.rowWeights = new double[] { 1 };
        gridBag4.rowHeights = new int[] { 200 };
        gridBag4.columnWeights = new double[] { 0.8, 1 };
        gridBag4.columnWidths = new int[] { 100, 400 };
        gridBag4.setConstraints(scrollpanehorf, gridconstrainer(30, 30, 30, 20, 1, 1, 0, 0));
        gridBag4.setConstraints(charthorf, gridconstrainer(5, 30, 5, 20, 1, 1, 1, 0));
        charthorf.setInteractors(ChartInteractor.createEditIteractors());
        charthorf.setDataSet(new DefaultDataSet("BPM Viewer", xxx, xxx));
        panelhorf.setBorder(new TitledBorder("Horizontal plane"));
        panelhorf.add(scrollpanehorf);
        panelhorf.add(charthorf);
        barrenh.setStyles(new Style[] { new Style(Color.BLUE), new Style(Color.GREEN) });
        barrenh.setWidthPercent(5);
        charthorf.addRenderer(barrenh);

        // ver
        tmodelver.addColumn("BPM");
        tmodelver.addColumn("S");
        tmodelver.addColumn("LABEL");
        tmodelver.addColumn("%");
        tablever.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablever.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablever.getColumnModel().getColumn(2).setPreferredWidth(10);
        tablever.getColumnModel().getColumn(3).setPreferredWidth(10);
        GridBagLayout gridBag5 = new GridBagLayout();
        panelverf.setPreferredSize(new Dimension(450, 200));
        panelverf.setLayout(gridBag5);
        gridBag5.rowWeights = new double[] { 1 };
        gridBag5.rowHeights = new int[] { 200 };
        gridBag5.columnWeights = new double[] { 0.8, 1 };
        gridBag5.columnWidths = new int[] { 100, 400 };
        gridBag5.setConstraints(scrollpaneverf, gridconstrainer(30, 30, 30, 20, 1, 1, 0, 0));
        gridBag5.setConstraints(chartverf, gridconstrainer(5, 30, 5, 20, 1, 1, 1, 0));
        chartverf.setInteractors(ChartInteractor.createEditIteractors());
        chartverf.setDataSet(new DefaultDataSet("BPM Viewer", xxx, xxx));
        panelverf.setBorder(new TitledBorder("vertical plane"));
        panelverf.add(scrollpaneverf);
        panelverf.add(chartverf);
        barrenv.setStyles(new Style[] { new Style(Color.BLUE), new Style(Color.green) });
        barrenv.setWidthPercent(5);
        chartverf.addRenderer(barrenv);

        // tab
        tab.addTab("BPM viewer", splitPane);
        tab.addTab("Faulty BPM", panelfaulty);

        // add
        add(panelnorth, BorderLayout.NORTH);
        add(tab, BorderLayout.CENTER);
        setVisible(true);
    }

    /*
     * graphics for faulty bpm
     */
    private void GraphFaultBPM(Map<String, double[]> bpmh, Map<String, double[]> bpmv, int length) {
        String[] nameh = new String[bpmh.size()];
        String[] namev = new String[bpmv.size()];
        double[] counth = new double[bpmh.size()];
        double[] countv = new double[bpmv.size()];
        double[] loch = new double[bpmh.size()];
        double[] locv = new double[bpmv.size()];

        while (tmodelhor.getRowCount() > 0) {
            tmodelhor.removeRow(0);
        }

        while (tmodelver.getRowCount() > 0) {
            tmodelver.removeRow(0);
        }

        Iterator<String> it = bpmh.keySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            String name = it.next();

            String label = null;
            double[] data = bpmh.get(name);

            nameh[count] = name;
            counth[count] = ((data[1] / length) * 100);
            loch[count] = data[0];

            if (data[2] == 0) {
                label = "SVD";
            } else {
                label = "SUSSIX";
            }

            tmodelhor.addRow(new Object[] { name, data[0], label, ((data[1] / length) * 100) });

            count++;
        }

        it = bpmv.keySet().iterator();
        count = 0;
        while (it.hasNext()) {
            String name = it.next().toString();

            String label = null;
            double[] data = bpmv.get(name);

            namev[count] = name;
            countv[count] = ((data[1] / length) * 100);
            locv[count] = data[0];

            if (data[2] == 0) {
                label = "SVD";
            } else {
                label = "SUSSIX";
            }

            tmodelver.addRow(new Object[] { name, data[0], label, ((data[1] / length) * 100) });

            count++;
        }

        DefaultDataSource dshb = new DefaultDataSource();
        dshb.addDataSet(new DefaultDataSet("Faulty H", loch, counth));
        barrenh.setDataSource(dshb);

        DefaultDataSource dsvb = new DefaultDataSource();
        dsvb.addDataSet(new DefaultDataSet("Faulty V", locv, countv));
        barrenv.setDataSource(dsvb);
    }
}
