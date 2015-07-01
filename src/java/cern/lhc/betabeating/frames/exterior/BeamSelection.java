package cern.lhc.betabeating.frames.exterior;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


import cern.lhc.betabeating.Tools.ScreenTools;
import cern.lhc.betabeating.external.ProgramPaths;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.model.BeamSelectionData;

/**
 * Displays a dialog to choose the initial values.
 * 
 * @author tbach
 */
public class BeamSelection extends JFrame {
    private static final long serialVersionUID = -6551127365517891814L;
    
    private final Controller controller;

    private static final int windowWidth = 400;
    private static final int windowHeight = 470;

    private final JComboBox jComboBoxBeam = new JComboBox(new String[] { "LHCB1", "LHCB2", "SPS", "RHICB", "RHICY", "SOLEIL" });
    private final JComboBox jComboBoxOutput = new JComboBox(new String[] { "LHC-Betabeat", "SPS-Betabeat", "Other" });
    private final JComboBox jComboBoxOptics = new JComboBox(new String[] { "External" });
    private final JComboBox jComboBoxInput = new JComboBox(new String[] { "LHC-Fill", "SPS-Fill", "Other" });
    private final JComboBox jComboBoxProgramLocation = new JComboBox(new String[] { ProgramPaths.defaultBetaBeatingPath, "Other" });

    private final JButton jButtonGo = new JButton("Go");

    public BeamSelection(final Controller controller) {
        super("Beta-beat selection");
        this.controller = controller;
    }

    public void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final int screenWidth = ScreenTools.getScreenWidth();
        final int screenHeight = ScreenTools.getScreenHeight();

        setLocation((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2); // place it in the mid

        setLayout(new GridLayout(0, 1));
        
        addLabelComboboxTooltipWithSpacer("Beam:", jComboBoxBeam, "Select accelerator/beam that will be used.");
        addLabelComboboxTooltipWithSpacer("Output:", jComboBoxOutput, "Select the path were that the data will be stored. Must be writable.");
        addLabelComboboxTooltipWithSpacer("Optics:", jComboBoxOptics, "Free to choose. No useful information here. Thanks for reading this Tooltip.");
        addLabelComboboxTooltipWithSpacer("Input:", jComboBoxInput, "Select the path to the fill dir. Must be readable.");
        addLabelComboboxTooltipWithSpacer("Location for Program dir (Optional):", jComboBoxProgramLocation, "Select the path to the program dir.");

        add(jButtonGo);
        
        doDeveloperThings();

        setSize(windowWidth, windowHeight);
        addListeners();
        setVisible(true);
    }
    
    private static final String tooltipLabelText = " ? ";
    private void addLabelComboboxTooltipWithSpacer(String labelText, JComboBox jComboBox, String tooltip)
    {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        
        add(new JLabel(labelText));
        
        JPanel innerPanel = new JPanel(new GridBagLayout());
        jComboBox.setToolTipText(getComboBoxData(jComboBox.getSelectedItem().toString()));
        innerPanel.add(jComboBox, gridBagConstraints);
        JLabel tooltipLabel = new JLabel(tooltipLabelText);
        tooltipLabel.setFont(new Font(tooltipLabel.getFont().getName(), Font.BOLD, tooltipLabel.getFont().getSize() + 4)); //increase size for better recognition
        tooltipLabel.setToolTipText(tooltip);
        gridBagConstraints.weightx = 0;
        innerPanel.add(tooltipLabel, gridBagConstraints);
        add(innerPanel);
        
        add(new JLabel());
    }
    
    private void doDeveloperThings()
    {
        if (System.getProperty("user.name").equals("tbach"))
        {
            String homePath = "/afs/cern.ch/user/t/tbach/";
            jButtonGo.setText("Go Developer");
            
            String inputPath = homePath + "lhc_data_new/";
            jComboBoxInput.addItem(inputPath);
            jComboBoxInput.setSelectedItem(inputPath);
            jComboBoxInput.setToolTipText(inputPath);
            
            String outPath = homePath + "temp/";
            jComboBoxOutput.addItem(outPath);
            jComboBoxOutput.setSelectedItem(outPath);
            jComboBoxOutput.setToolTipText(inputPath);
        }
    }

    private void addListeners() {
        jComboBoxBeam.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                String selectedString = ((JComboBox) actionEvent.getSource()).getSelectedItem().toString();
                if (selectedString.contains("LHC")) {
                    jComboBoxInput.setSelectedItem("LHC-Fill");
                    jComboBoxOutput.setSelectedItem("LHC-Betabeat");
                } else if (selectedString.contains("SPS")) {
                    jComboBoxInput.setSelectedItem("SPS-Fill");
                    jComboBoxOutput.setSelectedItem("SPS-Betabeat");
                }
            }
        });

        addLlistenerForFilechooser(jComboBoxInput);
        addLlistenerForFilechooser(jComboBoxOutput);
        addLlistenerForFilechooser(jComboBoxProgramLocation);

        jButtonGo.addActionListener(new ActionListener() {
            public void actionPerformed(@SuppressWarnings("unused") final ActionEvent actionEvent) {
                final String accelerator = jComboBoxBeam.getSelectedItem().toString();
                final String input = getComboBoxData(jComboBoxInput.getSelectedItem().toString());
                final String output = getComboBoxData(jComboBoxOutput.getSelectedItem().toString());
                String optics = jComboBoxOptics.getSelectedItem().toString();
                String programLocation = jComboBoxProgramLocation.getSelectedItem().toString();
                BeamSelectionData beamSelectionData = new BeamSelectionData(accelerator, input, output, optics, programLocation);

                String errorMessage = beamSelectionData.checkIfValid();
                if (errorMessage.length() > 0)
                    JOptionPane.showMessageDialog(new JPanel(), errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                else
                {
                    controller.showMainProgram(beamSelectionData);
                    controller.destroyBeamSelection();
                }
            }
        });
    }
    
    private void addLlistenerForFilechooser(JComboBox jComboBox) {
        jComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                JComboBox source = (JComboBox) actionEvent.getSource(); 
                if (source.getSelectedItem().toString().equals("Other"))
                    fileChooser(source);
            }
        });
    }
    
    private String getComboBoxData(String selected)
    {
        if (selected.equals("LHC-Betabeat"))
            return "/user/slops/data/LHC_DATA/OP_DATA/Betabeat";
        else if (selected.equals("SPS-Betabeat"))
            return "/nfs/cs-ccr-nfs4/sps_data/OP_DATA/multit/betabeat/";
        
        else if (selected.equals("LHC-Fill"))
            return "/nfs/cs-ccr-nfs4/lhc_data/OP_DATA/FILL_DATA/FILL_DIR/BPM/";
        
        else if (selected.equals("SPS-Fill"))
            return "/nfs/cs-ccr-nfs4/sps_data/OP_DATA/multit/";
        
        else
            return selected;
    }

    private void fileChooser(JComboBox jComboBox) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setPreferredSize(new Dimension(ScreenTools.getScreenWidth() / 2, ScreenTools.getScreenHeight() * 2 / 3));
        int value = jFileChooser.showOpenDialog(null);

        if (value == JFileChooser.APPROVE_OPTION) {
            String path = jFileChooser.getSelectedFile().getPath();
            jComboBox.addItem(path);
            jComboBox.setSelectedItem(path);
        } else {
            jComboBox.setSelectedIndex(0);
        }
        jComboBox.setToolTipText(getComboBoxData(jComboBox.getSelectedItem().toString()));
    }
}