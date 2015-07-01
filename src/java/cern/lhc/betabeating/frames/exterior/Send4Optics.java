package cern.lhc.betabeating.frames.exterior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.lhc.betabeating.datahandler.ReWriteFile;
import cern.lhc.betabeating.datahandler.ReWriteFile.ReWriteFileMethod;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.interior.BPMpanel;
import cern.lhc.betabeating.frames.interior.OpticsPanel;
import cern.lhc.betabeating.modelcreator.ModelCreator;

public class Send4Optics extends JFrame {
    private static final long serialVersionUID = 8798201466791661847L;
    private static final Logger log = Logger.getLogger(Send4Optics.class);
    
    private Controller controller = null;
    private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	private BPMpanel bpmpanel;
	private OpticsPanel opapanel;
	
	public Send4Optics(Controller controller, BPMpanel paneltemp, OpticsPanel oppaneltemp){
		super("Select for optics");
		bpmpanel=paneltemp;
		opapanel=oppaneltemp;
		this.controller = controller;
		
		CreateGUI();
		setVisible(false);
		listeners();
	}
	
	public void showpanel(){
		setVisible(true);
	}
	
	/*
	 * setting map;
	 */
	private String data4add;
	private String data4add2;
	private Map<String,String> allPath = new TreeMap<String, String>(); 
	private Map<String,String> bpmpanelmap = new TreeMap<String, String>();
	public void setmap(Map<String,String> maptemp){
		
		for(int i=table.getRowCount()-1;i>=0;i--){
			dmodel.removeRow(i);
		}
		
		allPath=maptemp;
		
	
		for(int i=0;i<bpmpanel.tablenorth.getRowCount();i++){
			data4add=bpmpanel.tablenorth.getValueAt(i, 0).toString();
			data4add2=bpmpanel.tablenorth.getValueAt(i, 1).toString();
			log.info(data4add+" "+data4add2);
			bpmpanelmap.put(data4add,data4add2);
		}
			
		
		 Iterator<Entry<String, String>> it = allPath.entrySet().iterator();
		log.info(allPath.size());
		while(it.hasNext()){
			Entry<String, String> pairs = it.next();
			data4add=pairs.getKey().toString();
			log.info(data4add);
			//SUSSIX
			if(data4add.contains("SUSSIX")){
				if(bpmpanelmap.containsKey(data4add.replace("_SUSSIX", ""))){
					dmodel.addRow(new Object[]{data4add,"No",bpmpanelmap.get(data4add.replace("_SUSSIX", ""))});
				}else{
					dmodel.addRow(new Object[]{data4add,"No","0.0"});									
				}
			}else if(data4add.contains("SVD")){
				//SVD
				if(bpmpanelmap.containsKey(data4add.replace("_SVD", ""))){
					dmodel.addRow(new Object[]{data4add,"No",bpmpanelmap.get(data4add.replace("_SVD", ""))});
				}else{
					dmodel.addRow(new Object[]{data4add,"No","0.0"});
				}
			}
		}
		
	}
	
	/*
	 * listeners
	 */
	private String option;
	private ArrayList<String> selfilessussix;
	private ArrayList<String> selfilessvd;
	
	private void listeners(){
		table.addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent evt){
			if (evt.getButton() == MouseEvent.BUTTON3){
					 JFrame frame = new JFrame();
					 int answer = JOptionPane.showConfirmDialog(frame, "Include the selected row ?");
					
					if(answer==JOptionPane.YES_OPTION){
						option="Yes";
						for(int i=0;i<table.getSelectedRows().length;i++){
							table.setValueAt(option,table.getSelectedRows()[i] , 1);
						}
					}else if(answer==JOptionPane.NO_OPTION){
						option="No";
						for(int i=0;i<table.getSelectedRows().length;i++){
							table.setValueAt(option,table.getSelectedRows()[i] , 1);
						}
					}else{
						
					}
			}
		}});
		
		//when go is pressed
		buttongo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			new ModelCreator(controller); //TODO delete? was: not_used = <line> (tbach), do not delete, change!
			selfilessussix=new ArrayList<String>();
			selfilessvd=new ArrayList<String>();

			for(int i=0;i<table.getRowCount();i++){
				
				if(table.getValueAt(i, 1).equals("Yes") && table.getValueAt(i, 0).toString().contains("SUSSIX")){
					selfilessussix.add(allPath.get(table.getValueAt(i, 0).toString()).replace("_SUSSIX", ""));
					String filePath = allPath.get(table.getValueAt(i, 0).toString()).replace("_SUSSIX", "");
					String DPP = table.getValueAt(i, 2).toString();
					ReWriteFileMethod reWriteFileMethod = ReWriteFileMethod.SUSSIX;
					ReWriteFile.executeForFilePathDPPMethod(filePath, DPP, reWriteFileMethod);
										
				}else if(table.getValueAt(i, 1).equals("Yes") && table.getValueAt(i, 0).toString().contains("SVD")){
					selfilessvd.add(allPath.get(table.getValueAt(i, 0).toString()).replace("_SVD", ""));
					String filePath = allPath.get(table.getValueAt(i, 0).toString()).replace("_SVD", "");
                    String DPP = table.getValueAt(i, 2).toString();
                    ReWriteFileMethod reWriteFileMethod = ReWriteFileMethod.SVD;
                    ReWriteFile.executeForFilePathDPPMethod(filePath, DPP, reWriteFileMethod);
				}else if(table.getValueAt(i, 1).equals("Yes")){
					MessageManager.getConsoleLogger().warn(" OpticsPanel =>"+table.getValueAt(i, 1).toString()+" files doesn't contain any method");
				}
								
			}
			
			
			task = new Task() {
				protected Object construct() {
				try{
								if(selfilessussix.size()>0){
										syscall("SUSSIX",selfilessussix);
										MessageManager.getConsoleLogger().info(" OpticsPanel => Running optics for SUSSIX  for "+selfilessussix.size()+" files");
								}
									
								if(selfilessvd.size()>0){
										syscall("SVD",selfilessvd);
										MessageManager.getConsoleLogger().info(" OpticsPanel => Running optics for SVD  for "+selfilessvd.size()+" files");
								}	
			
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("OpticsPanel => Caused by exception: ",ex,null);
					MessageManager.info("OpticsPanel => Error",null);
				}
						  return null;
				}
					
				};
						task.setName("OpticsPanel");
						task.setCancellable(false);
						task.start();
						setVisible(false);	
				
		}});
	}
	
	/*
	 * syscall
	 */
	

	private String command;
	private String output;
	private String dictionary;
	private Task task;
	private String optics;
	private String unit;
	
	private void syscall(final String method, final ArrayList<String> list){

					unit=controller.getPathDataForKey("labelug");
					optics=controller.getPathDataForKey("opticspath")+"/twiss.dat";
				
					output = controller.getBeamSelectionData().getOutputPath() + "/" + controller.getBeamSelectionData().getDate() + "/" + controller.getBeamSelectionData().getAccelerator() + "/Results/"+createtime();
					
					
					if(new File(output).mkdir()){
						
						
						if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
							command=controller.getPathDataForKey("python")+" "+controller.getPathDataForKey("getllmpro")+" -a " + controller.getBeamSelectionData().getAccelerator() + " -m "+optics
							+" -f "+append(list)+" -o "+output+" -t "+method+" -b "+unit;
						}else if(controller.getBeamSelectionData().getAccelerator().contains("SOLEIL")){
						    /*dictionary=controller.getPathDataForKey("opticspath")+"/mydictionary.py";*/
                            command=controller.getPathDataForKey("python")+" "+controller.getPathDataForKey("getllmpro")+" -a " + controller.getBeamSelectionData().getAccelerator() + " -m "+optics
                            +" -f "+append(list)+" -o "+output+" -t "+method+" -b "+unit;						    
						}else{
							dictionary=controller.getPathDataForKey("opticspath")+"/mydictionary.py";
							command=controller.getPathDataForKey("python")+" "+controller.getPathDataForKey("getllmpro")+" -a " + controller.getBeamSelectionData().getAccelerator() + " -m "+optics
							+" -f "+append(list)+" -o "+output+" -t "+method+" -b "+unit+" -d "+dictionary;
						}
						        Systemcall.execute(command,"Running for optics",env,output,true);
								opapanel.addfile(output, method);
										
					}else{
						MessageManager.error("OpticsPanel => Cannot create dir",null,null);
						MessageManager.getConsoleLogger().error("OpticsPanel => Cannot create dir "+output,null);
					}

	}
	
	/*
	 * append
	 */
	private StringBuffer sbuf;
	private String append(ArrayList list){
		sbuf= new StringBuffer();
		for(int i=0;i<list.size();i++){
			if(i==0 ){
				sbuf.append(list.get(i).toString());
			}else{
				sbuf.append(","+list.get(i).toString());					
			}
		}
		
		return sbuf.toString();
	}
	
	/*
	 * time
	 */
	private String createtime(){
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.HOUR)+"-"+calendar.get(Calendar.MINUTE)+"-"+calendar.get(Calendar.SECOND)+"/";
	}
	
	
	/*
	 * Create GUI
	 */
	private DefaultTableModel dmodel = new DefaultTableModel();
	private JTable table = new JTable(dmodel);
	private JScrollPane scroll = new JScrollPane(table);
	
	private JButton buttongo = new JButton("Go");
	
	private void CreateGUI(){
		buttongo.setBackground(Color.green);
		dmodel.addColumn("File :");	
		dmodel.addColumn("Yes/No");	
		dmodel.addColumn("Dpp");
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(75);
		table.getColumnModel().getColumn(2).setPreferredWidth(75);
		add(buttongo, BorderLayout.SOUTH);
		//setVisible(true);
		setSize(new Dimension(550,250));
		
	}
	
	/*public static void main (String args[]){
		new Send4Optics();
	}*/

}
