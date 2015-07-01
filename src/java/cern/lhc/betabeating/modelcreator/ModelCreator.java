package cern.lhc.betabeating.modelcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.lhc.betabeating.Tools.FileIO;
import cern.lhc.betabeating.constants.Analysis_const;
import cern.lhc.betabeating.frames.Controller;

public class ModelCreator extends JFrame {
    private static final long serialVersionUID = -8524971631980061642L;
    private static final Logger log = Logger.getLogger(ModelCreator.class);

    private Controller controller = null;
    private Analysis_const analcon= new Analysis_const();
	private Madx_class clmadx;

	private String path;
	
	private JLabel opticslabel;
	public void setlabel(JLabel labeltemp){
		opticslabel=labeltemp;
	}
	
	private String[] env;

	public void setenv(String[] envin){
		env=envin;
	}
	
	
	public ModelCreator(Controller controller){
		super("Model creator");
		this.controller = controller;
		clmadx = new Madx_class(controller);
		path = controller.getPathDataForKey("modelpath");
		listeners();
	}
	
	/*
	 * initial
	 */
	
	public void showGUI(){
		createGUI();
		if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
			field00.addItem("Nominal");
			field00.addItem("ATS");
			field00.addItem("High-beta");
			field00.addItem("Default");
			field00.setSelectedItem("Nominal");
			field0m.addItem("New");
		}
	}
	
    public void runmadx(final ArrayList dpp) {
        Task task = new Task() {
            protected Object construct() {
                try {
                    controller.setKeyWithPathData("%INCLUDE", "");
                    path = controller.getPathDataForKey("opticspath");
                    FileIO.createDirectory(path);

                    for (int i = 0; i < dpp.size(); i++) {
                        controller.setKeyWithPathData("%DPP", dpp.get(i).toString());
                        clmadx.writemadx(path, env);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageManager.error("Model Creator => Caused by exception: ", ex, null);
                    MessageManager.error("Model Creator => Error", null, null);
                }
                return null;
            }
        };
        task.setName("Model creator for opticspanel!");
        task.setCancellable(false);
        task.start();
    }
	
	/*
	 * listeners
	 */
	private String create="no";
	
	private void listeners(){
		//cancel
		buttoncancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			setVisible(false);
			dispose();
		}});
		//go
		buttoncreate.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			
			//path
			path=path+"/"+field0m.getSelectedItem().toString();
			controller.setKeyWithPathData("%PATH",path );
			
			
			if(controller.getBeamSelectionData().getAccelerator().contains("LHC") && create.contains("yes")){
						if(field00.getSelectedItem().toString().equals("Nominal")){
							controller.setKeyWithPathData("STR","db5/V6.5.inj.str");
							controller.setKeyWithPathData("IP1",pathoptics+pathip1 +field01.getSelectedItem().toString());
							controller.setKeyWithPathData("IP2",pathoptics+pathip2 +field02.getSelectedItem().toString());
							controller.setKeyWithPathData("IP5",pathoptics+pathip5 +field03.getSelectedItem().toString());
							controller.setKeyWithPathData("IP8",pathoptics+pathip8 +field04.getSelectedItem().toString());
							controller.setKeyWithPathData("%MATCHER","!");
						}else if(field00.getSelectedItem().toString().equals("High-beta")){
							controller.setKeyWithPathData("STR","db5/V6.5.inj.str");
							controller.setKeyWithPathData("IP1",hibeta_string +field01.getSelectedItem().toString());
							controller.setKeyWithPathData("IP2","injection");
							controller.setKeyWithPathData("IP5",hibeta_string +field03.getSelectedItem().toString());
							controller.setKeyWithPathData("IP8","injection");
							controller.setKeyWithPathData("%MATCHER","");
						}else if(field00.getSelectedItem().toString().equals("ATS")){
							controller.setKeyWithPathData("%MATCHER","!");
							controller.setKeyWithPathData("STR","ats/ats_V6.503.inj.str");
							controller.setKeyWithPathData("IP1",ats_string+ats_squeeze+field01.getSelectedItem().toString());
							controller.setKeyWithPathData("IP2","injection");
							controller.setKeyWithPathData("IP5","injection");
							controller.setKeyWithPathData("IP8","injection");			
						}
						
						// ac-dipole switch
						if(field15.isSelected()){
							controller.setKeyWithPathData("%STOP","");
						}else{
							controller.setKeyWithPathData("%STOP","stop;");				
						}
						
									
						if(Double.parseDouble(field20.getText())!=0.0){
							controller.setKeyWithPathData("%INCLUDE","");
							controller.setKeyWithPathData("%DPP",field20.getText());
						}else{
							controller.setKeyWithPathData("%INCLUDE","!");
							controller.setKeyWithPathData("%DPP",field20.getText());
						}
						
						controller.setKeyWithPathData("%QX",field11.getText() );
						
						if(field00.getSelectedItem().toString().equals("ATS")){
							controller.setKeyWithPathData("%QMX",field11.getText().replace("0.", "62."));
							controller.setKeyWithPathData("%QMY",field12.getText().replace("0.", "60."));
						}else{
							controller.setKeyWithPathData("%QMX",field11.getText().replace("0.", "64."));
							controller.setKeyWithPathData("%QMY",field12.getText().replace("0.", "59."));
						}
						

						
						controller.setKeyWithPathData("%QDX",field13.getText() );
						controller.setKeyWithPathData("%QY",field12.getText() );
						
						controller.setKeyWithPathData("%QDY",field14.getText() );
						
						if(controller.getBeamSelectionData().getAccelerator().contains("LHCB1")){
							controller.setKeyWithPathData("%BEAM","B1" );
						}else if(controller.getBeamSelectionData().getAccelerator().contains("LHCB2")){
							controller.setKeyWithPathData("%BEAM","B2" );		
						}
						
						if(FileIO.createDirectory(path)){
								controller.setKeyWithPathData("opticspath", path);
								opticslabel.setText("Model selected : "+new File(path).getName());
									
								clmadx.writemadx(path,env);
								setVisible(false);
								dispose();
						}else{
							MessageManager.error("Model Creator => Cannot create model ", null, null);
							MessageManager.getConsoleLogger().error("Model Creator => Cannot create model "+path+"\n Contact expert", null);
						}
				
			}else if(field00.getSelectedItem().equals("Default")){
                    MessageManager.info("Model Creator => Will copy optics ", null);
                    final File copytodir = new File(controller.getPathDataForKey("modelpath") + "/" + field0m.getSelectedItem() + "/");
                    controller.setKeyWithPathData("opticspath", copytodir.toString());
                    if (copytodir.exists()) {
                        MessageManager.warn("Model Creator => model already in dir", null, null);
                    } else if (copytodir.mkdir()) {
                        Task task = new Task() {
                            protected Object construct() {
                                boolean isCopyFileSuccessful = FileIO.copyDirectory(new File(controller.getPathDataForKey("opticsDir")
                                        + "/" + field0m.getSelectedItem()), copytodir);
                                if (!isCopyFileSuccessful)
                                    MessageManager.getConsoleLogger().error("Model Creator => CANNOT copy optics");

                                return null;
                            }

                        };
                        task.setName("Model creator => Copying dir!");
                        task.setCancellable(false);
                        task.start();
                    } else {
                        MessageManager.error("Model Creator => CANNOT create optics dir\n contact epxert", null, null);
                    }
                    MessageManager.info("Model Creator => Copying optics finished", null);
                    opticslabel.setText("Model selected : " + field0m.getSelectedItem());
                    setVisible(false);
					
			}else if(!field0m.getSelectedItem().toString().equals("New")){
				MessageManager.getConsoleLogger().info("Model Creator => Model will be activated "+new File(path).getName());
				opticslabel.setText("Model selected : "+new File(path).getName());
				controller.setKeyWithPathData("opticspath", path);
				setVisible(false);
			}else{
				MessageManager.getConsoleLogger().warn("Model Creator => Please select a model");
			}
			
			// full response
			if(jCheckBoxFullResponse21.isSelected()){
				MessageManager.info("Model Creator => Will create Fullresponse ", null);
				if(field00.getSelectedItem().toString().equals("High-beta")){
					controller.setKeyWithPathData("%MATCHER","");
				}else{
					controller.setKeyWithPathData("%MATCHER","!");
				}
				String incrementK = jTextFieldFullResponseIncrementK22.getText();
				try {
                    Double.parseDouble(incrementK);
                } catch (Exception e) {
                    MessageManager.error("Model Creator => IncrementK not d daouble value: " + incrementK, null, null);
                    MessageManager.getConsoleLogger().error("Model Creator => IncrementK not d daouble value: " + incrementK, null);
                    return;
                }
				clmadx.runFullresponse(path,env, incrementK);
			}
			
		}});
		
		//checkbox
		field15.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent itemEvent) {
			int state = itemEvent.getStateChange();
	        if (state == ItemEvent.SELECTED) {
	        	field13.setBackground(Color.white);
	        	field14.setBackground(Color.white);
	        	field13.setEditable(true);
	        	field14.setEditable(true);
	    		field13.setText(analcon.LHC().get("tunexd"));
	    		field14.setText(analcon.LHC().get("tuneyd"));		
	        }else if(state != ItemEvent.SELECTED){
	        	field13.setBackground(null);
	        	field14.setBackground(null);
	        	field13.setEditable(false);
	        	field14.setEditable(false);
	        }
			
		}});
		
		// main selector
		field00.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {			
			if(field00.getSelectedItem().equals("Default")){
				create="no";
			}
			
			findModels();
			fillsub(field00.getSelectedItem().toString());
			
		}});
		//model selector
		field0m.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
				if(field0m.getSelectedItem().equals("New") && controller.getBeamSelectionData().getAccelerator().contains("LHC") && !field00.getSelectedItem().equals("Default")){
					String response = JOptionPane.showInputDialog( "Enter a model name:" ,"none");
						if(!response.equals("none")){
							field0m.addItem(response);
							field0m.setSelectedItem(response);
							create="yes";
							buttoncreate.setText("Create new");
						}
				}else{
				    log.info("else for field0m");
					create="no";
					buttoncreate.setText("Load existing");
				}
		}});
				
	}
	

	
	/*
	 * Create GUI
	 */
	
	private JTabbedPane tabs = new JTabbedPane();
	private JButton buttoncreate = new JButton("Create new");
	private JButton buttoncancel = new JButton("Cancel");
	
	private GridLayout grid0 = new GridLayout(6,2,10,10);
	private GridLayout grid1 = new GridLayout(5,2,10,10);
	private GridLayout grid2 = new GridLayout(4,2,30,30);
	private GridLayout gridbutton = new GridLayout(1,2,10,10);	
	
	private JPanel panel0 = new JPanel();
	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel panelbutton = new JPanel();	
	
	//panel0
	private JLabel label00 = new JLabel("Optics :");
	private JLabel label0m = new JLabel("Model name :");
	private JLabel label01 = new JLabel("setting IP1 :");
	private JLabel label02 = new JLabel("setting IP2 :");
	private JLabel label03= new JLabel("setting IP5 :");
	private JLabel label04= new JLabel("setting IP8 :");
	
	private JComboBox field00 = new JComboBox();
	private JComboBox field0m = new JComboBox();
	private JComboBox field01 = new JComboBox();
	private JComboBox field02 = new JComboBox();
	private JComboBox field03= new JComboBox();
	private JComboBox field04= new JComboBox();
	
	//panel1
	private JLabel label15= new JLabel("AC-dipole :");
	private JLabel label11 = new JLabel("Free horizontal tune :");
	private JLabel label12 = new JLabel("Free vertical tune :");
	private JLabel label13= new JLabel("Driven horizontal tune :");
	private JLabel label14= new JLabel("Driven vertical tune :");	
	
	private JCheckBox field15 = new JCheckBox("",true);
	private JTextField field11 = new JTextField();
	private JTextField field12 = new JTextField();
	private JTextField field13= new JTextField();
	private JTextField field14= new JTextField();
	
	//panel2
	private JLabel label20 = new JLabel("dpp :");
	private JLabel labelFullResponseLabel21 = new JLabel("Full response:");
	//private JLabel label22 = new JLabel("Full response coupling :");
	private JLabel labelFullResponseIncrementKLabel21 = new JLabel("Full response Increment k:");
	private JLabel label23= new JLabel("Include b2 :");	
	
	private JTextField field20 = new JTextField();
	private JCheckBox jCheckBoxFullResponse21 = new JCheckBox("",false);
	private JTextField jTextFieldFullResponseIncrementK22 = new JTextField("0.00002");
	//private JCheckBox field22 = new JCheckBox("",false);
	private JCheckBox field23= new JCheckBox("",false);	
	
	private void createGUI(){
		//layout/general
		buttoncreate.setBackground(Color.GREEN);
		buttoncancel.setBackground(Color.RED);
		setLayout(new BorderLayout());
		panel0.setLayout(grid0);
		panel1.setLayout(grid1);
		panel2.setLayout(grid2);
		panelbutton.setLayout(gridbutton);	
		
		tabs.addTab("Settings", panel0);
		tabs.addTab("Tunes", panel1);
		tabs.addTab("Misc.", panel2);
		
		//panel0
		panel0.add(label00);panel0.add(field00);
		panel0.add(label0m);panel0.add(field0m);	
		panel0.add(label01);panel0.add(field01);
		panel0.add(label02);panel0.add(field02);
		panel0.add(label03);panel0.add(field03);
		panel0.add(label04);panel0.add(field04);
		
		//panel1
		panel1.add(label15);panel1.add(field15);
		panel1.add(label11);panel1.add(field11);
		panel1.add(label12);panel1.add(field12);
		panel1.add(label13);panel1.add(field13);
		panel1.add(label14);panel1.add(field14);
		
		//panel2
		panel2.add(label20);panel2.add(field20);
		labelFullResponseIncrementKLabel21.setToolTipText("Set this to a value which is used in the Python Script to increase this: incr=ones(len(variables))*incrementK");
		panel2.add(labelFullResponseLabel21);panel2.add(jCheckBoxFullResponse21);
		panel2.add(labelFullResponseIncrementKLabel21);panel2.add(jTextFieldFullResponseIncrementK22);
		//panel2.add(label22);panel2.add(field22);
		panel2.add(label23);panel2.add(field23);

		//button
		panelbutton.add(buttoncreate);panelbutton.add(buttoncancel);
		
		add(tabs, BorderLayout.CENTER);
		add(panelbutton, BorderLayout.SOUTH);		
		setAlwaysOnTop(true);
		setSize(400,400);
		toFront();
		setVisible(true);
	}
	
	/*
	 * data
	 */
	public void initiate(){
		field20.setText("0.0");
		if(controller.getBeamSelectionData().getAccelerator().equals("LHC")){
			field11.setText(controller.getPathDataForKey("%QX"));
			field12.setText(controller.getPathDataForKey("%QY"));
			field13.setText(controller.getPathDataForKey("%QDX"));
			field14.setText(controller.getPathDataForKey("%QDY"));	
		}else{
			field11.setText(controller.getPathDataForKey("%QX"));
			field12.setText(controller.getPathDataForKey("%QY"));
			field13.setText(controller.getPathDataForKey("%QDX"));
			field14.setText(controller.getPathDataForKey("%QDY"));
			field15.setSelected(false);
		}
	}
	
	
	/*
	 *  static paths for ATS and 90m and nominal
	 */
	private String hibeta_string="/afs/cern.ch/eng/lhc/optics/V6.503/HiBeta/";
	private String ats_string="/afs/cern.ch/eng/lhc/optics/ATS_V6.503/";
	//private String ats_squeeze="/OPTICS_MD2011/";
	private String ats_squeeze="/OPTICS_round_IR1_40-10_IR5_40-10/";
	private String pathoptics="/afs/cern.ch/eng/lhc/optics/V6.503/";
	private String pathip1="/IR1//new_ip1_b2_squeeze/";
	private String pathip2="/IR2/3.5TeV/";
	private String pathip5="/IR5//new_ip5_b2_squeeze/";
	private String pathip8="/IR8/3.5TeV/";
	private String[] ips;
	
	private FilenameFilter filter = new FilenameFilter() {
		public boolean accept(@SuppressWarnings("unused") File directory, String name) { //unused is OK, because there is no function without directory
	        return name.endsWith(".str");
	    }
	};
		
	private void fillsub(String caseMain){
		field01.removeAllItems();
		field02.removeAllItems();
		field03.removeAllItems();
		field04.removeAllItems();
		
		label01.setText("setting IP1 :");
		
		if(caseMain.equals("Nominal")){
			field01.setVisible(true);
			field02.setVisible(true);
			field03.setVisible(true);
			field04.setVisible(true);
			//IP1
			ips=new File(pathoptics+pathip1).list(filter);
			for(int i=0;i<ips.length;i++){
				field01.addItem(ips[i]);
			}
			field01.addItem("injection");
			
			//IP2
			ips=new File(pathoptics+pathip2).list(filter);
			for(int i=0;i<ips.length;i++){
				field02.addItem(ips[i]);
			}	
			field02.addItem("injection");
			
			//IP5
			ips=new File(pathoptics+pathip5).list(filter);
			for(int i=0;i<ips.length;i++){
				field03.addItem(ips[i]);
			}
			field03.addItem("injection");
			
			//IP8
			ips=new File(pathoptics+pathip8).list(filter);
			for(int i=0;i<ips.length;i++){
				field04.addItem(ips[i]);
			}
			field04.addItem("injection");
		}else if(caseMain.equals("ATS")){
			label01.setText("Strength file IP1 & 5:");
			field01.setVisible(true);
			field02.setVisible(false);
			field03.setVisible(false);
			field04.setVisible(false);
			ips=new File(ats_string+ats_squeeze).list();
			for(int i=0;i<ips.length;i++){
				field01.addItem(ips[i]);
			}
			field01.addItem("injection");
		}else if(caseMain.equals("High-beta")){
			field01.setVisible(true);
			field02.setVisible(false);
			field03.setVisible(true);
			field04.setVisible(false);
			
			field01.addItem("IP1_beta90.str");
			field01.addItem("injection");
			field03.addItem("IP5_beta90_2010.str");
			field03.addItem("injection");			
		}
		
	}
	
	/*
	 * load list of existing models
	 */
	private File fileoptics;
	private String[] filenames;
	private ArrayList modelList;
	
	private void findModels(){
		field01.removeAllItems();
		field02.removeAllItems();
		field03.removeAllItems();
		field04.removeAllItems();
		
		if(controller.getBeamSelectionData().getAccelerator().equals("SPS")){
			field01.setVisible(false);
			field02.setVisible(false);
			field03.setVisible(false);
			field04.setVisible(false);
			fileoptics=new File(controller.getBeamSelectionData().getProgramLocation() + "/MODEL/" + controller.getBeamSelectionData().getAccelerator() + "/defaultModels/");
			controller.setKeyWithPathData("opticsDir", fileoptics.toString());
		}else if(field00.getSelectedItem().toString().contains("Default")){
			field01.setVisible(false);
			field02.setVisible(false);
			field03.setVisible(false);
			field04.setVisible(false);
			fileoptics=new File(controller.getBeamSelectionData().getProgramLocation() + "/MODEL/LHCB/model/DefaultModels/" + controller.getBeamSelectionData().getAccelerator() + "/");
			controller.setKeyWithPathData("opticsDir", fileoptics.toString());
		}else{
			fileoptics=new File(controller.getPathDataForKey("modelpath"));
		}
		
		//getting children from optics path
		filenames=fileoptics.list();
		
		log.info("fileoptics: " + fileoptics);
		log.info("filenames: " + Arrays.toString(filenames));
		
		//checking data in model combobox
		modelList=new ArrayList();
		for(int i=0;i<field0m.getItemCount();i++){
			modelList.add(field0m.getItemAt(i));
		}
		
		for(int i=0;i<filenames.length;i++){
			if(!modelList.contains(filenames[i])){
				field0m.addItem(filenames[i]);
			}
		}
	}
}
