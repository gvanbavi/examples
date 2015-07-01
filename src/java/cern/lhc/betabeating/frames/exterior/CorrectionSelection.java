package cern.lhc.betabeating.frames.exterior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.lhc.betabeating.constants.MagnetsClassification;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.interior.CorrectionPanel;


public class CorrectionSelection extends JFrame{
    private static final long serialVersionUID = 1798030257560179765L;
    private static final Logger log = Logger.getLogger(CorrectionSelection.class);
    /*
	 * paths
	 */
    private Controller controller = null;
	private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	/*
	 * external 
	 */
	private MagnetsClassification magnet = new MagnetsClassification();
	
	/*
	 * main
	 */
	private String selfile;
	private String selfile2;
	private CorrectionPanel cpanel;
	
	public CorrectionSelection(Controller controller, String selfileT,String selfile2T,CorrectionPanel cpaneltemp){
		super("Correction selection panel");
		this.controller = controller;
		selfile=selfileT;
		selfile2=selfile2T;
		cpanel=cpaneltemp;
		createGUI();
		if(!controller.getBeamSelectionData().getAccelerator().contains("LHC")){
			buttonmag.setVisible(false);
		}


		
		for(int i=0;i<listbeta.length;i++){
			combody.addItem(listbeta[i]);
		}
		combobb.setSelectedIndex(0);
		combody.setSelectedItem("phase & Dx");
		settinglhccouple=magnet.settingsLHCcoupling();
		settinglhcbeta=magnet.settingsLHCnormalbeta();
		settinglhcphase=magnet.settingsLHCnormalphase();
		listeners();
	}
	
	/*
	 * listeners
	 */
	
	private void listeners(){
		//
		combo.setSelectedItem("SVD");
		fieldn.setBackground(null);
		fieldn.setEditable(false);
		combo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(combo.getSelectedItem().equals("MICADO")){
				fieldn.setBackground(Color.white);
				fieldn.setEditable(true);
			}else{
				fieldn.setBackground(null);
				fieldn.setEditable(false);
			}
		}});
		
		combocm.setSelectedItem("Global correction");
		combobb.setVisible(false);
		fieldns.setBackground(null);
		fieldns.setEditable(false);
		combocm.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(combocm.getSelectedItem().equals("Iterative correction")){
				combobb.setVisible(true);
				fieldns.setBackground(Color.WHITE);
				fieldns.setEditable(true);
			}else{
				combobb.setVisible(false);
				fieldns.setBackground(null);
				fieldns.setEditable(false);
			}
			listmodel.removeAllElements();
			combody.removeAllItems();
			if(combocm.getSelectedItem().equals("Coupling")){
				labelmp.setText("  Value for MODEL cut couple :");
				labelep.setText("  Value for ERROR cut couple :");
				for(int i=0;i<listdy.length;i++){
					combody.addItem(listdy[i]);
				}
				fieldns.setText(settinglhccouple[0]);
				fieldmp.setText(settinglhccouple[1]);
				fieldmd.setText(settinglhccouple[2]);	
				fieldmm.setText(settinglhccouple[3]);
				fieldep.setText(settinglhccouple[4]);
				fielded.setText(settinglhccouple[5]);
				fieldc.setText(settinglhccouple[6]);
				fieldn.setText(settinglhccouple[7]);				
			}else{
				labelmp.setText("  Value for MODEL cut phase :");
				labelep.setText("  Value for ERROR cut phase :");				
				for(int i=0;i<listbeta.length;i++){
					combody.addItem(listbeta[i]);
				}
				
				if(combody.getSelectedItem().toString().contains("phase") || combody.getSelectedItem().toString().contains("all") ){
					fieldns.setText(settinglhcphase[0]);
					fieldmp.setText(settinglhcphase[1]);
					fieldmd.setText(settinglhcphase[2]);	
					fieldmm.setText(settinglhcphase[3]);
					fieldep.setText(settinglhcphase[4]);
					fielded.setText(settinglhcphase[5]);
					fieldc.setText(settinglhcphase[6]);
					fieldn.setText(settinglhcphase[7]);
				}else{
					fieldns.setText(settinglhcbeta[0]);
					fieldmp.setText(settinglhcbeta[1]);
					fieldmd.setText(settinglhcbeta[2]);	
					fieldmm.setText(settinglhcbeta[3]);
					fieldep.setText(settinglhcbeta[4]);
					fielded.setText(settinglhcbeta[5]);
					fieldc.setText(settinglhcbeta[6]);
					fieldn.setText(settinglhcbeta[7]);					
				}
			}
		}});

		//cancel button
		buttoncancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			setVisible(false);
			dispose();
		}});
		
		//docorrections button
		buttongo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			
			String python=controller.getPathDataForKey("python");
			String programpath = null;
			
			if(combocm.getSelectedItem().toString().contains("Global")){
				programpath=controller.getPathDataForKey("corpro");
			}else if(combocm.getSelectedItem().toString().contains("Iterative")){
				programpath=controller.getPathDataForKey("itcorpro");
			}else if(combocm.getSelectedItem().toString().contains("Coupling")){
				programpath=controller.getPathDataForKey("coupcorpro");
			}
			
			String dictionary=controller.getPathDataForKey("opticspath")+"/mydictionary.py";
			
			dosystemcall(python,programpath,selfile, controller.getBeamSelectionData().getProgramLocation(),selfile2,controller.getPathDataForKey("madx"),dictionary);
			setVisible(false);
			//dispose();
		}});
		
		//options
		fieldns.setText(settinglhcphase[0]);
		fieldmp.setText(settinglhcphase[1]);
		fieldmd.setText(settinglhcphase[2]);	
		fieldmm.setText(settinglhcphase[3]);
		fieldep.setText(settinglhcphase[4]);
		fielded.setText(settinglhcphase[5]);
		fieldc.setText(settinglhcphase[6]);
		fieldn.setText(settinglhcphase[7]);
		
		combody.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(combody.toString().contains("phase") || combody.toString().contains("all") ){
				labelmp.setText("  Value for MODEL cut phase [2*pi]:");
				labelep.setText("  Value for ERROR cut phase [2*pi]:");				
				fieldns.setText(settinglhcphase[0]);
				fieldmp.setText(settinglhcphase[1]);
				fieldmd.setText(settinglhcphase[2]);	
				fieldmm.setText(settinglhcphase[3]);
				fieldep.setText(settinglhcphase[4]);
				fielded.setText(settinglhcphase[5]);
				fieldc.setText(settinglhcphase[6]);
				fieldn.setText(settinglhcphase[7]);
			}else if(combody.toString().contains("beta")){
				labelmp.setText("  Value for MODEL cut beta [m]:");
				labelep.setText("  Value for ERROR cut beta [m]:");				
				fieldns.setText(settinglhcbeta[0]);
				fieldmp.setText(settinglhcbeta[1]);
				fieldmd.setText(settinglhcbeta[2]);	
				fieldmm.setText(settinglhcbeta[3]);
				fieldep.setText(settinglhcbeta[4]);
				fielded.setText(settinglhcbeta[5]);
				fieldc.setText(settinglhcbeta[6]);
				fieldn.setText(settinglhcbeta[7]);
			}else if(combocm.toString().equals("Coupling")){
				fieldns.setText(settinglhccouple[0]);
				fieldmp.setText(settinglhccouple[1]);
				fieldmd.setText(settinglhccouple[2]);	
				fieldmm.setText(settinglhccouple[3]);
				fieldep.setText(settinglhccouple[4]);
				fielded.setText(settinglhccouple[5]);
				fieldc.setText(settinglhccouple[6]);
				fieldn.setText(settinglhccouple[7]);				
			}
			
		}});
		
		buttonmag.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			showMagnetSelection(combocm.getSelectedItem().toString());
		}});
		
		buttonmagnet.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
		    Object[] selectedValues = magnetlist.getSelectedValues();
			StringBuilder stringBuilder= new StringBuilder();
			for(int i=0;i<selectedValues.length;i++){
				if(i!=0 )
				    stringBuilder.append(",");					
				stringBuilder.append(selectedValues[i].toString());
			}
			framemagnet.setVisible(false);
			framemagnet.dispose();
			if(selectedValues.length>0){
				magnetsel=stringBuilder.toString();
			}
		}});
		
	   // one two beam checker
		combobb.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(selfile2.equals("None")){
				MessageManager.warn("Optics Panel => two beam correction not allowed!",null,null);
				MessageManager.getConsoleLogger().warn("Optics Panel => select two files in the optics panel");
				combobb.setSelectedIndex(0);
			}
		}});
		
	}
	
	/*
	 * data handling
	 */
	private String[] settinglhccouple;
	private String[] settinglhcbeta;
	private String[] settinglhcphase;
	
	private void savesettings(String cm){
		if(cm.equals("phase")){
			settinglhcphase=new String[8];
			settinglhcphase[0]=fieldns.getText();
			settinglhcphase[1]=fieldmp.getText();
			settinglhcphase[2]=fieldmd.getText();
			settinglhcphase[3]=fieldmm.getText();
			settinglhcphase[4]=fieldep.getText();
			settinglhcphase[5]=fielded.getText();
			settinglhcphase[6]=fieldc.getText();
			settinglhcphase[7]=fieldn.getText();
		}else if(cm.equals("beta")){
			settinglhcbeta=new String[8];
			settinglhcbeta[0]=fieldns.getText();
			settinglhcbeta[1]=fieldmp.getText();
			settinglhcbeta[2]=fieldmd.getText();
			settinglhcbeta[3]=fieldmm.getText();
			settinglhcbeta[4]=fieldep.getText();
			settinglhcbeta[5]=fielded.getText();
			settinglhcbeta[6]=fieldc.getText();
			settinglhcbeta[7]=fieldn.getText();
		}else{
			settinglhccouple=new String[8];
			settinglhccouple[0]=fieldns.getText();
			settinglhccouple[1]=fieldmp.getText();
			settinglhccouple[2]=fieldmd.getText();
			settinglhccouple[3]=fieldmm.getText();
			settinglhccouple[4]=fieldep.getText();
			settinglhccouple[5]=fielded.getText();
			settinglhccouple[6]=fieldc.getText();
			settinglhccouple[7]=fieldn.getText();			
		}
	}
	
	/*
	 * GUI
	 */
	private GridLayout grid=new GridLayout(14,2,30,20);
	
	private JLabel labelcm = new JLabel("  Correction method :");
	private JLabel labelmp = new JLabel("  Value for MODEL cut phase [2*pi]:");
	private JLabel labelmd = new JLabel("  Value for MODEL cut disp [m]:");	
	private JLabel labelmm = new JLabel("  Value for min strength :");
	private JLabel labelep = new JLabel("  Value for ERROR cut phase [2*pi]:");
	private JLabel labeled = new JLabel("  Value for ERROR cut disp [m]:");	
	private JLabel labelc = new JLabel("  Value for SVD cut :");
	private JLabel labeloptions = new JLabel("  Select option for correcting :");
	private JLabel labelnc = new JLabel("  Number of correctors");
	private JLabel labeldy = new JLabel("  Correction weights");
	private JLabel labelns = new JLabel("  Number of steps");
	private JLabel labelma = new JLabel("  Select magnet group:");
	private JLabel labelbb = new JLabel("  One/two beam");
	
	private JTextField fieldmp=new JTextField() ;
	private JTextField fieldmd=new JTextField() ;	
	private JTextField fieldmm=new JTextField();
	private JTextField fieldep=new JTextField() ;
	private JTextField fielded=new JTextField() ;
	private JTextField fieldc=new JTextField() ;
	private JTextField fieldn=new JTextField();
	private JTextField fieldns=new JTextField() ;
	private String[] listdy = {"difference & Dy","difference","sum","sum & Dy","difference & sum","Dy","all"};
	private String[] listbeta = {"phase","beta","Dx","phase & Dx", "phase & beta", "beta & Dx", "all"};
	
	private JComboBox combocm = new JComboBox(new String[]{"Coupling","Iterative correction","Global correction"});
	private JComboBox combo = new JComboBox(new String[]{"MICADO","Best corrector", "SVD"});
	private JComboBox combody= new JComboBox();
	private JComboBox combobb = new JComboBox(new String[]{"One beam","Two beam"});
	private JButton buttonmag=new JButton("Magnet selection");

	
	private JButton buttongo = new JButton("Do corrections");
	private JButton buttoncancel = new JButton("Cancel");

	
	
	private void createGUI(){
		buttongo.setBackground(Color.GREEN);
		buttoncancel.setBackground(Color.RED);
		
		this.setLayout(grid);
		
		this.add(labelcm);
		this.add(combocm);
		
		this.add(labeloptions);
		this.add(combo);		
		
		this.add(labelbb);
		this.add(combobb);		
		
		this.add(labelma);
		this.add(buttonmag);	
		
		this.add(labeldy);
		this.add(combody);	
		
		this.add(labelns);
		this.add(fieldns);		
		
		this.add(labelmp);
		this.add(fieldmp);	
		
		this.add(labelmd);
		this.add(fieldmd);			
		
		this.add(labelmm);
		this.add(fieldmm);
		
		this.add(labelep);
		this.add(fieldep);
		
		this.add(labeled);
		this.add(fielded);		
		
		this.add(labelc);
		this.add(fieldc);
		
		this.add(labelnc);
		this.add(fieldn);
		
		this.add(buttongo);
		this.add(buttoncancel);
		
		this.setSize(460,600);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void showGUI(){
		setVisible(true);
		fieldns.setText(settinglhcphase[0]);
		fieldmp.setText(settinglhcphase[1]);
		fieldmd.setText(settinglhcphase[2]);	
		fieldmm.setText(settinglhcphase[3]);
		fieldep.setText(settinglhcphase[4]);
		fielded.setText(settinglhcphase[5]);
		fieldc.setText(settinglhcphase[6]);
		fieldn.setText(settinglhcphase[7]);
	}
	
	/*
	 * system call
	 */
	private String command;
	private String weigths;
	private String accel2;
	private String errorcut;
	private String modelcut;
	private int sel;
	private String numberofcor;
	private String svdcut;
	private String mimstren;
	private String magnetsel="None";
	private String optics;
	
	private void dosystemcall(final String python,String programpath,final String path2files,final String bbpath,String path2files2,final String mad,String dictionary){
		optics=controller.getPathDataForKey("opticspath");
		
		if(combocm.getSelectedItem().equals("Coupling")){
			savesettings("couple");
			
			if(magnetsel.equals("None")){
				magnetsel="Qs";
			}
			
			if(combody.getSelectedItem().toString().contains("difference") || combody.getSelectedItem().toString().contains("all")){
				weigths="1,1";
			}else{weigths="0,0";}
			
			if(combody.getSelectedItem().toString().contains("sum") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1,1";
			}else{weigths=weigths+",0,0";}	
			
			if(combody.getSelectedItem().toString().contains("Dy") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1";
			}else{weigths=weigths+",0";}
			
			
			command=python+" "+programpath+" -a "+ controller.getBeamSelectionData().getAccelerator() +" -p "+path2files+" -c "+settinglhccouple[6]+" -e "+
			settinglhccouple[4]+","+settinglhccouple[5]+" -m "+settinglhccouple[1]+","+settinglhccouple[2]+" -r "+bbpath+" -s "+settinglhccouple[3]
			+" -d "+weigths+" -o "+optics+" -v "+magnetsel;
			
			
			
			MessageManager.getConsoleLogger().info("Correction panel => Sending command "+command);
			
			// running corrections
			Task task = new Task() {
				protected Object construct() {
				try{
			
				    Systemcall.execute(command,"Running for corrections",env,path2files,true);
					cpanel.addFile(path2files, "COUPLING");
					if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
						MessageManager.getConsoleLogger().info("Correction panel => Will create corrections plots checkout \n"+path2files);
							madxrun(path2files,bbpath+"/MODEL/LHCB/model/Corrections/",controller.getBeamSelectionData().getAccelerator(),mad,optics,"changeparameters_couple.madx");							
							command="chmod 777 "+path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=python+" "+bbpath+"/MODEL/LHCB/model/Corrections/getdiff.py "+path2files;
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command="gnuplot "+path2files+"/gplot";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);

					}
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("Correction panel => Caused by exception: ",ex,null);
					MessageManager.info("Correction panel => Error",null);
				}
				
				return null;
				}
				
				};
				
				task.setName("Correction panel for coupling");
				task.setCancellable(false);
				task.start();
					
		}else if(combocm.getSelectedItem().equals("Global correction")){
			savesettings("phase");	
			savesettings("beta");
			
			if(magnetsel.equals("None")){
				magnetsel="Q";
			}
			
			//Weights
			if(combody.getSelectedItem().toString().contains("phase") || combody.getSelectedItem().toString().contains("all")){
				weigths="1,1";
				errorcut=settinglhcphase[4]+","+settinglhcphase[5];
				modelcut=settinglhcphase[1]+","+settinglhcphase[2];
				numberofcor=settinglhcphase[7];
				svdcut=settinglhcphase[6];
				mimstren=settinglhcphase[3];
			}else{weigths="0,0";}
			
			if(combody.getSelectedItem().toString().contains("beta") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1,1";
				errorcut=settinglhcbeta[4]+","+settinglhcbeta[5];
				modelcut=settinglhcbeta[1]+","+settinglhcbeta[2];
				numberofcor=settinglhcbeta[7];
				svdcut=settinglhcbeta[6];
				mimstren=settinglhcbeta[3];
			}else{weigths=weigths+",0,0";}	
			
			if(combody.getSelectedItem().toString().contains("Dx") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1";
				errorcut=settinglhcphase[4]+","+settinglhcphase[5];
				modelcut=settinglhcphase[1]+","+settinglhcphase[2];
				numberofcor=settinglhcphase[7];
				svdcut=settinglhcphase[6];
				mimstren=settinglhcbeta[3];
			}else{weigths=weigths+",0";}
			
			weigths=weigths+",10";
			
			if(controller.getBeamSelectionData().getAccelerator().contains("LHCB")){
				command=python+" "+programpath+" -a "+ controller.getBeamSelectionData().getAccelerator() +" -t "+combo.getSelectedItem()+" -n "+numberofcor+" -p "+path2files
				+" -c "+svdcut+" -e "+errorcut+" -m "+modelcut+" -r "+bbpath+" -s "+mimstren+" -j "+sel
				+" -o "+optics+" -v "+magnetsel+" -w "+weigths;

			}else{
				
				command=python+" "+programpath+" -a "+controller.getBeamSelectionData().getAccelerator()+" -t "+combo.getSelectedItem()+" -n "+numberofcor+" -p "+path2files
				+" -c "+svdcut+" -e "+errorcut+" -m "+modelcut+" -r "+bbpath+" -s "+mimstren+" -j "+sel
				+" -o "+optics+" -v "+magnetsel+" -w "+weigths+" -d "+dictionary;
			}
			
			
			MessageManager.getConsoleLogger().info("Correction panel => Sending command "+command);
			// running corrections
			Task task = new Task() {
				protected Object construct() {
				try{
			
				    Systemcall.execute(command,"Running for corrections",env,path2files,true);
					cpanel.addFile(path2files, "BETA");
					if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
						MessageManager.getConsoleLogger().info("Correction panel => Will create corrections plots checkout \n"+path2files);
							madxrun(path2files,bbpath+"/MODEL/LHCB/model/Corrections/",controller.getBeamSelectionData().getAccelerator(),mad,optics,"changeparameters.madx");
							command="chmod 777 "+path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=python+" "+bbpath+"/MODEL/LHCB/model/Corrections/getdiff.py "+path2files;
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command="gnuplot "+path2files+"/gplot";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
					}
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("Correction panel => Caused by exception: ",ex,null);
					MessageManager.info("Correction panel => Error",null);
				}
				
				return null;
				}
				
				};
				
				task.setName("Correction panel for Global correction");
				task.setCancellable(false);
				task.start();
			
			
		}else if(combocm.getSelectedItem().equals("Iterative correction")){
			savesettings("phase");
			savesettings("beta");
			
			if(magnetsel.equals("None")){
				magnetsel="Q";
			}
			
			//Weights
			if(combody.getSelectedItem().toString().contains("phase") || combody.getSelectedItem().toString().contains("all")){
				weigths="1,1";
				errorcut=settinglhcphase[4]+","+settinglhcphase[5];
				modelcut=settinglhcphase[1]+","+settinglhcphase[2];
				numberofcor=settinglhcphase[7];
				svdcut=settinglhcphase[6];
				mimstren=settinglhccouple[3];
			}else{weigths="0,0";}
			
			if(combody.getSelectedItem().toString().contains("beta") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1,1";
				errorcut=settinglhcbeta[4]+","+settinglhcbeta[5];
				modelcut=settinglhcbeta[1]+","+settinglhcbeta[2];
				numberofcor=settinglhcbeta[7];
				svdcut=settinglhcbeta[6];
				mimstren=settinglhcbeta[3];
			}else{weigths=weigths+",0,0";}	
			
			if(combody.getSelectedItem().toString().contains("Dx") || combody.getSelectedItem().toString().contains("all")){
				weigths=weigths+",1";
				errorcut=settinglhcphase[4]+","+settinglhcphase[5];
				modelcut=settinglhcphase[1]+","+settinglhcphase[2];
				numberofcor=settinglhcphase[7];
				svdcut=settinglhcphase[6];
				mimstren=settinglhcbeta[3];
			}else{weigths=weigths+",0";}
			
			weigths=weigths+",10";
			
			//one beam two beam
			sel=combobb.getSelectedIndex();
			
			
			if(controller.getBeamSelectionData().getAccelerator().equals("LHCB1")){
				accel2="LHCB2";
				
				command=python+" "+programpath+" -a "+controller.getBeamSelectionData().getAccelerator()+" -b "+accel2+" -t "+combo.getSelectedItem()+" -n "+numberofcor+" -p "+path2files
				+" -q "+path2files2+" -c "+svdcut+" -e "+errorcut+" -m "+modelcut+" -r "+bbpath+" -s "+mimstren+" -j "+sel
				+" -o "+optics+" -x "+python+" -v "+magnetsel+" -z "+mad+" -w "+weigths+" -i "+settinglhcphase[0];
				
			}else if(controller.getBeamSelectionData().getAccelerator().equals("LHCB2")){
				accel2="LHCB1";
				
				command=python+" "+programpath+" -a "+controller.getBeamSelectionData().getAccelerator()+" -b "+accel2+" -t "+combo.getSelectedItem()+" -n "+numberofcor+" -p "+path2files
				+" -q "+path2files2+" -c "+svdcut+" -e "+errorcut+" -m "+modelcut+" -r "+bbpath+" -s "+mimstren+" -j "+sel
				+" -o "+optics+" -x "+python+" -v "+magnetsel+" -z "+mad+" -w "+weigths+" -i "+settinglhcphase[0];
			}else{
				accel2="SPS";
				
				command=python+" "+programpath+" -a "+controller.getBeamSelectionData().getAccelerator()+" -b "+accel2+" -t "+combo.getSelectedItem()+" -n "+numberofcor+" -p "+path2files
				+" -q "+path2files2+" -c "+svdcut+" -e "+errorcut+" -m "+modelcut+" -r "+bbpath+" -s "+mimstren+" -j "+sel
				+" -o "+optics+" -x "+python+" -v "+magnetsel+" -z "+mad+" -w "+weigths+" -i "+settinglhcphase[0]+" -d "+dictionary;
			}
			MessageManager.getConsoleLogger().info("Correction panel => Sending command "+command);
			// running corrections
			Task task = new Task() {
				protected Object construct() {
				try{
				    Systemcall.execute(command,"Running for corrections",env,path2files,true);
					cpanel.addFile(path2files, "BETA");
					
					if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
						MessageManager.getConsoleLogger().info("Correction panel => Will create corrections plots checkout \n"+path2files);
							madxrun(path2files,bbpath+"/MODEL/LHCB/model/Corrections/",controller.getBeamSelectionData().getAccelerator(),mad,optics,"changeparameters.madx");
							command="chmod 777 "+path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=path2files+"/mad_command";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command=python+" "+bbpath+"/MODEL/LHCB/model/Corrections/getdiff.py "+path2files;
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
							command="gnuplot "+path2files+"/gplot";
							Systemcall.execute(command,"Running for corrections",env,path2files,true);
					}
					
					
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("Correction panel => Caused by exception: ",ex,null);
					MessageManager.info("Correction panel => Error",null);
				}
				
				return null;
				}
				
				};
				
				task.setName("Correction panel for Iterative");
				task.setCancellable(false);
				task.start();
			
			
		}
		magnetsel="None";
		log.info("Command "+command);
	}
	
	/*
	 * madx for correction
	 */
	
	private void madxrun(String path,String fulldirpath,String accel,String madx,String opt,String cor){
	    BufferedWriter out2;
	    BufferedReader in2;
		// main madx
		try {
			out2 = new BufferedWriter(new FileWriter(path+"/job.cor.madx"));
		    in2 = new BufferedReader(new FileReader(fulldirpath+"/job.twiss_java.madx"));
		    String str;
		    while ((str = in2.readLine()) != null) {
				str=str.replace("%PATH", path);
				str=str.replace("%ACCEL",accel);
				str=str.replace("%OPT",opt);
				str=str.replace("%COR",cor);
		    	out2.write(str+"\n");
		    }
		    in2.close();
		    out2.close();
		    
		    out2 = new BufferedWriter(new FileWriter(path+"/mad_command"));
		    out2.write(madx+" < "+path+"/job.cor.madx"+"\n");
		    out2.close();
		    
			out2 = new BufferedWriter(new FileWriter(path+"/gplot"));
		    in2 = new BufferedReader(new FileReader(fulldirpath+"/gplot"));
		    while ((str = in2.readLine()) != null) {
				str=str.replace("%PATH", path);
		    	out2.write(str+"\n");
		    }
		    in2.close();
		    out2.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Magnet selection
	 */
	private JFrame framemagnet = new JFrame();
	private DefaultListModel listmodel = new DefaultListModel();
	private JList magnetlist = new JList(listmodel);
	private JScrollPane scrollmagnet = new JScrollPane(magnetlist);
	private JButton buttonmagnet = new JButton("Select magnet group");
	private void showMagnetSelection(String sele){
		listmodel.removeAllElements();
		log.info(sele);
		if(sele.equals("Coupling")){
			for(int i=0;i<magnet.skewquadsLHC().length;i++){
				listmodel.addElement(magnet.skewquadsLHC()[i]);
			}
		}else if(sele.equals("Global correction") || sele.equals("Iterative correction")){
			for(int i=0;i<magnet.normalquadsLHC().length;i++){
				listmodel.addElement(magnet.normalquadsLHC()[i]);
			}			
		}
		
		framemagnet.setLayout(new BorderLayout());
		framemagnet.add(scrollmagnet, BorderLayout.CENTER);
		framemagnet.add(buttonmagnet, BorderLayout.SOUTH);		
		framemagnet.setVisible(true);
		framemagnet.setSize(new Dimension(200,300));
		
	}
}