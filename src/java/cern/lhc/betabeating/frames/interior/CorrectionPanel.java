package cern.lhc.betabeating.frames.interior;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.jdve.Chart;
import cern.jdve.ChartRenderer;
import cern.jdve.data.DataSet;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultErrorDataSet;
import cern.jdve.event.ChartInteractionEvent;
import cern.jdve.event.ChartInteractionListener;
import cern.jdve.interactor.DataPickerInteractor;
import cern.jdve.utils.DisplayPoint;
import cern.lhc.betabeating.datahandler.TFSReader;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.exterior.SendCorrections;
import cern.lhc.betabeating.remote.services;

public class CorrectionPanel extends JPanel {
    private static final long serialVersionUID = 1791249681602594814L;
    private static final Logger log = Logger.getLogger(CorrectionPanel.class);
    
    private Controller controller = null;
    private services service;
	private SendCorrections scor;
	
	public CorrectionPanel(Controller controller){
	    this.controller = controller;
		CreateGUI();
		Listeners();
	}
	
	private HashMap<String,String> translatedmap= new HashMap<String, String>();
	
	/*
	 * Setting env and paths
	 */
	private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	/**
	 * Listeners
	 */
	
	private ListSelectionListener listlistener4data;
	private Task task;
	private DataPickerInteractor dataPicker ;
	
	private void Listeners(){
		list.addListSelectionListener(listlistener4data=new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		    	  
		    	  if(evt.getValueIsAdjusting()){
		    		 // chartcor.removeAllRenderers();
		    		  load(list.getSelectedValue().toString());
		    		//  System.out.println("Loading");
		    	  }
		      }});
		
		tabs4cor.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) { JTabbedPane pane = (JTabbedPane)evt.getSource(); 
			if(tabs4cor.getSelectedIndex()==0){
				boxpos.setText("Plot s position");
			}else{
				boxpos.setText("Plot difference");
			}
		}});
		
		combo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			//plot(combo.getSelectedItem().toString());
		}});
		
		button.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			task = new Task() {
				protected Object construct() {
				try{
					String method;
					if(list.getSelectedValue().toString().contains("coupling")){
						method="coupling";
					}else{
						method="beta";
					}
					service= new services(controller.getBeamSelectionData().getAccelerator());
					scor = new SendCorrections(service, method);
					//System.out.println("Number of values to be add "+values4cor.length);
					
					/////// test
					//TFSReader tfs = new TFSReader();
					//tfs.loadTable("/afs/cern.ch/eng/sl/lintrack/LHC_commissioning2011/EXP_22-2-11_Squeeze/corrections/changeparameters.tfs");
					//names=tfs.getStringData("NAMES");
					//values4cor=tfs.getDoubleData("DELTA");
						
						
					scor.setvalues(names, values4cor);
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("BPMPanel => Caused by exception: ",ex,null);
					MessageManager.info("BPMPanel => Error",null);
				}
				
				return null;
				}
				
				};
				task.setName("Correction");
				task.setCancellable(false);
				task.start();
		}});
		////datapicker
	     dataPicker.addChartInteractionListener(new ChartInteractionListener() {
	            public void interactionPerformed(ChartInteractionEvent evt) {
	                if (evt.getMouseEvent().getButton() == MouseEvent.BUTTON1) {
	                	DisplayPoint p = evt.getDisplayPoint();
		                DataSet dataset = p.getDataSet();
	                	JFrame frame = new JFrame();
	                	JOptionPane.showMessageDialog(frame, "The selected magnet is "+names[p.getIndex()]);
	                }
	            }

				
	        });
		chartcor.addInteractor(dataPicker);
		
	}
	
	/*
	 * for plotting
	 */
	
	private TFSReader tfsbetax;
	private TFSReader tfsbetay;
	private TFSReader tfsphasex;
	private TFSReader tfsphasey;
	private TFSReader tfsndx;
	
	private TFSReader tfscouple;
	private TFSReader tfsndy;
	
	private TFSReader tfscor;
	private TFSReader tfsmodel;
	
	
	private void load(String selecteditem){
		listofReaders=correctionmap.get(selecteditem);
		
		if(selecteditem.contains("_beta")){
			tfsbetax=listofReaders.get("betax");
			tfsbetay=listofReaders.get("betay");
			tfsndx=listofReaders.get("dx");
			tfsmodel=listofReaders.get("twiss");
		}else if(selecteditem.contains("_coupling")){
			tfscouple=listofReaders.get("couple");
			tfsndy=listofReaders.get("dy");	
			tfsmodel=listofReaders.get("twiss");				
		}
		plot(selecteditem);	
	}
	
	
	private double[] values;
	private double[] s;
	private double[] errvalues;
	private void plot(String selecteditem){
		
			
		if(selecteditem.contains("_beta")){
				// hor
				values=tfsbetax.getDoubleData("ERRBETX");
				s=tfsbetax.getDoubleData("STDBETX");
				errvalues=new double[values.length];
				for(int i=0;i<values.length;i++){
					errvalues[i]=Math.sqrt(values[i]*values[i]+s[i]*s[i]);
				}
				values=tfsbetax.getDoubleData("BETX");
				s=tfsbetax.getDoubleData("S");
				charthor.setDataSet(new DefaultErrorDataSet("Hor beta",s,values,errvalues));
				
				//ver
				values=tfsbetax.getDoubleData("ERRBETX");
				s=tfsbetax.getDoubleData("STDBETX");
				errvalues=new double[values.length];
				for(int i=0;i<values.length;i++){
					errvalues[i]=Math.sqrt(values[i]*values[i]+s[i]*s[i]);
				}
				values=tfsbetax.getDoubleData("BETY");
				s=tfsbetax.getDoubleData("S");
				chartver.setDataSet(new DefaultErrorDataSet("Ver beta",s,values,errvalues));	
				
			
				values=tfsndx.getDoubleData("NDX");
				errvalues=tfsndx.getDoubleData("STDNDX");
				s=tfsndx.getDoubleData("S");
				charthor.setDataSet(new DefaultErrorDataSet("Hor beta",s,values,errvalues));
		
		}
		
		
		
		if(translatedmap.containsKey(selecteditem)){
			reader4file2=new TFSReader();
			reader4file2.loadTable(translatedmap.get(selecteditem));
			values4cor=reader4file2.getDoubleData("DELTA");
			names=reader4file2.getStringData("NAME");
			s=new double[values4cor.length];
			for(int i=0;i<values4cor.length;i++){
				s[i]=i;
			}
			chartcor.setRenderingType(ChartRenderer.BAR);
			chartcor.setDataSet(new DefaultDataSet("Value",s,values4cor));
		}
	}
	
	
	
	/**
	 * datahandling
	 */
	private HashMap<String,HashMap<String,TFSReader>>  correctionmap= new HashMap<String, HashMap<String,TFSReader>>();

	/*
	 * Adding file to this panel for different methods
	 */
	private File namefile;
	private String file2add;
	private TFSReader reader4file;
	private TFSReader reader4file2;
	private String[] names;
	private double[] values4cor;
	
	private HashMap<String,TFSReader> listofReaders= new HashMap<String, TFSReader>();
	
	private TFSReader loadreader(String file){
		reader4file=new TFSReader();
		reader4file.loadTable(file);
		
		return reader4file;
	}
	
	public void addFile(String filename, String method){
		
		//
		// listofReaders contains the measurements, corrections  and simulations
		//
		list.removeListSelectionListener(listlistener4data);
		reader4file= new TFSReader();
		namefile=new File(filename);
		
		if(method.equals("COUPLING")){
			file2add=namefile.getName()+"_coupling";
			MessageManager.getConsoleLogger().info("CorrectionPanel => Adding file "+namefile.getName()+" correction method coupling");
			listofReaders.put("couple",loadreader(namefile+"/getcouple.out"));
			listofReaders.put("dy",loadreader(namefile+"/getNDy.out"));
			listofReaders.put("change",loadreader(namefile+"/changeparameters_couple.tfs"));
			reader4file2=loadreader(namefile+"/changeparameters_couple.tfs");
			listofReaders.put("twiss",loadreader(namefile+"/twiss.corrected.dat"));
			if(!listmodel.contains(file2add)){			
				listmodel.addElement(file2add);
			}
			translatedmap.put(file2add, namefile.toString()+"/changeparameters_couple.tfs");
			correctionmap.put(file2add, listofReaders);
			fillecombo("COUPLING");
		}else if(method.equals("BETA")){
			file2add=namefile.getName()+"_beta";	
			MessageManager.getConsoleLogger().info("CorrectionPanel => Adding file "+namefile.getName()+" correction method beta");	
			if(!listmodel.contains(file2add)){
				listmodel.addElement(file2add);
			}
			listofReaders.put("betax",loadreader(namefile+"/getbetax.out"));
			listofReaders.put("betay",loadreader(namefile+"/getbetay.out"));	
			listofReaders.put("dx",loadreader(namefile+"/getNDx.out"));
			listofReaders.put("change",loadreader(namefile+"/changeparameters.tfs"));
			reader4file2=loadreader(namefile+"/changeparameters.tfs");
			listofReaders.put("twiss",loadreader(namefile+"/twiss.corrected.dat"));
			translatedmap.put(file2add, namefile.toString()+"/changeparameters.tfs");
			correctionmap.put(file2add, listofReaders);
			fillecombo("BETA");
		}else{
			MessageManager.error("CorrectionPanel => Method does not exist",null,null);
			MessageManager.getConsoleLogger().error("CorrectionPanel => Method does not exist \n Contact expert",null);
		}
		
		
		values4cor=reader4file2.getDoubleData("DELTA");
		names=reader4file2.getStringData("NAME");
		s=new double[values4cor.length];
		for(int i=0;i<values4cor.length;i++){
			s[i]=i;
		}
		
		log.info(values4cor.length);
		
		chartcor.setRenderingType(ChartRenderer.BAR);
		chartcor.setDataSet(new DefaultDataSet("Value",s,values4cor));
			
		
		list.addListSelectionListener(listlistener4data);		
	}
	
	/**
	 * For plotter
	 */
	private void fillecombo(String method){
		
		combo.removeAllItems();
		
		if(method.equals("COUPLING")){
			combo.addItem("F-TERMS");
			combo.addItem("NDY");
		}else if(method.equals("BETA")){
			combo.addItem("BETA");
			combo.addItem("NDX");
		}
	}
	
	/**
	 *  Create GUI
	 */
	
	//tab model
	private JSplitPane split;
	
	//side
	private DefaultListModel listmodel = new DefaultListModel();
	private JList list = new JList(listmodel);
	private JScrollPane scroller = new JScrollPane(list);
	
	private JCheckBox boxpos = new JCheckBox("Plot s position",true);
	
	private JLabel labellist = new JLabel("Files :");
	private JComboBox combo = new JComboBox();
	
	private JButton button = new JButton("Send corrections");
	
	private Chart chartcor = new Chart();
	private Chart charthor = new Chart();
	private Chart chartver = new Chart();
	
	private JTabbedPane tabs4cor = new JTabbedPane();
	
	private GridBagLayout gridBag0;
	
	// 
	
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
	
	public void CreateGUI(){
		
		//chart
		dataPicker = new DataPickerInteractor();
		dataPicker.getPointCoordPane().getLabelRenderer().setBackground(new Color(204, 204, 255));
		//charthor.addInteractor(ChartInteractor.ZOOM);
		
		gridBag0 = new GridBagLayout();
		setLayout(gridBag0);
		setPreferredSize(new Dimension(500,500));
		gridBag0.rowWeights = new double[] {1,1,1,1,1,1};
		gridBag0.rowHeights = new int[] {10,60,80,20,10,10,70};
		gridBag0.columnWeights = new double[] {0.2,1.0};
		gridBag0.columnWidths = new int[] {40,250};
		
		//split
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,charthor,chartver);
		split.setResizeWeight(0.5);
		split.setOneTouchExpandable(true);
		
		initialChart(chartcor);
		initialChart(charthor);
		initialChart(chartver);
		
		//tabs
		tabs4cor.addTab("Strengths",chartcor );
		//tabs4cor.addTab("Model", split);
		
		// add evt together
		gridBag0.setConstraints(labellist, gridconstrainer(5,10,0,10,1,1,0,0,this,labellist));
		gridBag0.setConstraints(scroller, gridconstrainer(0,10,0,10,1,5,0,1,this,scroller));	
		//gridBag0.setConstraints(boxpos, gridconstrainer(0,10,0,10,1,1,0,5,this,boxpos));	
		//gridBag0.setConstraints(label, gridconstrainer(0,10,0,10,1,1,0,3,this,label));
		//gridBag0.setConstraints(combo, gridconstrainer(0,20,0,20,1,1,0,4,this,combo));	
		gridBag0.setConstraints(button, gridconstrainer(10,10,10,10,1,1,0,6,this,button));
		gridBag0.setConstraints(tabs4cor, gridconstrainer(5,10,0,10,1,7,1,0,this,tabs4cor));	
		//gridBag0.setConstraints(tabs4cor, gridconstrainer(5,10,0,10,1,7,1,0,this,chartcor));	
		
		
		
	}
	
	private double[] xxx={0};
	
	private void initialChart(Chart chart){
		
		//chart.setInteractors(ChartInteractor.createEditIteractors());
		chart.setDataSet(new DefaultDataSet("Data Viewer",xxx,xxx)); 	
	}
	
/*	public static void main(String[] args) {
		JFrame frame= new JFrame();
		frame.setLayout(new BorderLayout());
		CorrectionPanel cpanel=new CorrectionPanel();
		//cpanel.CreateGUI();
		frame.add(cpanel,BorderLayout.CENTER);
		frame.setVisible(true);
		//setPreferredSize(new Dimension(800,800));
		frame.setSize(new Dimension(800,800));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}*/
	

}
