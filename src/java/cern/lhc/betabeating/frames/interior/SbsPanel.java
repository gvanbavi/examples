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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.Style;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.data.DefaultErrorDataSet;
import cern.jdve.renderer.ErrorDataSetRenderer;
import cern.lhc.betabeating.constants.Segment;
import cern.lhc.betabeating.datahandler.TFSReader;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.frames.Controller;

public class SbsPanel extends JPanel {
    private static final long serialVersionUID = -4010563484821371858L;
    private static final Logger log = Logger.getLogger(SbsPanel.class);
    
    private Controller controller = null;
    private Segment seg=new Segment();
	//setting filename
	private String Filename="empty";
	public void setFilename(String temp){
		Filename=temp;
		readingtwiss();
		reinit();
		SegmentTable(controller.getBeamSelectionData().getAccelerator());
	}
	/*
	 * Main
	 */
	public SbsPanel(Controller controller){
	    this.controller = controller;
		createGUI();
		fillsettings();
		listeners();
		SegmentTable("LHCB1");
		//SegmentTable(controller.getPathDataForKey("accel"));
	}
	
	/*
	 * listenrs
	 */
	private String option;

	private void listeners(){
		buttonadd.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(listleft.getSelectedValue().equals("IP")){
				 Iterator<Entry<String, String>> it = ip.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
			      String key = pairs.getKey();
			      String value = pairs.getValue() ;
			      if(value.equals("Yes")){
			    	  listmodelright.addElement(key);
			      }
			    }
                
			}else if(listleft.getSelectedValue().equals("Collimators")){
				 Iterator<Entry<String, String>> it = colli.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
			      String key = pairs.getKey();
			      String value = pairs.getValue() ;
			      if(value.equals("Yes")){
			    	  listmodelright.addElement(key);
			      }
			    
			    }				
			}else if(listleft.getSelectedValue().equals("Transverse Dampers")){
				 Iterator<Entry<String, String>> it = adt.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
			      String key = pairs.getKey();
			      String value = pairs.getValue() ;
			      if(value.equals("Yes")){
			    	  listmodelright.addElement(key);
			      }
			    }
			}
		}});
		buttonremove.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(listleft.getSelectedValue().equals("IP")){
				 Iterator<Entry<String, String>> it = ip.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
				   String key =pairs.getKey();
			      String value = pairs.getValue();
			      if(value.equals("Yes")){
			    	  listmodelright.removeElement(key);
			      }
			    }
			}else if(listleft.getSelectedValue().equals("Collimators")){
				 Iterator<Entry<String, String>> it = colli.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
				   String key =pairs.getKey();
			      String value = pairs.getValue();
			      if(value.equals("Yes")){
			    	  listmodelright.removeElement(key);
			      }
			    }		
			}else if(listleft.getSelectedValue().equals("Transverse Dampers")){
				 Iterator<Entry<String, String>> it = adt.entrySet().iterator();
				  while (it.hasNext()) {
				        Entry<String, String> pairs = it.next();
				   String key =pairs.getKey();
			      String value = pairs.getValue();
			      if(value.equals("Yes")){
			    	  listmodelright.removeElement(key);
			      }
			    }	
			}
		}});
		
		buttonrun.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			systemcallforelement();
		}});
		
		buttonrunSbs.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(table.getSelectedRow()!=-1){
				selname=table.getValueAt(table.getSelectedRow(),0).toString();
				systemcallforsegment(table.getValueAt(table.getSelectedRow(),1).toString() , table.getValueAt(table.getSelectedRow(),2).toString(),selname,Filename);
			}else{
				MessageManager.getConsoleLogger().info(" Sbspanel => Please select a segment");
			}
		}});
		
		buttonclear.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(table.getSelectedRow()!=-1){
				tablemodel.removeRow(table.getSelectedRow());
			}
		}});
		
		buttonsetting.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			settings();
		}});
		
		////listener
		tableset.addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent evt){
			if(evt.getButton() == MouseEvent.BUTTON3){
					 JFrame frame = new JFrame();
					 int answer = JOptionPane.showConfirmDialog(frame, "Include element ?");
					
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
		
		///button go
		buttongo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			for(int i=0;i<tableset.getRowCount();i++){
				if(listleft.getSelectedValue().equals("IP")){
					ip.put(tableset.getValueAt(i, 0).toString(), tableset.getValueAt(i, 1).toString());
				}else if(listleft.getSelectedValue().equals("Collimators")){
					colli.put(tableset.getValueAt(i, 0).toString(), tableset.getValueAt(i, 1).toString());
				}else if(listleft.getSelectedValue().equals("Transverse Dampers")){
					adt.put(tableset.getValueAt(i, 0).toString(), tableset.getValueAt(i, 1).toString());
				}
			}
			frameset.setVisible(false);
			frameset.dispose();
		}});
		
		///load
		table.addMouseListener(new MouseAdapter() {public void mouseClicked(MouseEvent evt){
			if(evt.getButton() == MouseEvent.BUTTON1){
		    	selname=table.getValueAt(table.getSelectedRow(),0).toString();
		    	plot(Filename,selname);
		        //setfile(translate.get(listsetting.getSelectedValue()),"None");
			}
		}});
	}
	
	/*
	 * settings
	 */
	private JFrame frameset;
	private DefaultTableModel dmodel;
	private JTable tableset = new JTable();
	private JScrollPane scroll;
	private JButton buttongo = new JButton("Go");
	private void settings(){
		frameset=new JFrame();
		dmodel = new DefaultTableModel();
		
		log.info(ip.size()+" "+listleft.getSelectedValue());
		//filling data
		if(listleft.getSelectedValue().equals("IP")){
			 Iterator<Entry<String, String>> it = ip.entrySet().iterator();
			  while (it.hasNext()) {
			        Entry<String, String> pairs = it.next();
			   String key =pairs.getKey();
		      String value = pairs.getValue();
		      if(value.equals("Yes")){
		    	  dmodel.addRow(new Object[]{key,"Yes"});
		      }else{
		    	  dmodel.addRow(new Object[]{key,"No"});
		      }
		    }
		}else if(listleft.getSelectedValue().equals("Collimators")){
			 Iterator<Entry<String, String>> it = colli.entrySet().iterator();
			  while (it.hasNext()) {
			        Entry<String, String> pairs = it.next();
			   String key =pairs.getKey();
		      String value = pairs.getValue();
		      if(value.equals("Yes")){
		    	  dmodel.addRow(new Object[]{key,"Yes"});
		      }else{
		    	  dmodel.addRow(new Object[]{key,"No"});
		      }
		    }
		}else if(listleft.getSelectedValue().equals("Transverse Dampers")){
			 Iterator<Entry<String, String>> it = adt.entrySet().iterator();
			  while (it.hasNext()) {
			        Entry<String, String> pairs = it.next();
			   String key = pairs.getKey();
		      String value = pairs.getValue();
		      if(value.equals("Yes")){
		    	  dmodel.addRow(new Object[]{key,"Yes"});
		      }else{
		    	  dmodel.addRow(new Object[]{key,"No"});
		      }
		    }
		}
		
		//gui
		buttongo.setBackground(Color.green);
		dmodel.addColumn("Element :");	
		dmodel.addColumn("Yes/No");	
		tableset.setModel(dmodel);
		scroll = new JScrollPane(tableset);
		frameset.setLayout(new BorderLayout());
		frameset.add(scroll, BorderLayout.CENTER);
		tableset.getColumnModel().getColumn(0).setPreferredWidth(125);
		tableset.getColumnModel().getColumn(1).setPreferredWidth(75);
		frameset.add(buttongo, BorderLayout.SOUTH);
		frameset.setVisible(true);
		frameset.setSize(new Dimension(300,450));
	}
	
	/*
	 * filehandling
	 */
	private String selname;
	//private HashMap<String,String> translate = new HashMap();
	/*private void setfile(String file,String name){
		
		/*if(!name.equals("None")){
			if(!premap.containsKey(new File(file).getName()+"_"+name)){
				tablemodel.addRow(new Object[]{name});
			}
			plot(file+"/sbs/",name);
		}else{
			//name=file.split("_")[1];
			plot(file+"/",selname.split("_")[1]);
		}
		readingtwiss();*/
		
	//}
	
	
	private void fillsettings(){
		listmodelleft.addElement("IP");
		listmodelleft.addElement("Collimators");
		listmodelleft.addElement("Transverse Dampers");
	}
	
	/*
	 * Setting env and paths
	 */
	private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	
	/**
	 * GUI
	 */
	
	/*
	 * general
	 */
	private JTabbedPane mainsbs = new JTabbedPane();
	private ArrayList<Chart> chartlist= new ArrayList<Chart>();
	
	/*
	 * elementpanel
	 */
	private DefaultTableModel tablemodel1 = new DefaultTableModel();
	private JTable table1 = new JTable(tablemodel1);
	private JScrollPane scrolltable1 = new JScrollPane(table1);
	
	private DefaultTableModel tablemodel2 = new DefaultTableModel();
	private JTable table2 = new JTable(tablemodel2);
	private JScrollPane scrolltable2 = new JScrollPane(table2);	
	
	private DefaultTableModel tablemodel3 = new DefaultTableModel();
	private JTable table3 = new JTable(tablemodel3);
	private JScrollPane scrolltable3 = new JScrollPane(table3);
	
	private JTabbedPane tabstable = new JTabbedPane();
	
	private JLabel labelset = new JLabel("Element list : ");	
	private DefaultListModel listmodelleft = new DefaultListModel();
	private JList listleft = new JList(listmodelleft);
	private JScrollPane scrolltableleft = new JScrollPane(listleft);		
	
	private JLabel labelop = new JLabel("Operational list : ");
	private DefaultListModel listmodelright = new DefaultListModel();
	private JList listright = new JList(listmodelright);	
	private JScrollPane scrolltableright = new JScrollPane(listright);	
	
	private JButton buttonsetting = new JButton("Settings");
	private JButton buttonadd = new JButton("Add element list");
	private JButton buttonremove = new JButton("Remove element list");
	private JButton buttonrun = new JButton("Run");	
	private JButton buttonsave = new JButton("Save to file");		
	
	private GridBagLayout gridBag = new GridBagLayout();
	
	private JPanel panelelemenet = new JPanel();
	
	//private JComboBox combo = new JComboBox();
	private ArrayList<String> transmap = new ArrayList<String>();
	
	/*
	 * plot
	 */
	private DefaultDataSource ds1;
	private ErrorDataSetRenderer errorRenderer;
	
	private TFSReader tfsread;
	private double[] d1;
	private double[] d2;
	private double[] d3;
	private double[] d4;
	private double[] d5;

	
	private void plot(String file,String name){
		chartb1.removeAllRenderers();
		chartb2.removeAllRenderers();
		chartc1.removeAllRenderers();
		chartc2.removeAllRenderers();
		//System.out.println(file+"/sbs/sbsPhasext_"+name+".out"+" "+new File(file+"/sbs/sbsPhasext_"+name+".out").exists());
		if(new File(file+"/sbs/sbsphasext_"+name+".out").exists()){
			//phase x
			ds1=new DefaultDataSource();
			errorRenderer=new ErrorDataSetRenderer();
			errorRenderer.setDrawBars(false);
			errorRenderer.setDrawPolyLine(true);
			errorRenderer.setStyle(0, new Style(Color.red, new Color(255, 0, 0)));
			chartb1.addRenderer(errorRenderer);
			tfsread= new TFSReader();
			tfsread.loadTable(file+"/sbs/sbsphasext_"+name+".out");
			d1=tfsread.getDoubleData("MODEL_S");
			d2=tfsread.getDoubleData("PHASEXT");
			d3=tfsread.getDoubleData("ERRORX");
			d4=tfsread.getDoubleData("PHASE_PLAY");
			d5=tfsread.getDoubleData("XXX");
					
			ds1.addDataSet(new DefaultErrorDataSet("measurement",d1,d2,d3));
			ds1.addDataSet(new DefaultErrorDataSet("model",d1,d4,d5));
			errorRenderer.setVisible(true);
			errorRenderer.setDataSource(ds1);
			
			//phase y
			ds1=new DefaultDataSource();
			errorRenderer=new ErrorDataSetRenderer();
			errorRenderer.setDrawBars(false);
			errorRenderer.setDrawPolyLine(true);
			errorRenderer.setStyle(0, new Style(Color.red, new Color(255, 0, 0)));
			chartb2.addRenderer(errorRenderer);
			tfsread= new TFSReader();
			tfsread.loadTable(file+"/sbs/sbsphaseyt_"+name+".out");
			d1=tfsread.getDoubleData("MODEL_S");
			d2=tfsread.getDoubleData("PHASEYT");
			d3=tfsread.getDoubleData("ERRORY");
			d4=tfsread.getDoubleData("PHASE_PLAY");
			d5=tfsread.getDoubleData("XXX");
					
			ds1.addDataSet(new DefaultErrorDataSet("measurement",d1,d2,d3));
			ds1.addDataSet(new DefaultErrorDataSet("model",d1,d4,d5));
			errorRenderer.setVisible(true);
			errorRenderer.setDataSource(ds1);	
			
			//couple f1001
			ds1=new DefaultDataSource();
			errorRenderer=new ErrorDataSetRenderer();
			errorRenderer.setDrawBars(false);
			errorRenderer.setDrawPolyLine(true);
			errorRenderer.setStyle(0, new Style(Color.red, new Color(255, 0, 0)));
			chartc1.addRenderer(errorRenderer);
			tfsread= new TFSReader();
			tfsread.loadTable(file+"/sbs/sbscouple_"+name+".out");
			d1=tfsread.getDoubleData("S_MODEL");
			d2=tfsread.getDoubleData("f1001_EXP");
			d3=tfsread.getDoubleData("f1001err_EXP");
			d4=tfsread.getDoubleData("f1001_PLAY");
			d5=tfsread.getDoubleData("ef1001_PLAY");
					
			ds1.addDataSet(new DefaultErrorDataSet("measurement",d1,d2,d3));
			ds1.addDataSet(new DefaultErrorDataSet("model",d1,d4,d5));
			errorRenderer.setVisible(true);
			errorRenderer.setDataSource(ds1);
			
			//couple f1010
			ds1=new DefaultDataSource();
			errorRenderer=new ErrorDataSetRenderer();
			errorRenderer.setDrawBars(false);
			errorRenderer.setDrawPolyLine(true);
			errorRenderer.setStyle(0, new Style(Color.red, new Color(255, 0, 0)));
			chartc2.addRenderer(errorRenderer);
			tfsread= new TFSReader();
			tfsread.loadTable(file+"/sbs/sbscouple_"+name+".out");
			d1=tfsread.getDoubleData("S_MODEL");
			d2=tfsread.getDoubleData("f1010_EXP");
			d3=tfsread.getDoubleData("f1010err_EXP");
			d4=tfsread.getDoubleData("f1010_PLAY");
			d5=tfsread.getDoubleData("ef1010_PLAY");
					
			ds1.addDataSet(new DefaultErrorDataSet("measurement",d1,d2,d3));
			ds1.addDataSet(new DefaultErrorDataSet("model",d1,d4,d5));
			errorRenderer.setVisible(true);
			errorRenderer.setDataSource(ds1);		
		}		
	}
	
	/*
	 * segmentpanel
	 */
	//private JLabel labelist = new JLabel("Segments :");
	//private DefaultListModel listlist = new DefaultListModel();
	//private JList listsetting = new JList(listlist);
	//private JScrollPane scrolllist = new JScrollPane(listsetting);
	
	private JLabel labelcorr = new JLabel("Segment settings :");
	private DefaultTableModel tablemodel = new DefaultTableModel();
	private JTable table = new JTable(tablemodel);
	private JScrollPane scrolltable = new JScrollPane(table);
	
	private JTabbedPane tabs = new JTabbedPane();
	
	private JButton buttonclear = new JButton("Remove Segment ");
	private JButton buttonrunSbs = new JButton("Run segment ");
	
	private Chart chartb1 = new Chart();
	private Chart chartb2 = new Chart();
	private JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,chartb1,chartb2);
	
	private Chart chartc1 = new Chart();
	private Chart chartc2 = new Chart();
	private JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,chartc1,chartc2);	
	
	private Chart chartm1 = new Chart();
	private Chart chartm2 = new Chart();
	private JSplitPane pane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,chartm1,chartm2);	
	
	
	
	private GridBagLayout gridBag1 = new GridBagLayout();
	
	private JPanel panelsbs = new JPanel();
	private JPanel paneloptions = new JPanel();
	
	
	private void createGUI(){
		
		//
		// segment panel
		//
		//setting view
		buttonclear.setBackground(Color.RED); 
		chartlist.add(chartb1);
		chartlist.add(chartb2);
		chartlist.add(chartc1);
		chartlist.add(chartc2);
		chartlist.add(chartm1);
		chartlist.add(chartm2);
		InitialChart();
		pane1.setResizeWeight(0.5);
		pane2.setResizeWeight(0.5);
		pane3.setResizeWeight(0.5);
		tabs.addTab("Phase", pane1);
		tabs.addTab("Coupling", pane2);	
		tabs.addTab("Off-momentum", pane3);	
		panelsbs.setLayout(new BorderLayout());
		paneloptions.setLayout(gridBag1);
		gridBag1.rowWeights = new double[] {1,1,1,1,1,1};
		gridBag1.rowHeights = new int[] {20,150,20,150,20,20};
		gridBag1.columnWeights = new double[] {1};
		gridBag1.columnWidths = new int[] {80};
		//gridBag1.setConstraints(labelist, gridconstrainer(0,10,0,0,1,1,0,0,paneloptions,labelist));
		//gridBag1.setConstraints(scrolllist, gridconstrainer(0,10,0,100,1,1,0,1,paneloptions,scrolllist));
		gridBag1.setConstraints(labelcorr, gridconstrainer(0,10,0,5,1,1,0,0,paneloptions,labelcorr));
		gridBag1.setConstraints(scrolltable, gridconstrainer(5,10,10,5,1,3,0,1,paneloptions,scrolltable));
		gridBag1.setConstraints(buttonclear, gridconstrainer(5,50,10,50,1,1,0,4,paneloptions,buttonclear));		
		gridBag1.setConstraints(buttonrunSbs, gridconstrainer(5,50,10,50,1,1,0,5,paneloptions,buttonrunSbs));		
        panelsbs.add(paneloptions,BorderLayout.WEST);
        panelsbs.add(tabs,BorderLayout.CENTER);
		tablemodel.addColumn("Segment");
		tablemodel.addColumn("Start BPM");
		tablemodel.addColumn("End BPm");
			
		// elementpanel
		//
		//
		panelelemenet.setLayout(gridBag);
		buttonadd.setBackground(Color.GREEN);
		buttonremove.setBackground(Color.RED);
		
		//tabs
		tabstable.setTabPlacement(SwingConstants.BOTTOM);
		tabstable.addTab("Beta",scrolltable1 );
		tablemodel1.addColumn("Name");
		tablemodel1.addColumn("\u03b2x");
		tablemodel1.addColumn("\u03b2xe");
		tablemodel1.addColumn("\u03b2y");
		tablemodel1.addColumn("\u03b2ye");	
		tabstable.addTab("Couple",scrolltable2 );
		tablemodel2.addColumn("Name");
		tablemodel2.addColumn("F1001");
		tablemodel2.addColumn("F1001e");
		tablemodel2.addColumn("F1010");
		tablemodel2.addColumn("F1010e");	
		tabstable.addTab("Off-momentum",scrolltable3 );	
		tablemodel3.addColumn("Name");
		tablemodel3.addColumn("Dx");
		tablemodel3.addColumn("Dxe");
		tablemodel3.addColumn("Dy");
		tablemodel3.addColumn("Dye");
		
		
		//setting view
		gridBag.rowWeights = new double[] {1,1,1,1,1,1};
		gridBag.rowHeights = new int[] {200,30,30,30,60,30};
		gridBag.columnWeights = new double[] {1,1,1};
		gridBag.columnWidths = new int[] {200,40,200};
		gridBag.setConstraints(tabstable, gridconstrainer(5,10,0,5,3,1,0,0,panelelemenet,tabstable));
		gridBag.setConstraints(labelset, gridconstrainer(0,10,10,20,1,1,0,1,panelelemenet,labelset));
		gridBag.setConstraints(scrolltableleft, gridconstrainer(5,10,10,5,1,3,0,2,panelelemenet,scrolltableleft));
		gridBag.setConstraints(buttonsetting, gridconstrainer(5,10,10,5,1,1,0,5,panelelemenet,buttonsetting));
		gridBag.setConstraints(buttonadd, gridconstrainer(5,10,10,5,1,1,1,2,panelelemenet,buttonadd));		
		gridBag.setConstraints(buttonremove, gridconstrainer(5,10,10,5,1,1,1,3,panelelemenet,buttonremove));		
		gridBag.setConstraints(labelop, gridconstrainer(5,10,10,5,1,1,2,1,panelelemenet,labelop));
		gridBag.setConstraints(scrolltableright, gridconstrainer(5,10,10,5,1,3,2,2,panelelemenet,scrolltableright));
		gridBag.setConstraints(buttonrun, gridconstrainer(5,10,10,5,1,1,2,5,panelelemenet,buttonrun));	
		//gridBag.setConstraints(combo, gridconstrainer(5,10,10,5,1,1,1,4,panelelemenet,combo));	
		gridBag.setConstraints(buttonsave, gridconstrainer(5,10,10,5,1,1,1,5,panelelemenet,buttonsave));	
		
		
		
		
		//converging
		setLayout(new BorderLayout());
		mainsbs.addTab("Segment", panelsbs);
		mainsbs.addTab("Element", panelelemenet);
		add(mainsbs, BorderLayout.CENTER);
		
	}
	
	/*
	 * Initial chart
	 */
	private double[] xxx={0};
	
	
	private void InitialChart(){
		for(int i=0;i<chartlist.size();i++){
			chartlist.get(i).setInteractors(ChartInteractor.createEditIteractors());
			chartlist.get(i).setDataSet(new DefaultDataSet("Empty",xxx,xxx));
		}
	}
	
	/*
	 * grid constraints
	 */
	private GridBagConstraints gridconstrainer(int top,int left,int bottom,int right,int gridwidth,int gridheight,int gridx,int gridy,JPanel panel, JComponent comp){
		GridBagConstraints gridCons = new GridBagConstraints();
		gridCons.insets = new Insets(top,left,bottom,right);
		gridCons.anchor = GridBagConstraints.CENTER;
		gridCons.gridwidth = gridwidth;
		gridCons.gridheight =gridheight;
		gridCons.gridx = gridx;
		gridCons.gridy = gridy;
		gridCons.fill=GridBagConstraints.BOTH;
		panel.add(comp);
		
		return gridCons;
	}
	
	/*
	 * System call
	 */
	private String command;
	//private HashMap<String,String[]> datamap = new HashMap();
	//private String[] dataformap;
	private Boolean pass;
	private int cut=10;
	private Task task;
	
	
	public void systemcallforsegment(final String startbpm, final String endbpm, final String segname, final String filename){
		
		/*dataformap=new String[4];
		dataformap[0]=startbpm;
		dataformap[1]=endbpm;
		dataformap[2]=segname;
		dataformap[3]=filename;*/
		
		//datamap.put(new File(filename).getName().toString()+"_"+segname, dataformap);
		
		if(!new File(filename+"/sbs/").exists()){
			pass=new File(filename+"/sbs/").mkdir();
		}else{
			pass=true;
		}
		
		if(pass){
			MessageManager.getConsoleLogger().info(" Sbspanel => Sending command for sbs");
			command=controller.getPathDataForKey("python")+" "+controller.getPathDataForKey("sbsmeas")+" -a "+controller.getBeamSelectionData().getAccelerator()+" -f "+filename+" -s "
			+startbpm+","+endbpm+","+segname+" -t "+controller.getPathDataForKey("opticspath")+"/twiss_elements.dat"+" -p "+filename+"/sbs/ -m "+controller.getPathDataForKey("madx")
			+" -b "+ controller.getBeamSelectionData().getProgramLocation() + " -c "+cut;
			
			task = new Task() {
				protected Object construct() {
				try{
			
				    Systemcall.execute(command,"sbs for segment",env,filename+"/sbs/",true);
					MessageManager.getConsoleLogger().info(" Sbspanel => Command "+command);
					
					if(!transmap.contains(segname)){
						tablemodel.addRow(new Object[]{segname,startbpm,endbpm});
					}
					transmap.add(segname);
					plot(filename,segname);
			
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("AnalysisPanel => Caused by exception: ",ex,null);
					MessageManager.info("AnalysisPanel => Error",null);
				}
				
				return null;
				}
				
				};
				
				task.setName("Sbspanel segment");
				task.setCancellable(false);
				task.start();
			
			
		}else{
			MessageManager.error(" Sbspanel => Cannot create dir", null, null);
			MessageManager.getConsoleLogger().error(" Sbspanel => Cannot create dir : "+filename+"/sbs/ \n sbs will not run !! Contact expert");
		}
				
	}
	
	/*
	 * system call for element
	 */
	private TFSReader tfsread4element;
	private String[] names;
	private double[] double1;
	private double[] double2;
	private double[] double3;
	private double[] double4;
	private ArrayList<String> list1;
	private ArrayList<String> list2;
	private ArrayList<String> list3;
	
	private void filltableElement(String path){
		//empty
		
		for(int i=table1.getRowCount()-1;i>0;i--){
				tablemodel1.removeRow(i);
		}
		
		for(int i=table2.getRowCount()-1;i>0;i--){
				tablemodel2.removeRow(i);
		}
		
		for(int i=table3.getRowCount()-1;i>0;i--){
				tablemodel3.removeRow(i);
		}
		
		list1=new ArrayList<String>();
		list2=new ArrayList<String>();
		list3=new ArrayList<String>();
		
		
		//bet
		tfsread4element=new TFSReader();
		tfsread4element.loadTable(path+"/sbs_summary_bet.out");
		names=tfsread4element.getStringData("NAME");
		double1=tfsread4element.getDoubleData("BETXP");
		double2=tfsread4element.getDoubleData("ERRBETXP");
		double3=tfsread4element.getDoubleData("BETY");
		double4=tfsread4element.getDoubleData("ERRBETY");
		
		for(int i=0;i<names.length;i++){
			if(list1.contains(names[i])){
				tablemodel1.removeRow(list1.indexOf(names[i]));
				list1.remove(list1.indexOf(names[i]));
				tablemodel1.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}else{
				tablemodel1.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}
			list1.add(names[i]);
		}
		
		
		//couple
		tfsread4element=new TFSReader();
		tfsread4element.loadTable(path+"/sbs_summary_cou.out");
		names=tfsread4element.getStringData("NAME");
		double1=tfsread4element.getDoubleData("f1001_PLAY");
		double2=tfsread4element.getDoubleData("ef1001_play");
		double3=tfsread4element.getDoubleData("f1010_PLAY");
		double4=tfsread4element.getDoubleData("ef1010_play");
		
		for(int i=0;i<names.length;i++){
			if(list2.contains(names[i])){
				tablemodel2.removeRow(list2.indexOf(names[i]));
				list2.remove(list2.indexOf(names[i]));
				tablemodel2.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}else{
				tablemodel2.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}
			list2.add(names[i]);
		}
		
		//disp
		tfsread4element=new TFSReader();
		tfsread4element.loadTable(path+"/sbs_summary_disp.out");	
		names=tfsread4element.getStringData("NAME");
		double1=tfsread4element.getDoubleData("DX_PLAY");
		double2=tfsread4element.getDoubleData("EDX_PLAY");
		double3=tfsread4element.getDoubleData("DY_PLAY");
		double4=tfsread4element.getDoubleData("EDY_PLAY");		
		
		for(int i=0;i<names.length;i++){
			if(list3.contains(names[i])){
				tablemodel3.removeRow(list3.indexOf(names[i]));
				list3.remove(list3.indexOf(names[i]));
				tablemodel3.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}else{
				tablemodel3.addRow(new Object[]{names[i],double1[i],double2[i],double3[i],double4[i]});
			}
			list3.add(names[i]);
		}
		
	}
	

	private String[] selkeys;
	
	private void systemcallforelement(){
		
		selkeys=new String[listmodelright.getSize()];
			
		for(int i=0;i<listmodelright.getSize();i++){
			selkeys[i]=listmodelright.get(i).toString();
		}
		
		
		if(!new File(Filename+"/sbs/").exists()){
			pass=new File(Filename+"/sbs/").mkdir();
		}else{
			pass=true;
		}
		
		if(pass){
			
			command=controller.getPathDataForKey("python")+" "+controller.getPathDataForKey("sbsmeas")+" -a "+controller.getBeamSelectionData().getAccelerator()+" -f "+Filename+" -s "
			+append(selkeys)+" -t "+controller.getPathDataForKey("opticspath")+"/twiss_elements.dat"+" -p "+Filename+"/sbs/ -m "+controller.getPathDataForKey("madx")
			+" -b "+controller.getBeamSelectionData().getProgramLocation()+" -c "+cut;
			
			task = new Task() {
				protected Object construct() {
				try{
			
						MessageManager.getConsoleLogger().info(" Sbspanel => Command "+command);
						Systemcall.execute(command,"sbs for element",env,Filename+"/sbs/",true);
						filltableElement(Filename+"/sbs/");
						MessageManager.info(" Sbspanel => sbsPanel finished",null);
	
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("AnalysisPanel => Caused by exception: ",ex,null);
					MessageManager.info("AnalysisPanel => Error",null);
				}return null;}};
		
				task.setName("Sbspanel element");
				task.setCancellable(false);
				task.start();
		}else{
			MessageManager.error(" Sbspanel => Cannot create dir", null, null);
			MessageManager.getConsoleLogger().error(" Sbspanel => Cannot create dir : "+Filename+"/sbs/ \n sbs will not run !! Contact expert");
		}
		
	}
	
	// appending
	private StringBuffer sbuf;
	private String append(String[] values){
		sbuf=new StringBuffer();
		for(int i=0;i<values.length;i++){
			if(i==0){
				sbuf.append(values[i]);
			}else{
				sbuf.append(","+values[i]);
			}
		}
		return sbuf.toString();
	}
	
	
	/*
	 * reading twiss
	 */
	private TFSReader tfsreadval;
	private String[] data;
	private String[] data2;
	private double[] datas;
	private HashMap<String,String> ip= new HashMap<String, String>();
	private HashMap<String,String> colli= new HashMap<String, String>();
	private HashMap<String,String> adt= new HashMap<String, String>();
	
	private HashMap<String,Double> map4ele = new HashMap<String, Double>();
	
	private void readingtwiss(){
	
		ip.clear();
		colli.clear();
		adt.clear();
		
		tfsreadval = new TFSReader();
		tfsreadval.loadTable(controller.getPathDataForKey("opticspath")+"/twiss_elements.dat");
		
		MessageManager.getConsoleLogger().info("SBSPanel => Filling twiss");
		
		data=tfsreadval.getStringData("NAME");
		data2=tfsreadval.getStringData("KEYWORD");
		datas=tfsreadval.getDoubleData("S");
		
		for(int i=0;i<data.length;i++){
			map4ele.put(data[i], datas[i]);
			if(data[i].contains("IP") && !data[i].contains("IP.L1")){
				ip.put(data[i],"Yes");
			}
			if(data2[i].equals("RCOLLIMATOR")){
				colli.put(data[i],"Yes");
			}
			
			if(data[i].contains("ADT")){
				adt.put(data[i],"Yes");
			}			
		}
		
		log.info(ip.size()+" "+colli.size()+" "+adt.size());
		
	}
	
	/*
	 * reinitialize
	 */
	private void reinit(){		
			for(int i=table.getRowCount()-1;table.getRowCount()>0;i--){
				tablemodel.removeRow(i);
			}
	}
	
	/*
	 *  fill table segment
	 */
	private HashMap<String, String[]> premap=new HashMap<String, String[]>();
	private void SegmentTable(String accel){
		if(accel.equals("LHCB1")){
			premap=seg.LHCB1();
		}else if(accel.equals("LHCB2")){
			premap=seg.LHCB2();
		}
		transmap.add("IP1");
		transmap.add("IP2");
		transmap.add("IP3");
		transmap.add("IP4");
		transmap.add("IP5");
		transmap.add("IP6");
		transmap.add("IP7");
		transmap.add("IP8");
		
		tablemodel.addRow(new Object[]{"IP1",premap.get("IP1")[0],premap.get("IP1")[1]});
		tablemodel.addRow(new Object[]{"IP2",premap.get("IP2")[0],premap.get("IP2")[1]});
		tablemodel.addRow(new Object[]{"IP3",premap.get("IP3")[0],premap.get("IP3")[1]});
		tablemodel.addRow(new Object[]{"IP4",premap.get("IP4")[0],premap.get("IP4")[1]});
		tablemodel.addRow(new Object[]{"IP5",premap.get("IP5")[0],premap.get("IP5")[1]});
		tablemodel.addRow(new Object[]{"IP6",premap.get("IP6")[0],premap.get("IP6")[1]});
		tablemodel.addRow(new Object[]{"IP7",premap.get("IP7")[0],premap.get("IP7")[1]});
		tablemodel.addRow(new Object[]{"IP8",premap.get("IP8")[0],premap.get("IP8")[1]});
	}
	
	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(800,800));
		frame.add(new sbsPanel());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}*/

}
