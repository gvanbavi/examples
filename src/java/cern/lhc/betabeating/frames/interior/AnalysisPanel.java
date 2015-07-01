package cern.lhc.betabeating.frames.interior;

import java.awt.BorderLayout;
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
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cern.accsoft.gui.frame.MessageManager;
import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.ChartRenderer;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.interactor.CursorInteractor;
import cern.jdve.utils.DataRange;
import cern.lhc.betabeating.Tools.MathTools;
import cern.lhc.betabeating.datahandler.TFSFilter;
import cern.lhc.betabeating.datahandler.TFSReader;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.exterior.Send4Optics;


public class AnalysisPanel extends JPanel {
    private static final long serialVersionUID = -7677366687977691510L;

    private Controller controller = null;
    /*
	 * constants
	 */
	private double adjustment=0.001;
	
	/*
	 * Main class
	 */
	private ListSelectionListener listlistener4data;
	private ListSelectionListener listlistener4h;
	private ListSelectionListener listlistener4v;
	

	
	/*
	 * Setting env and paths
	 */
	private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	private Send4Optics s4o;
	
	public void sets4o(Send4Optics s4temp){
		s4o=s4temp;
	}
		
	public AnalysisPanel(Controller controller){
	    this.controller = controller;
		/*
		 * Listeners
		 */
		list4data.addListSelectionListener(listlistener4data=new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		    	  
		    	  
		    	  if(evt.getValueIsAdjusting()){
		    		  loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(),"BOTH");
		    	  }
		      }});
		
		listh.addListSelectionListener(listlistener4h=new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		    	  
		    	 
		    	 if(evt.getValueIsAdjusting() && !list4data.isSelectionEmpty()){
		    		 
		    		 loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(),"HOR");
		    	 }
		}});
		
		listv.addListSelectionListener(listlistener4v=new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		
		    	  
		    	  if(evt.getValueIsAdjusting() && !list4data.isSelectionEmpty()){
		    		  loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(),"VER");
		    	  }
		}});
		
		buttonclean.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
					
			if(listh.getSelectedValue().toString().contains("TUNE") && listv.getSelectedValue().toString().contains("TUNE")){
				dataSearch();
			}else{
				MessageManager.getConsoleLogger().warn("AnalysisPanel => Can only clean for TUNE \n select TUNEX and TUNEY to enable cleaner");
			}
		}});
		
		butttondo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			s4o.setmap(allPath);
			s4o.setenv(env);
			s4o.showpanel();
		}});
		
	}
	
	
	

	
	/**
	 * Data handling
	 **/
	private Map<String,ArrayList<TFSReader>> mapsussix = new HashMap<String, ArrayList<TFSReader>>();
	private Map<String,ArrayList<TFSReader>> mapsvd = new HashMap<String, ArrayList<TFSReader>>();
	private Map<String,String> mapsussixPath = new TreeMap<String, String>();
	private Map<String,String> mapsvdPath = new TreeMap<String, String>();
	private Map<String,String> allPath = new TreeMap<String, String>();
	
	private TFSReader tfsreaderfx;
	private TFSReader tfsreaderfy;
	private TFSReader tfsreadersx;
	private TFSReader tfsreadersy;
	private TFSReader tfsreaderux;
	private TFSReader tfsreaderuy;
	private TFSReader tfsreadervx;
	private TFSReader tfsreadervy;
	private TFSReader tfsreadernx;
	private TFSReader tfsreaderny;
	private ArrayList<TFSReader> list4tfs;
	
	public void addFile(String file, String method){
		// Arraylist will contain all the readers for the data coming from SUSSIX or SVD
		// mapsussix=> tfsreaderx,tfsreadery
		// mapsvd=> tfsreaderx,tfsreadery,tfsreaderfx,tfsreaderfy,tfsreadersx,tfsreadersy,tfsreaderux,tfsreaderuy,
		// tfsreadervx,tfsreadervy,tfsreaderhx,tfsreaderhy
		list4data.removeListSelectionListener(listlistener4data);
		list4tfs = new ArrayList<TFSReader>();
		
		TFSReader tfsreaderx = new TFSReader();
		TFSReader tfsreadery = new TFSReader();		
		
		if(method.equals("SUSSIX") && new File(file+"_linx").exists()){
			// readers
			tfsreaderx.loadTable(file+"_linx");
			tfsreadery.loadTable(file+"_liny");
			list4tfs.add(tfsreaderx);
			list4tfs.add(tfsreadery);
			
			String key=new File(file).getName()+"_"+method;
			mapsussix.put(key, list4tfs);
			mapsussixPath.put(key, file);
			allPath.put(key, file);
			if(!model4data.contains(key)){
				model4data.addElement(key);
			}
			
			MessageManager.getConsoleLogger().info("AnalysisPanel => Will add "+key+" to the list for the method "+method);
			
			list4data.addListSelectionListener(listlistener4data);	
			
			loadnames(tfsreaderx,tfsreadery);
			
			if(!list4data.isSelectionEmpty()){
				loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(), "BOTH");
			}else if(!model4data.isEmpty()){
				list4data.setSelectedIndex(0);
				loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(), "BOTH");
			}
			

			
		}else if(method.equals("SVD")){
			// initialize readers
			tfsreaderfx = new TFSReader();
			tfsreaderfy = new TFSReader();
			tfsreadersx = new TFSReader();
			tfsreadersy = new TFSReader();
			tfsreaderux = new TFSReader();
			tfsreaderuy = new TFSReader();
			tfsreadervx = new TFSReader();
			tfsreadervy = new TFSReader();
			tfsreadernx = new TFSReader();
			tfsreaderny = new TFSReader();
			
			tfsreaderx.loadTable(file+"_svdx");
			tfsreadery.loadTable(file+"_svdy");
			tfsreaderfx.loadTable(file+"_Fx");
			tfsreaderfy.loadTable(file+"_Fy");
			tfsreadersx.loadTable(file+"_Sx");
			tfsreadersy.loadTable(file+"_Sy");
			tfsreaderux.loadTable(file+"_Ux");
			tfsreaderuy.loadTable(file+"_Uy");
			tfsreadervx.loadTable(file+"_Vx");
			tfsreadervy.loadTable(file+"_Vy");
			tfsreadernx.loadTable(file+"_nhistx");
			tfsreaderny.loadTable(file+"_nhistx");
			
			list4tfs.add(tfsreaderx);
			list4tfs.add(tfsreadery);
			list4tfs.add(tfsreaderfx);
			list4tfs.add(tfsreaderfy);
			list4tfs.add(tfsreadersx);
			list4tfs.add(tfsreadersy);
			list4tfs.add(tfsreaderux);
			list4tfs.add(tfsreaderuy);
			list4tfs.add(tfsreadervx);
			list4tfs.add(tfsreadervy);
			list4tfs.add(tfsreadernx);
			list4tfs.add(tfsreaderny);
			
			String key=new File(file).getName().toString()+"_"+method;
			mapsvd.put(key, list4tfs);
			mapsvdPath.put(key, file);
			allPath.put(key, file);
			if(!model4data.contains(key)){
				model4data.addElement(key);
			}
			//list4data.setSelectedValue(key, true);
			
			MessageManager.getConsoleLogger().info("AnalysisPanel => Will add "+key+" to the list for the method "+method);
			
			list4data.addListSelectionListener(listlistener4data);	
			
			loadnames(tfsreaderx,tfsreadery);
						
			if(!list4data.isSelectionEmpty()){
				loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(), "BOTH");
			}else if(!model4data.isEmpty()){
				list4data.setSelectedIndex(0);
				loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(), "BOTH");
			}
			
		}else{
			MessageManager.warn("AnalysisPanel => File for "+method+" does not exist",null,null);
			
			MessageManager.getConsoleLogger().warn("AnalysisPanel => Did Analysis finish properly ?");
		}
		

				
	}
	
	
	private ArrayList<String> map4ignore = new ArrayList<String>();
	private Boolean Ignore4table(String data){
		map4ignore.add("NAMES");
		map4ignore.add("S");
		map4ignore.add("SLABEL");
		map4ignore.add("BINDEX");
		
		return map4ignore.contains(data);
		
	}
	
	private void loadnames(TFSReader tfsreaderx,TFSReader tfsreadery){
		
		list4data.removeListSelectionListener(listlistener4data);
		listh.removeListSelectionListener(listlistener4h);
		listv.removeListSelectionListener(listlistener4v);

		
		model4bpmh.removeAllElements();
		model4bpmv.removeAllElements();
		model4bpmv.addElement("None");
		model4bpmh.addElement("None");
		
		ArrayList<String> namesx=tfsreaderx.getKeys();
		ArrayList<String> namesy=tfsreadery.getKeys();
		
		for(int i=0;i<namesx.size();i++){
			if(!Ignore4table(namesx.get(i))){
				model4bpmh.addElement(namesx.get(i));
			}
		}
		
		for(int i=0;i<namesy.size();i++){
			if(!Ignore4table(namesy.get(i))){
				model4bpmv.addElement(namesy.get(i));
			}
		}
			
		// set selection		
		listh.setSelectedValue("TUNEX", true);
		listv.setSelectedValue("TUNEY",true);
		
		listh.addListSelectionListener(listlistener4h);
		listv.addListSelectionListener(listlistener4v);	
		list4data.addListSelectionListener(listlistener4data);		
		

		
	}
	
	
	/*
	 * For removing the data points
	 */
	
	private double value1=0;
	private double value2=0;
	private double up=0;
	private double down=0;
	private int datasetcount=0;
	private String nameDataSet;
	private TFSFilter tfsfilter=new TFSFilter();
	private TFSReader tfs4clean;
	
	private void dataSearch(){
		value1=cursor1.getValue();
		value2=cursor2.getValue();
		
		if(value1>value2){
			up=value1;
			down=value2;
		}else{
			up=value2;
			down=value1;			
		}
		
		DataRange datarangecursors=new DataRange(down, up);
		
		datasetcount=chartdata.getDataSource().getDataSetsCount();
		
		for(int i=0;i<datasetcount;i++){
			
				nameDataSet=chartdata.getDataSource().getDataSet(i).getName();
				
				if(nameDataSet.contains("SUSSIX")){
					if(nameDataSet.contains("_X")){
						nameDataSet=nameDataSet.replace("_X","");
						tfs4clean=mapsussix.get(nameDataSet).get(0);
						if(Insider(tfs4clean.getDoubleData("TUNEX"),up,down)){
							MessageManager.getConsoleLogger().info("AnalysisPanel => Will remove for SUSSIX "+nameDataSet+" horizontal");
							tfsfilter.Clean(mapsussixPath.get(nameDataSet)+"_linx", tfs4clean, up, down, "TUNEX");
							reload(nameDataSet,mapsussixPath.get(nameDataSet)+"_linx","SUSSIX","H");
						}
					}else{
						nameDataSet=nameDataSet.replace("_Y","");
						tfs4clean=mapsussix.get(nameDataSet).get(1);
						if(Insider(tfs4clean.getDoubleData("TUNEY"),up,down)){
							MessageManager.getConsoleLogger().info("AnalysisPanel => Will remove for SUSSIX "+nameDataSet+" vertical");
							tfsfilter.Clean(mapsussixPath.get(nameDataSet)+"_liny", tfs4clean, up, down, "TUNEY");
							reload(nameDataSet,mapsussixPath.get(nameDataSet)+"_liny","SUSSIX","V");
						}
					}
					
					
					
				}else if(nameDataSet.contains("SVD")){
					if(nameDataSet.contains("_X")){
						nameDataSet=nameDataSet.replace("_X","");
						tfs4clean=mapsvd.get(nameDataSet).get(0);
						if(Insider(tfs4clean.getDoubleData("SVDX"),up,down)){
							MessageManager.getConsoleLogger().info("AnalysisPanel => Will remove for SVD "+nameDataSet+" horizontal");
							tfsfilter.Clean(mapsvdPath.get(nameDataSet)+"_svdx", tfs4clean, up, down, "TUNEX");	
							reload(nameDataSet,mapsvdPath.get(nameDataSet)+"_svdx","SVD","H");
						}
					}else{
						nameDataSet=nameDataSet.replace("_Y","");
						tfs4clean=mapsvd.get(nameDataSet).get(1);
						if(Insider(tfs4clean.getDoubleData("SVDY"),up,down)){
							MessageManager.getConsoleLogger().info("AnalysisPanel => Will remove for SVD "+nameDataSet+" vertical");
							tfsfilter.Clean(mapsvdPath.get(nameDataSet)+"_svdy", tfs4clean, up, down, "TUNEY");	
							reload(nameDataSet,mapsvdPath.get(nameDataSet)+"_svdy","SVD","V");
						}
					}
					
					
				}else{
					MessageManager.getConsoleLogger().error("AnalysisPanel => Not found in database "+nameDataSet+" \n Cannot clean data");
				}
		}
		
			loadcharts(listh.getSelectedValue().toString(),listv.getSelectedValue().toString(),list4data.getSelectedValues(), "BOTH");
		
	}

	/*
	 * insidewindow
	 */
	private Boolean inside;
	private Boolean Insider(double[] values,double up,double down){
		inside=false;
		double value=0.7;
		double count=0;
		
		for(int i=0;i<values.length;i++){
			//System.out.println(values[i]+" "+(values[i]>down && values[i]<up));
			if( values[i]>down && values[i]<up){
				count++;
			}
		}
		
		System.out.println(values.length+" "+count+" "+(count/values.length)+" "+((count/values.length)>value));
		
		if((count/values.length)>value){
			inside=true;
		}
		
		return inside;
	}
	/*
	 * Reload dataset
	 */
	
	private TFSReader reloadtfs;
	private ArrayList<TFSReader> reloadarray;
	private ArrayList<TFSReader> newarray;
	
	private void reload(String namedataset,String file2load,String method,String plane){
		
		reloadtfs=new TFSReader();
		reloadtfs.loadTable(file2load);
		newarray= new ArrayList<TFSReader>();
		
		if(method.equals("SUSSIX") && plane.equals("H")){
			reloadarray=mapsussix.get(namedataset);
			newarray.add(reloadtfs);
			newarray.add(reloadarray.get(1));	
			mapsussix.put(namedataset, newarray);
		}else if(method.equals("SUSSIX") && plane.equals("V")){
			reloadarray=mapsussix.get(namedataset);
			newarray.add(reloadarray.get(0));
			newarray.add(reloadtfs);		
			mapsussix.put(namedataset, newarray);
		}else if(method.equals("SVD") && plane.equals("H")){
			reloadarray=mapsvd.get(namedataset);
			newarray.add(reloadtfs);
			newarray.add(reloadarray.get(1));
			newarray.add(reloadarray.get(2));
			newarray.add(reloadarray.get(3));
			newarray.add(reloadarray.get(4));
			newarray.add(reloadarray.get(5));
			newarray.add(reloadarray.get(6));
			newarray.add(reloadarray.get(7));
			newarray.add(reloadarray.get(8));
			newarray.add(reloadarray.get(9));
			newarray.add(reloadarray.get(10));
			newarray.add(reloadarray.get(11));	
			mapsvd.put(namedataset, newarray);
		}else if(method.equals("SVD") && plane.equals("V")){
			reloadarray=mapsvd.get(namedataset);
			newarray.add(reloadarray.get(0));
			newarray.add(reloadtfs);
			newarray.add(reloadarray.get(2));
			newarray.add(reloadarray.get(3));
			newarray.add(reloadarray.get(4));
			newarray.add(reloadarray.get(5));
			newarray.add(reloadarray.get(6));
			newarray.add(reloadarray.get(7));
			newarray.add(reloadarray.get(8));
			newarray.add(reloadarray.get(9));
			newarray.add(reloadarray.get(10));
			newarray.add(reloadarray.get(11));
			mapsvd.put(namedataset, newarray);			
		}
		
	}
	
	/*
	 * Graphical part
	 */
	private DefaultListModel model4data = new DefaultListModel();
	private JList list4data = new JList(model4data);
	private JScrollPane scroll4list = new JScrollPane(list4data);
	private DefaultListModel model4bpmh = new DefaultListModel();
	private JList listh = new JList(model4bpmh);
	private JScrollPane scroll4h = new JScrollPane(listh);
	private DefaultListModel model4bpmv = new DefaultListModel();
	private JList listv = new JList(model4bpmv);
	private JScrollPane scroll4v = new JScrollPane(listv);	
	private JLabel lafiles=new JLabel("Files :");
	private JLabel hfiles=new JLabel("Horizontal :");		
	private JLabel vfiles=new JLabel("Vertical :");		
	private JButton buttonclean = new JButton("Clean");
	private JButton butttondo = new JButton("Get Optics");	
	private Chart chartdata = new Chart();
	
	private Chart charthist = new Chart();
	private Chart chartsing = new Chart();
	private Chart chartdisp = new Chart();
	private JList listindx = new JList();
	private JScrollPane scrollindx = new JScrollPane(listindx);	
	private JCheckBox bov = new JCheckBox("V");
	private JCheckBox bou = new JCheckBox("U");	
	private JCheckBox bof = new JCheckBox("F");
	
	private CursorInteractor cursor1 = new CursorInteractor(false);
	private CursorInteractor cursor2 = new CursorInteractor(false);
	
	public void createGUI(){
		setLayout(new BorderLayout());
		//interior panel
		JPanel paneldata = new JPanel();
		JPanel panelextra = new JPanel();
		
		// main
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Data",paneldata );
		tabs.addTab("Extra",panelextra);
		
		// data panel
		list4data.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list4data.addMouseListener(new MouseAdapter() { 
			 public void mouseClicked(MouseEvent me) {
				 if(me.getButton()==MouseEvent.BUTTON3){
					 SaveTune();
				 }
			  }
	     });
		listh.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		paneldata.setLayout(new BorderLayout());
		chartdata.setRenderingType(ChartRenderer.SCATTER);	
		JPanel panelleft = new JPanel();
		panelleft.setBorder(new TitledBorder("Data"));
		chartdata.setBorder(new TitledBorder("Chart"));
		paneldata.add(panelleft, BorderLayout.WEST);
		paneldata.add(chartdata, BorderLayout.CENTER);

				
		// extra panel 
		charthist.setRenderingType(ChartRenderer.SCATTER);
		chartsing.setRenderingType(ChartRenderer.SCATTER);
		chartdisp.setRenderingType(ChartRenderer.SCATTER);
		GridBagLayout gridBag0 = new GridBagLayout();
		panelextra.setLayout(gridBag0);
		panelextra.setPreferredSize(new Dimension(800,800));
		gridBag0.rowWeights = new double[] {1,1,1,1,1,1,1};
		gridBag0.rowHeights = new int[] {260,260,50,20,20,20,50};
		gridBag0.columnWeights = new double[] {1,1,1};
		gridBag0.columnWidths = new int[] {150,150,300};
		gridBag0.setConstraints(charthist, gridconstrainer(5,10,10,10,2,1,0,0,panelextra,charthist));
		charthist.setBorder(new TitledBorder("Histogram"));
		gridBag0.setConstraints(chartsing, gridconstrainer(5,10,10,10,1,1,2,0,panelextra,chartsing));
		chartsing.setBorder(new TitledBorder("Singular values"));
		gridBag0.setConstraints(scrollindx, gridconstrainer(5,10,10,50,1,1,0,1,panelextra,scrollindx));
		gridBag0.setConstraints(bov, gridconstrainer(5,10,10,10,1,1,0,3,panelextra,bov));
		gridBag0.setConstraints(bou, gridconstrainer(5,10,10,10,1,1,0,4,panelextra,bou));
		gridBag0.setConstraints(bof, gridconstrainer(5,10,10,10,1,1,0,5,panelextra,bof));
		gridBag0.setConstraints(chartdisp, gridconstrainer(5,10,10,10,2,6,1,1,panelextra,chartdisp));		

		
		
		// connecting panelleft
		GridBagLayout gridBag1 = new GridBagLayout();
		panelleft.setLayout(gridBag1);
		panelleft.setPreferredSize(new Dimension(300,600));
		gridBag1.rowWeights = new double[] {1,1,1,1,1,1,1,1,1,1,1};
		gridBag1.rowHeights = new int[] {60,150,30,60,150,30,60,150,30,75,75,30};
		gridBag1.columnWeights = new double[] {1};
		gridBag1.columnWidths = new int[] {300};
		gridBag1.setConstraints(lafiles, gridconstrainer(5,10,0,10,1,1,0,0,panelleft,lafiles));
		gridBag1.setConstraints(scroll4list, gridconstrainer(5,10,10,10,1,1,0,1,panelleft,scroll4list));
		gridBag1.setConstraints(hfiles, gridconstrainer(5,10,0,10,1,1,0,3,panelleft,hfiles));
		gridBag1.setConstraints(scroll4h, gridconstrainer(5,10,10,10,1,1,0,4,panelleft,scroll4h));
		gridBag1.setConstraints(vfiles, gridconstrainer(5,10,0,10,1,1,0,6,panelleft,vfiles));
		gridBag1.setConstraints(scroll4v, gridconstrainer(5,10,10,10,1,1,0,7,panelleft,scroll4v));	
		gridBag1.setConstraints(buttonclean, gridconstrainer(5,10,5,10,1,1,0,9,panelleft,buttonclean));
		gridBag1.setConstraints(butttondo, gridconstrainer(5,10,10,10,1,1,0,10,panelleft,butttondo));
		
		//
		add(tabs,BorderLayout.CENTER);
		setVisible(true);
		

		
		
		//initial chart
		initialChart(charthist);initialChart(chartsing);initialChart(chartdisp);initialChart(chartdata);
		
		chartdata.setInteractors(ChartInteractor.createEditIteractors());
		
		chartdata.addInteractor(cursor1);//////!!!!
		chartdata.addInteractor(cursor2);
		
	}
	
	/*
	 * Save tune
	 */
	private void SaveTune() {
	    JFrame frame = new JFrame("Saving tune data");
	    MessageManager.getConsoleLogger().info("AnalysisPanel => Please specify name");
	    String name = JOptionPane.showInputDialog(frame, "Name for list:","tune");
	    if(name!=null){
	    	MessageManager.getConsoleLogger().info("AnalysisPanel => Saving data ");
	    	String savepath = controller.getBeamSelectionData().getOutputPath() + "/" + controller.getBeamSelectionData().getDate() + "/" + controller.getBeamSelectionData().getAccelerator() +"/Measurements/";
	    	MessageManager.getConsoleLogger().info("AnalysisPanel => Will save at the following location "+savepath);
	    	try {
	    	    BufferedWriter out = new BufferedWriter(new FileWriter(savepath+name));
	    	    out.write("* FILE \t TUNEX \t TUNEXe \t TUNEY \t TUNEYe\n");
	    	    out.write("$ %s %le %le %le %le\n");
	    	    System.out.println(listtunessussix.size());
	    	    for(int i=0;i<listtunessussix.size();i++){
	    	    	double[] data=maptunes.get(listtunessussix.get(i));
	    	    	out.write(listtunessussix.get(i)+" "+data[0]+" "+data[1]+" "+data[2]+" "+data[3]+"\n");
	    	    }
	    	    
	    	    if(listtunessvd.size()>0){
	    	    	out.write("##### SVD  #####\n");
	    	    	 for(int i=0;i<listtunessvd.size();i++){
	 	    	    	double[] data=maptunes.get(listtunessvd.get(i));
		    	    	out.write(listtunessvd.get(i)+" "+data[0]+" "+data[1]+" "+data[2]+" "+data[3]+"\n");	 	    	    	
	 	    	    }
	    	    }
	    	    
	    	    out.close();
	    	    MessageManager.info("AnalysisPanel => Data saved ",null);
	    	} catch (IOException e) {
	    		MessageManager.getConsoleLogger().warn("AnalysisPanel => Cannot save file, write access to location? \n Contact expert if problem continues ");
	    	}

	    }else{
	    	MessageManager.getConsoleLogger().warn("AnalysisPanel => *name error* Contact expert ");
	    }
	}
	
	
	/*
	 * set location cursor
	 */
	private double mean;
	
	private void setlocation(double[] values,String name){
		
		if(name.contains("TUNE")){
			cursor1.setEnabled(true);
			cursor2.setEnabled(true);
		
			
			mean = MathTools.arithmeticMean(values);
			
			cursor1.setValue(mean+mean*adjustment);
			cursor2.setValue(mean-mean*adjustment);
			
		}else{
			cursor1.setEnabled(false);
			cursor2.setEnabled(false);
		}

		
	}
	
	private double[] xxx={0};
		
	private void initialChart(Chart chart){
		
		chart.setInteractors(ChartInteractor.createEditIteractors());
		chart.setDataSet(new DefaultDataSet("Data Viewer",xxx,xxx)); 	
	}
	
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
	
	private DefaultDataSource dsmain;
	private ArrayList<TFSReader> list4readers;
	private HashMap<String,double[]> maptunes;
	private ArrayList<String> listtunessussix;
	private ArrayList<String> listtunessvd;
	
	private void loadcharts(final String selnamex,final String selnamey,final Object[] selfiles, String listchange){
		dsmain = new DefaultDataSource();
		maptunes = new HashMap<String, double[]>(); // double[rmsx,stdx,rmsy,stdy]
		listtunessussix= new ArrayList<String>();
		listtunessvd= new ArrayList<String>();
		
		emptyDS(dsmain);
		
	
		for(int i=0;i<selfiles.length;i++){
			
					
			String sinfile=selfiles[i].toString();
			
			
			if(sinfile.contains("SUSSIX")){
				list4readers=mapsussix.get(sinfile);
				double[] avex=new double[2];
				double[] avey=new double[2];
				
				if(!selnamex.equals("None")){
					double[] datax=list4readers.get(0).getDoubleData("S");
					double[] datay=list4readers.get(0).getDoubleData(selnamex);
					if(selnamex.equals("TUNEX")){
						avex=DoAveraging(datay,sinfile+"_X");
					}
					dsmain.addDataSet(new DefaultDataSet(sinfile+"_X",datax,datay));
				}
				
				if(!selnamey.equals("None")){
					double[] datax=list4readers.get(1).getDoubleData("S");
					double[] datay=list4readers.get(1).getDoubleData(selnamey);
					if(selnamey.equals("TUNEY")){
						avey=DoAveraging(datay,sinfile+"_Y");
					}
					dsmain.addDataSet(new DefaultDataSet(sinfile+"_Y",datax,datay));
				}
				
				if(selnamex.equals("TUNEX") && selnamey.equals("TUNEY")){
					maptunes.put(sinfile, new double[]{avex[0],avex[1],avey[0],avey[1]});
					listtunessussix.add(sinfile);
				}
				
			}else{
				list4readers=mapsvd.get(sinfile);
				double[] avex=new double[2];
				double[] avey=new double[2];
				
				if(!selnamex.equals("None")){
					double[] datax=list4readers.get(0).getDoubleData("S");
					double[] datay=list4readers.get(0).getDoubleData(selnamex);
					DoAveraging(datay,sinfile+"_X");
					if(selnamex.equals("TUNEX")){
						avex=DoAveraging(datay,sinfile+"_X");
					}
					dsmain.addDataSet(new DefaultDataSet(sinfile+"_X",datax,datay));
				}
				if(!selnamey.equals("None")){
					double[] datax=list4readers.get(1).getDoubleData("S");
					double[] datay=list4readers.get(1).getDoubleData(selnamey);
					DoAveraging(datay,sinfile+"_Y");
					if(selnamey.equals("TUNEY")){
						avey=DoAveraging(datay,sinfile+"_Y");
					}
					dsmain.addDataSet(new DefaultDataSet(sinfile+"_Y",datax,datay));
				}
				
				if(selnamex.equals("TUNEX") && selnamey.equals("TUNEY")){
					maptunes.put(sinfile, new double[]{avex[0],avex[1],avey[0],avey[1]});
					listtunessvd.add(sinfile);
				}
				
			}
		            
		}
		
		cursor1.setValue(dsmain.getYRange().getMax()*0.7);
		cursor2.setValue(dsmain.getYRange().getMax()*1.2);
		
		chartdata.setDataSource(dsmain);
		chartdata.setLegendVisible(true);
		
		if(listchange.equals("VER")){
			setlocation(list4readers.get(0).getDoubleData(selnamey),selnamey);
		}else{
			setlocation(list4readers.get(1).getDoubleData(selnamex),selnamex);
		}
		
	}
	
	/*
	 * Averaging 
	 */
	private double[] DoAveraging(double[] data,String file){
		double rms = MathTools.rootMeanSquare(data);
		double std = MathTools.standardDeviation(data);
        MessageManager.getConsoleLogger().info("BPMpanel => File: " + file + "\nrms " + rms + "\nstd " + std);
		return new double[]{rms, std};
	}
	
	/*
	 * empty ds
	 */
	
	private void emptyDS(DefaultDataSource ds){
		
		for(int i=0;i<ds.getDataSetsCount();i++){
			ds.removeDataSet(i);
		}
	}
}
