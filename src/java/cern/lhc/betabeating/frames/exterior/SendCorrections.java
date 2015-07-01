package cern.lhc.betabeating.frames.exterior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


import cern.accsoft.gui.frame.MessageManager;
import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.ChartRenderer;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.interactor.DataPickerInteractor;
import cern.lhc.betabeating.remote.services;
import cern.lsa.optics.domain.OpticsTableItem;
import cern.lsa.settings.domain.StandAloneBeamProcess;

public class SendCorrections extends JFrame{
    private static final long serialVersionUID = 3397858388852740202L;
    /**
	 * @param args
	 */
	private ArrayList<StandAloneBeamProcess> bps;
	private OpticsTableItem[] opticnames;		
	private ArrayList<String> knobnames;
	private Object[] selvalues;
	private Object[] cyvalues;
	private services ser;
	private HashMap<String,Double> mapknobs;
	private double[] knobvalues;
	private String[] knobstring;
	private double[] knobcount;
	
	public SendCorrections(final services sertmp,final String method){
		super("Knob panel");
		ser=sertmp;
		
		//fill bps
		fillbp();
		
		//fill knobs
		fillknobs();
		
		CreateGUI();
		
		
		//get optics
		listbp.addListSelectionListener(new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		    	  
		    	  if(evt.getValueIsAdjusting()){
			    	  listmodelop.removeAllElements();
			    	  cyvalues=listbp.getSelectedValues();
			    	  for(int i=0;i<cyvalues.length;i++){
			    		  opticnames=ser.getOptic(cyvalues[i].toString());
				    	  for(int j=0;j<opticnames.length;j++){		  			
				  				listmodelop.addElement(opticnames[j].getOpticName());
				    	  }
			    	  }
		    	  }
		}});
		
		//cancel
		buttoncancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			setVisible(false);
			dispose();
		}});
		
		buttoncancelt.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			setVisible(false);
			dispose();
		}});
		
		//create knob
		buttoncreate.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			
			if(!names[0].equals("empty")){
				MessageManager.getConsoleLogger().info("Services => Creating knob");
				ser.createKnob(listbp.getSelectedValue().toString(),fieldname.getText(),listop.getSelectedValues(),names,data,method);
			}else{
				MessageManager.getConsoleLogger().warn("Services =>  No data file selected " +
						"\n please select file in corrections window");
			}
			
		}});
		
		//delete knob
		buttonremove.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			selvalues=listknob.getSelectedValues();
			 for(int i=0;i<selvalues.length;i++){	
				 ser.removeknob(ser.prefix+selvalues[i]);		 
			 }
			 listmodelknob.removeAllElements();
			 fillknobs();
		}});
		
		//refresh
		buttonrefresh.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			 listmodelknob.removeAllElements();
			 fillknobs();
			 fillbp();
		}});
		
		///knob settings
		listknob.addListSelectionListener(new ListSelectionListener() {public void valueChanged(ListSelectionEvent evt) {
			
			
			if(evt.getValueIsAdjusting()){
				mapknobs=ser.viewknob(listknob.getSelectedValue().toString());
				emptytable();
				knobvalues=new double[mapknobs.size()];
				knobstring=new String[mapknobs.size()];
				knobcount=new double[mapknobs.size()];
				int count=0;
				
				Iterator<Entry<String, Double>> it = mapknobs.entrySet().iterator();
				    while (it.hasNext()) {
				        Entry<String, Double> pairs = it.next();
				        defaulttableknob.addRow(new Object[]{count+1,pairs.getKey(),pairs.getValue()});
				        knobstring[count]=pairs.getKey();
				        knobvalues[count]= pairs.getValue();
				        knobcount[count]=count+1;
				        chart.setDataSet(new DefaultDataSet("Knobs",knobcount,knobvalues));
				        count++;
				 }
			}

		}});
			
	}
	
	/*
	 * knob
	 */
	private String[] names=new String[]{"empty"};
	private double[] data;
	public void setvalues(String[] namestemp,double[] datatemp){
		names=namestemp;
		data=datatemp;
	}
	
	/*
	 * empty table
	 */
	private void emptytable(){
		for(int i=defaulttableknob.getRowCount()-1;i>=0;i--){
			defaulttableknob.removeRow(i);
		}
		
	}
	
	/*
	 * fill knobs
	 */
	private void fillknobs(){
		knobnames=ser.getknobs();
		for(int i=0;i<knobnames.size();i++){
			listmodelknob.addElement(knobnames.get(i).replace(ser.prefix,""));
		}
	}
	
	/*
	 * fill bp
	 */
	private void fillbp(){
		bps=ser.getbeamprocesses();
		listmodelbp.removeAllElements();
		listmodelbpt.removeAllElements();
		
		for(int i=0;i<bps.size();i++){
			//if(bps.get(i).isResident()){
				listmodelbp.addElement(bps.get(i).getName());
				listmodelbpt.addElement(bps.get(i).getName());
			//}
		}
		listmodelop.removeAllElements();
	}
	
	/**
	 * GUI
	 */
	
	private JPanel panelgeneration= new JPanel();
	private JPanel paneltrim = new JPanel();
	private JPanel panelknob = new JPanel();	
	private JPanel panelfollow = new JPanel();
	
	private JTabbedPane tabs = new JTabbedPane();
	
	//labelgeneration
	private GridBagLayout gridbag0 = new GridBagLayout();
	private DefaultListModel listmodelbp = new DefaultListModel();
	private JList listbp = new JList(listmodelbp);
	private JScrollPane scrollbp = new JScrollPane(listbp);
	private DefaultListModel listmodelop = new DefaultListModel();
	private JList listop = new JList(listmodelop);
	private JScrollPane scrollop = new JScrollPane(listop);
	private JLabel labelbp = new JLabel("Beam Process:");
	private JLabel labelop = new JLabel("Optics:");
	private JLabel labelname = new JLabel("Knob name:");
	private JTextField fieldname = new JTextField();
	private JButton buttoncreate = new JButton("Create knob");
	private JButton buttoncancel = new JButton("Cancel");
	
	//labeltrim
	private GridBagLayout gridbag1 = new GridBagLayout();
	private JLabel labelbpt =  new JLabel("Beam process :");
	private JLabel lbaleknob = new JLabel("Knobs :");
	
	private DefaultListModel listmodelbpt = new DefaultListModel();
	private JList listbpt = new JList(listmodelbpt);
	private JScrollPane scrollbpt = new JScrollPane(listbpt);
	
	private DefaultListModel listmodelknob = new DefaultListModel();
	private JList listknob = new JList(listmodelknob);
	private JScrollPane scrollknob = new JScrollPane(listknob);	
	
	private JLabel labelknob = new JLabel("Knob settings:");
	private DefaultTableModel defaulttable = new DefaultTableModel();
	private JTable tabletrim = new JTable(defaulttable); 
	private JScrollPane scrolltable = new JScrollPane(tabletrim);	
	
	private JButton buttontrim = new JButton("Trim");
	private JButton buttoncancelt = new JButton("Cancel");	
	private JButton buttonrefresh = new JButton("Refresh");	
	private JButton buttonremove = new JButton("Remove knob");	
	private double[] xxx ={0};
	
	//knob
	private DefaultTableModel defaulttableknob = new DefaultTableModel();
	private JTable tabletrimknob = new JTable(defaulttableknob); 
	private JScrollPane scrolltableknob = new JScrollPane(tabletrimknob);
	private JTabbedPane tabsknob = new JTabbedPane();
	private Chart chart = new Chart();
	private DataPickerInteractor dataPicker = new DataPickerInteractor();

	
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
		//tabs
		tabs.addTab("Generation", panelgeneration);
		tabs.addTab("Trim", paneltrim);
		tabs.addTab("Knob settings", panelknob);		
		tabs.addTab("PC", panelfollow);
		
		//generation	
		panelgeneration.setLayout(gridbag0);
		buttoncreate.setBackground(Color.green);
		buttoncancel.setBackground(Color.red);
		gridbag0.rowWeights = new double[] {1,1,1,1};
		gridbag0.rowHeights = new int[] {20,250,50,50};
//		gridbag0.rowWeights = new double[] {1, 1, 1, 1, 1};
//		gridbag0.rowHeights = new int[] {20, 250, 40, 40, 50};
		gridbag0.columnWeights = new double[] {1,1};
		gridbag0.columnWidths = new int[] {300,300};
		gridbag0.setConstraints(labelbp, gridconstrainer(0,5,0,10,1,1,0,0,panelgeneration,labelbp));
		gridbag0.setConstraints(labelop, gridconstrainer(0,5,0,10,1,1,1,0,panelgeneration,labelop));
		gridbag0.setConstraints(scrollbp, gridconstrainer(0,5,0,10,1,1,0,1,panelgeneration,scrollbp));
		gridbag0.setConstraints(scrollop, gridconstrainer(0,5,0,10,1,1,1,1,panelgeneration,scrollop));	
		gridbag0.setConstraints(labelname, gridconstrainer(20,5,20,10,1,1,0,2,panelgeneration,labelname));
		gridbag0.setConstraints(fieldname, gridconstrainer(20,5,20,10,1,1,1,2,panelgeneration,fieldname));
		gridbag0.setConstraints(buttoncreate, gridconstrainer(5,20,10,20,1,1,0,3,panelgeneration,buttoncreate));
		gridbag0.setConstraints(buttoncancel, gridconstrainer(5,20,10,20,1,1,1,3,panelgeneration,buttoncancel));	
//		gridbag0.setConstraints(new JLabel("Filter:"), gridconstrainer(20,5,20,10,1,1,0,2,panelgeneration,labelname));
//		gridbag0.setConstraints(fieldname, gridconstrainer(20,5,20,10,1,1,1,2,panelgeneration,fieldname));
//		gridbag0.setConstraints(labelname, gridconstrainer(20,5,20,10,1,1,0,3,panelgeneration,labelname));
//		gridbag0.setConstraints(fieldname, gridconstrainer(20,5,20,10,1,1,1,3,panelgeneration,fieldname));
//		gridbag0.setConstraints(buttoncreate, gridconstrainer(5,20,10,20,1,1,0,4,panelgeneration,buttoncreate));
//		gridbag0.setConstraints(buttoncancel, gridconstrainer(5,20,10,20,1,1,1,4,panelgeneration,buttoncancel));	
		
		//trim
		buttontrim.setBackground(Color.green);
		buttoncancelt.setBackground(Color.red);
		buttonremove.setBackground(Color.orange);
		defaulttable.addColumn("Knob");
		defaulttable.addColumn("Previous");
		defaulttable.addColumn("New");
		defaulttable.addRow(new Object[]{"Empty","0","0"});
		paneltrim.setLayout(gridbag1);
		gridbag1.rowWeights = new double[] {1,1,1,1,1,1};
		gridbag1.rowHeights = new int[] {20,200,50,50,50,50};
		gridbag1.columnWeights = new double[] {1,1};
		gridbag1.columnWidths = new int[] {300,300};
		gridbag1.setConstraints(labelbpt, gridconstrainer(0,5,0,10,1,1,0,0,paneltrim,labelbpt));
		gridbag1.setConstraints(lbaleknob, gridconstrainer(0,5,0,10,1,1,1,0,paneltrim,lbaleknob));
		gridbag1.setConstraints(scrollbpt, gridconstrainer(0,5,0,10,1,1,0,1,paneltrim,scrollbpt));
		gridbag1.setConstraints(scrollknob, gridconstrainer(0,5,0,10,1,1,1,1,paneltrim,scrollknob));	
		gridbag1.setConstraints(labelknob, gridconstrainer(20,5,0,10,1,1,0,2,paneltrim,labelknob));
		gridbag1.setConstraints(scrolltable, gridconstrainer(0,5,20,10,2,1,0,3,paneltrim,scrolltable));	
		gridbag1.setConstraints(buttontrim, gridconstrainer(5,5,10,5,1,1,0,4,paneltrim,buttontrim));
		gridbag1.setConstraints(buttonremove, gridconstrainer(5,5,10,5,1,1,1,4,paneltrim,buttonremove));	
		gridbag1.setConstraints(buttoncancelt, gridconstrainer(5,50,5,50,1,1,0,5,paneltrim,buttoncancelt));
		gridbag1.setConstraints(buttonrefresh, gridconstrainer(5,50,5,50,1,1,1,5,paneltrim,buttonrefresh));		
		
		//knob
		chart.setRenderingType(ChartRenderer.BAR);
		tabsknob.addTab("Table",scrolltableknob);
		tabsknob.addTab("Chart",chart);
		panelknob.setLayout(new BorderLayout());
		panelknob.add(tabsknob);
		defaulttableknob.addColumn("#");
		defaulttableknob.addColumn("Component name");
		defaulttableknob.addColumn("Value");
		chart.setInteractors(ChartInteractor.createEditIteractors());
		chart.setDataSet(new DefaultDataSet("Data Viewer",xxx,xxx)); 	
		
		add(tabs);
		setVisible(true);
		setSize(650, 500);
//		setSize(new Dimension(600,500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	

	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		new SendCorrections(new services("LHC"), "beta");
//	}
}
