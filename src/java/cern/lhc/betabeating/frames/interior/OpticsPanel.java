package cern.lhc.betabeating.frames.interior;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.log4j.Logger;

import cern.jdve.Chart;
import cern.jdve.ChartInteractor;
import cern.jdve.Style;
import cern.jdve.data.DataSet;
import cern.jdve.data.DataSource;
import cern.jdve.data.DefaultDataSet;
import cern.jdve.data.DefaultDataSource;
import cern.jdve.data.DefaultErrorDataSet;
import cern.jdve.event.ChartInteractionEvent;
import cern.jdve.event.ChartInteractionListener;
import cern.jdve.interactor.DataPickerInteractor;
import cern.jdve.renderer.ErrorDataSetRenderer;
import cern.jdve.utils.DataWindow;
import cern.jdve.utils.DisplayPoint;
import cern.lhc.betabeating.datahandler.TFSReader;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.exterior.CorrectionSelection;

public class OpticsPanel extends JPanel {
    private static final long serialVersionUID = 2214260970949402343L;
    private static final Logger log = Logger.getLogger(OpticsPanel.class);
    
    private Controller controller = null;
    private SbsPanel sbspanel= null;
    public OpticsPanel(Controller controller){
        this.controller = controller;
        this.sbspanel = new SbsPanel(controller);
		CreateGUI();
		filltree();
		fillchildren();
		placeChildren();
		listeners();
	}
	
	/*
	 * load files
	 */
	private HashMap<String,String> translatemap = new HashMap<String, String>();
	public void addfile(String file,String method){
		loaddata(file+"/",method);
		listmodel.addElement(new File(file).getName()+"_"+method);
		listfiles.removeListSelectionListener(listselection);
		listfiles.setSelectedValue(new File(file).getName()+"_"+method, true);
		listfiles.addListSelectionListener(listselection);
		translatemap.put(new File(file).getName()+"_"+method,file+"/");
		sbspanel.setenv(env);
		sbspanel.setFilename(file+"/");
	}
	
	/*
	 * Setting env and paths
	 */
	private String[] env;
	public void setenv(String[] envin){
		env=envin;
	}
	
	/*
	 * listeners
	 */
	private DefaultMutableTreeNode node;
	private TreeSelectionListener treelistener;
	private CorrectionSelection csel;
	private String selfile1;
	private String selfile2;
	private CorrectionPanel corpanel;
	
	public void setcorsel(CorrectionPanel corpanelt){
		corpanel=corpanelt;
	}
	
	private  void listeners(){
		///tab
		tabs.addChangeListener(new ChangeListener() { // This method is called whenever the selected tab changes 
			public void stateChanged(ChangeEvent evt) { 
			filltree();
			placeChildren();
		}});
		
		///tree listener
		tree.addTreeSelectionListener(treelistener=new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();        
		        if(node.isLeaf()){
		        	plot(node);
		        }else{
			        for(int i=0;i<root.getChildCount();i++){
			        	tree.collapseRow(i);
			        }
			        tree.expandRow(tree.getSelectionRows()[0]);
		        }
		        
		        
		    }});   
		
		buttonsbsgo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			sbspanel.systemcallforsegment(filedbpm1.getText(), filedbpm2.getText(), filedseg.getText(),translatemap.get(listfiles.getSelectedValue().toString())+"/");
			framsbs.setVisible(false);
			framsbs.dispose();
		}});
		
		buttonsbscancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			framsbs.setVisible(false);
			framsbs.dispose();
		}});
		
		listfiles.addListSelectionListener(listselection=new ListSelectionListener() {public void valueChanged(ListSelectionEvent evt) {
			if(evt.getValueIsAdjusting()){
				emptycharts();
				loaddata(translatemap.get(listfiles.getSelectedValue().toString())+"/","none");
				filltree();
				placeChildren();
				sbspanel.setFilename(translatemap.get(listfiles.getSelectedValue().toString()));
			}
		}});
		
		corbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			
			if(listfiles.getSelectedIndices().length>1){
				selfile2=listfiles.getSelectedValues()[1].toString();
			}else{
				selfile2="None";
			}
			
			selfile1=listfiles.getSelectedValues()[0].toString();
			
			csel = new CorrectionSelection(controller, translatemap.get(selfile1),translatemap.get(selfile2),corpanel);
			csel.setenv(env);
		}});
	}
	
	
	/**
	 *  data handling
	 */
	/*
	 * plot data
	 */
	private String parentname;
	private String childname;
	
	private void plot(DefaultMutableTreeNode node){
		parentname=node.getParent().toString();
		childname=node.toString();
		
		switch(index){
			case 0:
				filldsource(parentname,childname,chartlist.get(0),chartlist.get(1));
				break;
			case 1:
				filldsource(parentname,childname,chartlist.get(2),chartlist.get(3));
				break;
			case 2:
				filldsource(parentname,childname,chartlist.get(4),chartlist.get(5));
				break;
			case 3:
				
				
			case 4:
		}
//        System.out.println("chart2y.getArea().getSize():" + chart2y.getArea().getSize()); //chart2y.getArea().getSize():java.awt.Dimension[width=1173,height=34 //TODO inspect (tbach)
		log.info(parentname+" "+childname);
	}
	
	/*
	 * internal plot
	 */
	private String selfilename;
	private HashMap<String,TFSReader> maptemp;
	
	private double[] slocx;
	private double[] slocy;	
	private double[] yvaluex1;
	private double[] yvaluex2;
	private double[] yvaluey1;
	private double[] yvaluey2;
	private double[] diffx;
	private double[] diffy;	
	private double[] errorx;
	private double[] errory;
	private double[] zerox;
	private double[] zeroy;
	private String xkey;
	private String ykey;
	
	private ErrorDataSetRenderer errorRendererx;
	private DefaultDataSource dsourcex;	
	private ErrorDataSetRenderer errorRenderery;
	private DefaultDataSource dsourcey;		
	private DataSource jj;
	private ArrayList<ErrorDataSetRenderer> errorlist;
	
	private HashMap<String,String[]> bpmnames= new HashMap<String, String[]>();
	
	private void setRange(DefaultDataSource ds, Chart chart,double scale){
		
		DataWindow dw;
		if(scale==-100){
			dw = new DataWindow(ds.getXRange().getMin(),ds.getXRange().getMax(),-0.1,0.5);
		}else{
			dw = new DataWindow(ds.getXRange().getMin(),ds.getXRange().getMax(),ds.getYRange().getMin()*scale,ds.getYRange().getMax()*scale);
		}
		chart.zoom(dw);
	}
	
	private void filldsource(String parent,String child,Chart chart1,Chart chart2){
		
		chart1.removeAllRenderers();
		chart2.removeAllRenderers();
		errorlist=new ArrayList<ErrorDataSetRenderer>();
		
		dsourcex=new DefaultDataSource();
		errorRendererx=new ErrorDataSetRenderer();
		errorRendererx.setDrawBars(false);
		errorRendererx.setDrawPolyLine(true);
		errorRendererx.setStyle(0, new Style(Color.RED, new Color(255, 0, 0)));
		chart1.addRenderer(errorRendererx);
		errorlist.add(errorRendererx);

		
		dsourcey=new DefaultDataSource();
		errorRenderery=new ErrorDataSetRenderer();
		errorRenderery.setDrawBars(false);
		errorRenderery.setDrawPolyLine(true);
		errorRenderery.setStyle(0, new Style(Color.red, new Color(255, 0, 0)));
		chart2.addRenderer(errorRenderery);
		errorlist.add(errorRenderery);
		
		for(int i=0;i<listfiles.getSelectedValues().length;i++){
			selfilename=listfiles.getSelectedValues()[i].toString();
				
				maptemp=datamap.get(selfilename);
			
				/*
				 * For phase
				 */
				if(parent.contains("Phase")){ 
						// for phases
						if(parent.contains("total")){
							xkey="phasetotx";
							ykey="phasetoty";
						}else{
							xkey="phasex";
							ykey="phasey";
						}
						
						/*if(parent.contains("free")){
							xkey=xkey+"_free";
							ykey=ykey+"_free";
						}*/
						maptemp.get(xkey);
						//x
						bpmnames.put("Phasex",maptemp.get(xkey).getStringData("NAME"));
						slocx=maptemp.get(xkey).getDoubleData("S");
						yvaluex1=maptemp.get(xkey).getDoubleData("PHASEX");
						yvaluex2=maptemp.get(xkey).getDoubleData("PHXMDL");
						errorx=maptemp.get(xkey).getDoubleData("STDPHX");
						
						diffx=new double[yvaluex1.length];
						zerox=new double[yvaluex1.length];
						
						for(int j=0;j<yvaluex1.length;j++){
							diffx[j]=(yvaluex1[j]-yvaluex2[j]);
							zerox[j]=0;
						}
									
						//y
						bpmnames.put("Phasey",maptemp.get(ykey).getStringData("NAME"));
						slocy=maptemp.get(ykey).getDoubleData("S");
						yvaluey1=maptemp.get(ykey).getDoubleData("PHASEY");
						yvaluey2=maptemp.get(ykey).getDoubleData("PHYMDL");
						errory=maptemp.get(ykey).getDoubleData("STDPHY");
						diffy=new double[yvaluey1.length];
						zeroy=new double[yvaluey1.length];
						
						for(int j=0;j<yvaluey1.length;j++){
							diffy[j]=(yvaluey1[j]-yvaluey2[j]);
							zeroy[j]=0;
						}	
								
						//plotting
						if(child.equals("Phase-PhMdl")){
							//x
							dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));
							dsourcex.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocx,yvaluex2,zerox));
							chart1.setYScaleTitle("\u03c6x [2*\u03c0]");
							chart1.setXScaleTitle("Location [m]");
							chart1.setLegendVisible(true);
							setRange(dsourcex,chart1,-100);
							
							//y
							dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));
							dsourcey.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocy,yvaluey2,zeroy));
							chart2.setYScaleTitle("\u03c6y [2*\u03c0]");
							chart2.setXScaleTitle("Location [m]");
							chart2.setLegendVisible(true);
							setRange(dsourcey,chart2,-100);
						
						}else if(child.equals("Diff(Phase-PhMdl)")){
							//x					
							dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,diffx,errorx));					
							chart1.setYScaleTitle("\u0394 \u03c6x [2**\u03c0]");
							chart1.setXScaleTitle("Location [m]");
							chart1.setLegendVisible(true);
							setRange(dsourcex,chart1,-100);
							//y					
							dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,diffy,errory));					
							chart2.setYScaleTitle("\u0394 \u03c6x [2*\u03c0]");
							chart2.setXScaleTitle("Location [m]");
							chart2.setLegendVisible(true);
							setRange(dsourcey,chart2,-100);
							
						}else if(child.equals("Error")){
							//x
							dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));					
							chart1.setYScaleTitle("\u03c6x [2**\u03c0]");
							chart1.setXScaleTitle("Location [m]");
							chart1.setLegendVisible(true);					
							setRange(dsourcex,chart1,0.4);
							//y
							dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));					
							chart2.setYScaleTitle("\u03c6x [2*\u03c0]");
							chart2.setXScaleTitle("Location [m]");
							chart2.setLegendVisible(true);							
							setRange(dsourcey,chart2,0.4);
							
						}
				 /*
				  * for betaaaa
				  */
				}else if(parent.contains("Beta") ){
					xkey="betax";
					ykey="betay";			
					//beta
					/*if(parent.contains("free")){
						xkey=xkey+"_free";
						ykey=ykey+"_free";
					}*/
					if(parent.contains("amp")){
						xkey="amp"+xkey;
						ykey="amp"+ykey;
					}
					
					//error handling
					if(parent.contains("amp")){
						errorx=maptemp.get(xkey).getDoubleData("BETXSTD");
						errory=maptemp.get(ykey).getDoubleData("BETYSTD");
					}else{
						yvaluex1=maptemp.get(xkey).getDoubleData("STDBETX");
						yvaluex2=maptemp.get(xkey).getDoubleData("ERRBETX");
						yvaluey1=maptemp.get(ykey).getDoubleData("STDBETY");
						yvaluey2=maptemp.get(ykey).getDoubleData("ERRBETY");
						
						errorx=new  double[yvaluex1.length];
						for(int k=0;k<yvaluex1.length;k++){
							errorx[k]=Math.sqrt(yvaluex1[k]*yvaluex1[k]+yvaluex2[k]*yvaluex2[k]);
						}
						errory=new  double[yvaluey1.length];
						for(int k=0;k<yvaluey1.length;k++){
							errory[k]=Math.sqrt(yvaluey1[k]*yvaluey1[k]+yvaluey2[k]*yvaluey2[k]);
						}				
					}
						
					
					//x
					bpmnames.put("Betax",maptemp.get(xkey).getStringData("NAME"));
					slocx=maptemp.get(xkey).getDoubleData("S");
					yvaluex1=maptemp.get(xkey).getDoubleData("BETX");
					yvaluex2=maptemp.get(xkey).getDoubleData("BETXMDL");
					diffx=new double[yvaluex1.length];
					zerox=new double[yvaluex1.length];
			
					for(int j=0;j<yvaluex1.length;j++){
						diffx[j]=(yvaluex1[j]-yvaluex2[j])/yvaluex2[j];
						errorx[j]=errorx[j]/yvaluex1[j];
						zerox[j]=0;
					}
					
					//y
					bpmnames.put("Betay",maptemp.get(ykey).getStringData("NAME"));
					slocy=maptemp.get(ykey).getDoubleData("S");
					yvaluey1=maptemp.get(ykey).getDoubleData("BETY");
					yvaluey2=maptemp.get(ykey).getDoubleData("BETYMDL");
					diffy=new double[yvaluey1.length];
					zeroy=new double[yvaluey1.length];
					
					for(int j=0;j<yvaluey1.length;j++){
						diffy[j]=(yvaluey1[j]-yvaluey2[j])/yvaluey2[j];
						errory[j]=errory[j]/yvaluey1[j];
						zeroy[j]=0;
					}
		
		
					if(child.equals("Beta-beat")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,diffx,errorx));
						chart1.setYScaleTitle("(\u0394\u03b2x)\\\u03b2x");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.1);
						
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,diffy,errory));
						chart2.setYScaleTitle("(\u0394\u03b2y)\\\u03b2y");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.1);
		
											
					}else if(child.equals("Beta-BMdl")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));
						dsourcex.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocx,yvaluex2,zerox));
						chart1.setYScaleTitle("\u03b2x  [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.4);
						
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));
						dsourcey.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocy,yvaluey2,zeroy));
						chart2.setYScaleTitle("(\u03b2y [m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.4);
						
					}else if(child.equals("Error")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));
						chart1.setYScaleTitle("\u03b2x");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.4);
						
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));
						chart2.setYScaleTitle("\u03b2y");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.4);
					}
				/*
				 * For coupling
				 */
				}else if(parent.contains("Coupling")){
				
					log.info("Going trough coupling");
					
					//couple
					xkey="couple";
					/*if(parent.contains("free")){
						xkey=xkey+"_free";			
					}*/
					
					//general
					bpmnames.put("Coupling",maptemp.get(xkey).getStringData("NAME"));
					
					if(child.equals("amp")){
						//x
						slocx=maptemp.get(xkey).getDoubleData("S");
						yvaluex1=maptemp.get(xkey).getDoubleData("F1001W");
						errorx=maptemp.get(xkey).getDoubleData("FWSTD1");
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));					
						chart1.setYScaleTitle("abs(F1001)");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.4);
		
						//y
						slocy=maptemp.get(xkey).getDoubleData("S");
						yvaluey1=maptemp.get(xkey).getDoubleData("F1010W");
						errory=maptemp.get(xkey).getDoubleData("FWSTD2");
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));					
						chart2.setYScaleTitle("abs(F1010)");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.4);
		
					}else if(child.equals("real")){
						//x
						slocx=maptemp.get(xkey).getDoubleData("S");
						yvaluex1=maptemp.get(xkey).getDoubleData("F1001R");
						errorx=maptemp.get(xkey).getDoubleData("FWSTD1");
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));					
						chart1.setYScaleTitle("re(F1001)");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.4);
		
						//y
						slocy=maptemp.get(xkey).getDoubleData("S");
						yvaluey1=maptemp.get(xkey).getDoubleData("F1010R");
						errory=maptemp.get(xkey).getDoubleData("FWSTD2");
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));					
						chart2.setYScaleTitle("re(F1010)");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.4);
		
					}else if(child.equals("imaginary")){
						//x
						slocx=maptemp.get(xkey).getDoubleData("S");
						yvaluex1=maptemp.get(xkey).getDoubleData("F1001I");
						errorx=maptemp.get(xkey).getDoubleData("FWSTD1");
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));					
						chart1.setYScaleTitle("im(F1001)");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.4);
		
						//y
						slocy=maptemp.get(xkey).getDoubleData("S");
						yvaluey1=maptemp.get(xkey).getDoubleData("F1010I");
						errory=maptemp.get(xkey).getDoubleData("FWSTD2");
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));					
						chart2.setYScaleTitle("im(F1010)");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.4);
		
					}else if(child.equals("error")){
						//x
						slocx=maptemp.get(xkey).getDoubleData("S");
						errorx=maptemp.get(xkey).getDoubleData("FWSTD1");
						zerox=new double[errorx.length];
						for(int j=0;j<errorx.length;j++){
							zerox[j]=0;
						}
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));					
						chart1.setYScaleTitle("F1001e");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						slocy=maptemp.get(xkey).getDoubleData("S");
						errory=maptemp.get(xkey).getDoubleData("FWSTD2");
						zeroy=new double[errory.length];
						for(int j=0;j<errory.length;j++){
							zeroy[j]=0;
						}
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));					
						chart2.setYScaleTitle("F1010e)");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);
						
					}
				}else if(parent.equals("Dispersion")){
					
					xkey="dx";
					ykey="dy";
					
					//x
					bpmnames.put("Dispersionx",maptemp.get(xkey).getStringData("NAME"));
					slocx=maptemp.get(xkey).getDoubleData("S");
					yvaluex1=maptemp.get(xkey).getDoubleData("DX");
					yvaluex2=maptemp.get(xkey).getDoubleData("DXMDL");
					errorx=maptemp.get(xkey).getDoubleData("STDDX");
					diffx=new double[yvaluex1.length];
					zerox=new double[yvaluex1.length];
			
					for(int j=0;j<yvaluex1.length;j++){
						diffx[j]=(yvaluex1[j]-yvaluex2[j]);
						zerox[j]=0;
					}
					
					//y
					bpmnames.put("Dispersiony",maptemp.get(ykey).getStringData("NAME"));
					slocy=maptemp.get(ykey).getDoubleData("S");
					yvaluey1=maptemp.get(ykey).getDoubleData("DY");
					yvaluey2=maptemp.get(ykey).getDoubleData("DYMDL");
					errory=maptemp.get(ykey).getDoubleData("STDDY");			
					diffy=new double[yvaluey1.length];
					zeroy=new double[yvaluey1.length];
					
					for(int j=0;j<yvaluey1.length;j++){
						diffy[j]=(yvaluey1[j]-yvaluey2[j]);
						zeroy[j]=0;
					}
					if(child.equals("Disp-DMdl")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));	
						dsourcex.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocx,yvaluex2,zerox));
						chart1.setYScaleTitle("Dx [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));		
						dsourcey.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocy,yvaluey2,zeroy));
						chart2.setYScaleTitle("Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);
						
					}else if(child.equals("diff(Disp-DMdl)")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,diffx,errorx));	
						chart1.setYScaleTitle("\u0394Dx [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,diffy,errory));		
						chart2.setYScaleTitle("\u0394Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);	
					}else if(child.equals("Dipse")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));	
						chart1.setYScaleTitle("\u0394Dx [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));		
						chart2.setYScaleTitle("\u0394Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);			
						
					}
				}else if(parent.contains("Normalized")){
					
					xkey="ndx";
					ykey="ndy";
					
					//x
					bpmnames.put("Normalizedx",maptemp.get(xkey).getStringData("NAME"));
					slocx=maptemp.get(xkey).getDoubleData("S");
					yvaluex1=maptemp.get(xkey).getDoubleData("NDX");
					yvaluex2=maptemp.get(xkey).getDoubleData("NDXMDL");
					errorx=maptemp.get(xkey).getDoubleData("STDNDX");		
					diffx=new double[yvaluex1.length];
					zerox=new double[yvaluex1.length];
			
					for(int j=0;j<yvaluex1.length;j++){
						diffx[j]=(yvaluex1[j]-yvaluex2[j]);
						zerox[j]=0;
					}
					
					//y
				/*	bpmnames.put("Normalizedy",maptemp.get(ykey).getStringData("NAME"));
					slocy=maptemp.get(ykey).getDoubleData("S");
					yvaluey1=maptemp.get(ykey).getDoubleData("NDY");
					yvaluey2=maptemp.get(ykey).getDoubleData("NDYMDL");
					errory=maptemp.get(ykey).getDoubleData("STDNDY");			
					diffy=new double[yvaluey1.length];
					zeroy=new double[yvaluey1.length];*/
					
				/*	for(int j=0;j<yvaluey1.length;j++){
						diffy[j]=(yvaluey1[j]-yvaluey2[j]);
						zeroy[j]=0;
					}*/
					if(child.equals("NDisp-NDMdl")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,errorx));	
						dsourcex.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocx,yvaluex2,zerox));
						chart1.setYScaleTitle("NDx [sqrt(m)]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
					/*	dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey1,errory));		
						dsourcey.addDataSet(new DefaultErrorDataSet("mo_"+selfilename,slocy,yvaluey2,zeroy));
						chart2.setYScaleTitle("Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);*/
						
					}else if(child.equals("diff(NDisp-NDMdl)")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,diffx,errorx));	
						chart1.setYScaleTitle("\u0394Dx [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						/*dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,diffy,errory));		
						chart2.setYScaleTitle("\u0394Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);*/	
					}else if(child.equals("Dipse")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));	
						chart1.setYScaleTitle("\u0394Dx [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						/*dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));		
						chart2.setYScaleTitle("\u0394Dy[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);*/			
						
					}
				}else if(parent.contains("Closed")){
					
					xkey="cox";
					ykey="coy";
					
					//x
					bpmnames.put("Closedx",maptemp.get(xkey).getStringData("NAME"));
					slocx=maptemp.get(xkey).getDoubleData("S");
					yvaluex1=maptemp.get(xkey).getDoubleData("X");
					yvaluex2=maptemp.get(xkey).getDoubleData("STDX");
					diffx=new double[yvaluex1.length];
					zerox=new double[yvaluex1.length];
			
					for(int j=0;j<yvaluex1.length;j++){
						//diffx[j]=(yvaluex1[j]-yvaluex2[j]);
						zerox[j]=0;
					}
					
					//y
					bpmnames.put("Closedy",maptemp.get(ykey).getStringData("NAME"));
					slocy=maptemp.get(ykey).getDoubleData("S");
					yvaluey1=maptemp.get(ykey).getDoubleData("Y");
					yvaluey2=maptemp.get(ykey).getDoubleData("STDY");
					diffy=new double[yvaluey1.length];
					zeroy=new double[yvaluey1.length];
					
					for(int j=0;j<yvaluey1.length;j++){
					//	diffy[j]=(yvaluey1[j]-yvaluey2[j]);
						zeroy[j]=0;
					}
					
					if(child.equals("CO")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,yvaluex1,yvaluex2));	
						chart1.setYScaleTitle("\u0394x [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,yvaluey2,yvaluey2));		
						chart2.setYScaleTitle("\u0394y[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);	
					}else if(child.equals("COe")){
						//x
						dsourcex.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocx,errorx,zerox));	
						chart1.setYScaleTitle("\u0394x [m]");
						chart1.setXScaleTitle("Location [m]");
						chart1.setLegendVisible(true);
						setRange(dsourcex,chart1,0.8);
		
						//y
						dsourcey.addDataSet(new DefaultErrorDataSet("me_"+selfilename,slocy,errory,zeroy));		
						chart2.setYScaleTitle("\u0394y[m]");
						chart2.setXScaleTitle("Location [m]");
						chart2.setLegendVisible(true);
						setRange(dsourcey,chart2,0.8);			
						
					}
				}
				
				errorRendererx.setDataSource(dsourcex);
				errorRenderery.setDataSource(dsourcey);
		}
	}
	
	/*
	 * load data
	 */
	private HashMap<String,HashMap<String,TFSReader>> datamap=new HashMap<String, HashMap<String,TFSReader>>();
	private HashMap<String,TFSReader> tfsmap;
	
	private void loaddata(String path,String method){
		tfsmap=new HashMap<String, TFSReader>();
		//phases
		if(new File(path+"getphasetotx_free.out").exists()){
			addTfsReader("phasex",path+"getphasex_free.out");
			addTfsReader("phasey",path+"getphasey_free.out");
		}else{
			addTfsReader("phasex",path+"getphasex.out");
			addTfsReader("phasey",path+"getphasey.out");
		}

		if(new File(path+"getphasetotx_free.out").exists()){
			addTfsReader("phasetotx",path+"getphasetotx_free.out");
			addTfsReader("phasetoty",path+"getphasetoty_free.out");
		}else{
			addTfsReader("phasetotx",path+"getphasetotx.out");
			addTfsReader("phasetoty",path+"getphasetoty.out");
		}
		
		//beta

		if(new File(path+"getbetax_free.out").exists()){
			addTfsReader("betax",path+"getbetax_free.out");
			addTfsReader("betay",path+"getbetay_free.out");
		}else{
			addTfsReader("betax",path+"getbetax.out");
			addTfsReader("betay",path+"getbetay.out");	
		}

		if(new File(path+"getampbetax_free.out").exists()){
			addTfsReader("ampbetax",path+"getampbetax_free.out");
			addTfsReader("ampbetay",path+"getampbetay_free.out");
		}else{
			addTfsReader("ampbetax",path+"getampbetax.out");
			addTfsReader("ampbetay",path+"getampbetay.out");	
		}
		
		//coupling
		if(new File(path+"getcouple_free.out").exists()){
			addTfsReader("couple",path+"getcouple_free.out");
		}else{
			addTfsReader("couple",path+"getcouple.out");			
		}
		addTfsReader("couple_terms",path+"getcoupleterms.out");	
		
		//non-linear
		addTfsReader("sex1200",path+"getsex1200.out");	
		addTfsReader("sex3000",path+"getsex3000.out");
		addTfsReader("sex4000",path+"getoct4000.out");
		addTfsReader("chi1200",path+"getchi1200.out");	
		addTfsReader("chi3000",path+"getchi3000.out");
		addTfsReader("chi4000",path+"getchi4000.out");		
		
		
		
		if(new File(path+"getDx.out").exists()){
			addTfsReader("dx",path+"getDx.out");
			addTfsReader("ndx",path+"getNDx.out");
			addTfsReader("dy",path+"getDy.out");
			//addTfsReader("ndy",path+"getNDy.out");
			addTfsReader("cox",path+"getCOx.out");
			addTfsReader("coy",path+"getCOy.out");	
			//addTfsReader("chroma",path+"getchro.out");
			//addTfsReader("chromacouple",path+"getchrocouple.out");	
			addTfsReader("chromabeta",path+"getchrobeta.out");	
			
		}
		
		//misc
		addTfsReader("kick",path+"getkick.out");
		log.info(new File(path).getName());
		datamap.put(new File(path).getName()+"_"+method,tfsmap);
	
		
	}
	
	/*
	 * adding to tfsmap
	 */

    private void addTfsReader(String value, String filepath) {
        TFSReader tfstemp = new TFSReader();
        tfstemp.loadTable(filepath);
        tfsmap.put(value, tfstemp);
    }
	
	
	/*
	 * handling children
	 */
	
	private HashMap<String,ArrayList<String>> childrenmap = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> childrenlist;
	
	private void fillchildren(){
		
		//phase
		childrenlist=new ArrayList<String>();
		childrenlist.add("Phase-PhMdl");
		childrenlist.add("Diff(Phase-PhMdl)");
		//childrenlist.add("Phase");
		//childrenlist.add("PhMdl");
		//childrenlist.add("Error");
		childrenmap.put("Phase", childrenlist);
				
		//beta
		childrenlist=new ArrayList<String>();
		childrenlist.add("Beta-beat");
		childrenlist.add("Beta-BMdl");
		//childrenlist.add("Beta");
		//childrenlist.add("BMdl");
		//childrenlist.add("Error");	
		childrenmap.put("Beta", childrenlist);
		
		//coupling
		childrenlist=new ArrayList<String>();
		childrenlist.add("amp");
		childrenlist.add("real");
		childrenlist.add("imaginary");
		//childrenlist.add("error");
		childrenmap.put("Coupling", childrenlist);	
		
		//dispersion
		childrenlist=new ArrayList<String>();
		childrenlist.add("Disp-DMdl");
		childrenlist.add("diff(Disp-DMdl)");
		//childrenlist.add("Disp");
		//childrenlist.add("Dispe");
		childrenmap.put("Dispersion", childrenlist);	
		
		//co
		childrenlist=new ArrayList<String>();
		childrenlist.add("CO");
		//childrenlist.add("COe");
		childrenmap.put("Closed", childrenlist);	
		
		//norm disp
		childrenlist=new ArrayList<String>();
		childrenlist.add("NDisp-NDMdl");
		childrenlist.add("diff(NDisp-NDMdl)");
		//childrenlist.add("NDisp");
		//childrenlist.add("NDispe");
		childrenmap.put("Normalized", childrenlist);	
		
		//chromatic coupling
		childrenlist=new ArrayList<String>();
		childrenlist.add("DCC-DCCMDL");
		childrenlist.add("diff(DCC-DCCMDL)");
		//childrenlist.add("DCC");
		childrenlist.add("DCCMDL");
		//childrenlist.add("Error");		
		childrenmap.put("ChromaCouple", childrenlist);	
		
		//chromatic beta
		childrenlist=new ArrayList<String>();
		childrenlist.add("DBB-DBBMDL");
		childrenlist.add("diff(DBB-DBBMDL)");
		//childrenlist.add("DBB");
		childrenlist.add("DBBMDL");
		//childrenlist.add("Error");		
		childrenmap.put("ChromaBeta", childrenlist);		
		
		//rdt
		childrenlist=new ArrayList<String>();
		childrenlist.add("F1200");
		childrenlist.add("F3000");
		//childrenlist.add("F1200e");
		//childrenlist.add("R3000e");
		childrenmap.put("Resonance", childrenlist);			
		
		//chi
		childrenlist=new ArrayList<String>();
		childrenlist.add("C1200");
		childrenlist.add("C3000");
		//childrenlist.add("C1200e");
		//childrenlist.add("C3000e");
		childrenmap.put("Chi", childrenlist);			
		
		
	}
	
	private String[] child;
	private DefaultMutableTreeNode tempnode;
	
	
	private void placeChildren(){
		model = (DefaultTreeModel)tree.getModel();
		for(int i=0;i<root.getChildCount();i++){
			child=root.getChildAt(i).toString().split(" ");
			if(childrenmap.containsKey(child[0])){
				childrenlist=childrenmap.get(child[0]);
				for(int j=0;j<childrenlist.size();j++){
					tempnode=new DefaultMutableTreeNode(childrenlist.get(j));
					model.insertNodeInto(tempnode,(MutableTreeNode) root.getChildAt(i),j);
				}
			}
			
		}
	}
	
	/*
	 * filling tree
	 */
	private DefaultMutableTreeNode fol1;
	private DefaultMutableTreeNode fol2;
	private DefaultMutableTreeNode fol3;
	private DefaultMutableTreeNode fol4;
	private DefaultMutableTreeNode fol5;
	private DefaultMutableTreeNode fol6;
	private DefaultMutableTreeNode fol7;
	private DefaultMutableTreeNode fol8;
	private DefaultTreeModel model;
	private int index;
	
	private void filltree(){
		tree.removeTreeSelectionListener(treelistener);
		root.removeAllChildren();
		index=tabs.getSelectedIndex();
		//MessageManager.getConinfo(" OpticsPanel => You selected tab "+index);
		model = (DefaultTreeModel)tree.getModel();
		
		
		
		switch (index) {
			case 0:
				fol1 = new DefaultMutableTreeNode("Phase");
				//fol2 = new DefaultMutableTreeNode("Phase free");
				fol3 = new DefaultMutableTreeNode("Phase total");
				//fol4 = new DefaultMutableTreeNode("Phase total free");				
				fol5 = new DefaultMutableTreeNode("Beta");
				//fol6 = new DefaultMutableTreeNode("Beta free");
				fol7 = new DefaultMutableTreeNode("Beta amp");
				//fol8 = new DefaultMutableTreeNode("Beta amp free");
				
				model.insertNodeInto(fol1, root, 0);
				//model.insertNodeInto(fol2, root, 1);
				model.insertNodeInto(fol3, root, 1);
				//model.insertNodeInto(fol4, root, 3);				
				model.insertNodeInto(fol5, root, 2);
				//model.insertNodeInto(fol6, root, 5);
				model.insertNodeInto(fol7, root, 3);
				//model.insertNodeInto(fol8, root, 7);
				model.reload();
				break;
				
			case 1:
				fol1 = new DefaultMutableTreeNode("Coupling");
				//fol2 = new DefaultMutableTreeNode("Coupling free");
				
				model.insertNodeInto(fol1, root, 0);
				//model.insertNodeInto(fol2, root, 1);
				model.reload();
				break;
				
			case 2:		
				fol1 = new DefaultMutableTreeNode("Dispersion");
				fol2 = new DefaultMutableTreeNode("Normalized dispersion");
				fol3 = new DefaultMutableTreeNode("Closed orbit");
				
				model.insertNodeInto(fol1, root, 0);
				model.insertNodeInto(fol2, root, 1);
				model.insertNodeInto(fol3, root, 2);
				model.reload();	
				break;
				
			case 3:
				fol1 = new DefaultMutableTreeNode("ChromaCouple");
				//fol2 = new DefaultMutableTreeNode("ChromaCouple free");
				fol3 = new DefaultMutableTreeNode("ChromaBeta");
				//fol4 = new DefaultMutableTreeNode("ChromaBeta free");
				
				model.insertNodeInto(fol1, root, 0);
				//model.insertNodeInto(fol2, root, 1);
				model.insertNodeInto(fol3, root, 1);
				//model.insertNodeInto(fol4, root, 3);				
				model.reload();		
				break;
				
			case 4:
				fol1 = new DefaultMutableTreeNode("Resonance driving terms");
				fol2 = new DefaultMutableTreeNode("Chi terms");				
				model.insertNodeInto(fol1, root, 0);
				model.insertNodeInto(fol2, root, 1);
				model.reload();				
				break;
				
		}
		tree.addTreeSelectionListener(treelistener);
	}
	
	/**
	 * Create GUI
	 */
	private JTabbedPane tabs = new JTabbedPane();
	private GridBagLayout gridBag0 = new GridBagLayout();
	
	private ListSelectionListener listselection;
	
	//charts
	private Chart chart1x=new Chart();
	private Chart chart1y=new Chart();
	
	private Chart chart2x=new Chart();
	private Chart chart2y=new Chart();	
	
	private Chart chart3x=new Chart();
	private Chart chart3y=new Chart();
	
	private Chart chart4x=new Chart();
	private Chart chart4y=new Chart();	
	
	private Chart chart5x=new Chart();
	private Chart chart5y=new Chart();	
	
	private JSplitPane split1= new JSplitPane(JSplitPane.VERTICAL_SPLIT,chart1x,chart1y);
	private JSplitPane split2= new JSplitPane(JSplitPane.VERTICAL_SPLIT,chart2x,chart2y);
	private JSplitPane split3= new JSplitPane(JSplitPane.VERTICAL_SPLIT,chart3x,chart3y);
	private JSplitPane split4= new JSplitPane(JSplitPane.VERTICAL_SPLIT,chart4x,chart4y);	
	private JSplitPane split5= new JSplitPane(JSplitPane.VERTICAL_SPLIT,chart5x,chart5y);	
	
	private ArrayList<Chart> chartlist = new ArrayList<Chart>();
	
	//option panel
	private JPanel panelcor = new JPanel();
	private JLabel labelfiles = new JLabel("Files:");
	private DefaultListModel listmodel = new DefaultListModel();
	private JList listfiles = new JList(listmodel);
	private JScrollPane scrolllist = new JScrollPane(listfiles);
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	private JTree tree = new JTree(root);
	private JScrollPane scrolltree = new JScrollPane(tree);
	//private JCheckBox checkdiff = new JCheckBox("Show error bar",true);
	private JButton corbutton = new JButton("Correction");
	
	//general panel
	private JPanel oppanel = new JPanel();
	private JTabbedPane tabsmain = new JTabbedPane();
	
	public void CreateGUI(){
	    		
		//placing charts in list
		chart1x.setName("chart1x");
		chart1y.setName("chart1y");
		chart2x.setName("chart2x");
		chart2y.setName("chart2y");
		chart3x.setName("chart3x");
		chart3y.setName("chart3y");
		chart4x.setName("chart4x");
		chart4y.setName("chart4y");
		chart5x.setName("chart5x");
		chart5y.setName("chart5y");
		
		chartlist.add(chart1x);
		chartlist.add(chart1y);	
		chartlist.add(chart2x);
		chartlist.add(chart2y);
		chartlist.add(chart3x);
		chartlist.add(chart3y);	
		chartlist.add(chart4x);
		chartlist.add(chart4y);		
		chartlist.add(chart5x);
		chartlist.add(chart5y);
		
		tree.setRootVisible(false);
		
		oppanel.setLayout(new BorderLayout());
		
		//tabs
		oppanel.add(tabs,BorderLayout.CENTER);
		tabs.addTab("Linear", split1);	
		split1.setResizeWeight(0.5);
		split1.setOneTouchExpandable(true);
		tabs.addTab("Coupling", split2);
		split2.setResizeWeight(0.5);
		split2.setOneTouchExpandable(true);
		tabs.addTab("Off-momentum",split3 );
		split3.setResizeWeight(0.5);
		split3.setOneTouchExpandable(true);
		tabs.addTab("Chromatic",split4 );
		split4.setResizeWeight(0.5);
		split4.setOneTouchExpandable(true);
		tabs.addTab("Non-linear", split5);
		split5.setResizeWeight(0.5);
		split5.setOneTouchExpandable(true);
		InitialChart();
		
		//option panel
		panelcor.setLayout(gridBag0);
		gridBag0.rowWeights = new double[] {1,1,1,1,1};
		gridBag0.rowHeights = new int[] {10,70,250,20,30};
		gridBag0.columnWeights = new double[] {1};
		gridBag0.columnWidths = new int[] {200};
		gridBag0.setConstraints(labelfiles, gridconstrainer(5,10,0,5,1,1,0,0,panelcor,labelfiles));
		gridBag0.setConstraints(scrolllist, gridconstrainer(0,10,10,5,1,1,0,1,panelcor,scrolllist));
		gridBag0.setConstraints(scrolltree, gridconstrainer(5,10,10,5,1,1,0,2,panelcor,scrolltree));
		//gridBag0.setConstraints(checkdiff, gridconstrainer(5,10,10,5,1,1,0,3,panelcor,checkdiff));
		gridBag0.setConstraints(corbutton, gridconstrainer(5,10,10,5,1,1,0,4,panelcor,corbutton));		
		oppanel.add(panelcor,BorderLayout.WEST);
		
		oppanel.setVisible(true);		
		
		tabsmain.addTab("Optics",oppanel);
		tabsmain.addTab("Segment-by-Segment",sbspanel);
		
		setLayout(new BorderLayout());
		add(tabsmain,BorderLayout.CENTER);
	
	}
	
	/*
	 * Initial chart
	 */
	private double[] xxx={0};
	private DataPickerInteractor dataPicker;
	private String[] selnames=new String[]{""};
	
	private void InitialChart(){
		for(int i=0;i<chartlist.size();i++){
			dataPicker = new DataPickerInteractor();
			dataPicker.getPointCoordPane().getLabelRenderer().setBackground(new Color(204, 204, 255));
			chartlist.get(i).addInteractor(ChartInteractor.ZOOM);
			chartlist.get(i).addInteractor(dataPicker);
			chartlist.get(i).setDataSet(new DefaultDataSet("Empty",xxx,xxx));
		     dataPicker.addChartInteractionListener(new ChartInteractionListener() {
		            public void interactionPerformed(ChartInteractionEvent evt) {
		                		                
		                if (evt.getMouseEvent().getButton() == MouseEvent.BUTTON1) {
			                DisplayPoint p = evt.getDisplayPoint();
			                DataSet dataset = p.getDataSet();
			                if(evt.getInteractor().getChart().getName().contains("x")){
			                	selnames=bpmnames.get(node.getParent().toString().split(" ")[0]+"x");
			                }else{
			                	selnames=bpmnames.get(node.getParent().toString().split(" ")[0]+"y");
			                }			        
			                
			                log.info(counter);
		                	
		                	if(counter==1 && selnames.length>2){
		                		endbpm=selnames[p.getIndex()];
			                	sbssel();
			                	counter=0;
		                	}else if(counter==0 && selnames.length>2){
		                		startbpm=selnames[p.getIndex()];
		                		 counter++;
		                	}
		                }
		            }
		        });
		}
	}
	
	private void emptycharts(){
		for(int i=0;i<chartlist.size();i++){
			chartlist.get(i).removeAllRenderers();
		}
	}
	
	/*
	 * segment selector
	 */
	private JFrame framsbs= new JFrame();
	private GridLayout sbslay = new GridLayout(4,2,20,20);
	
	private JButton buttonsbsgo = new JButton("Go");
	private JButton buttonsbscancel = new JButton("Cancel");	
	
	private JLabel labelbpm1 = new JLabel("Start BPM :");
	private JTextField filedbpm1 = new JTextField();	
	
	private JLabel labelbpm2 = new JLabel("End BPM :");
	private JTextField filedbpm2 = new JTextField("");	
	
	private JLabel labelseg = new JLabel("Segment name :");
	private JTextField filedseg = new JTextField("");	
	
	private int counter=0;
	private String startbpm;
	private String endbpm;
	

	/*
	 * sbs selector
	 */
	private void sbssel(){
		framsbs.setLayout(sbslay);
		framsbs.setVisible(true);
		framsbs.setSize(new Dimension(350,200));
		framsbs.setTitle("Segment sel");
		framsbs.add(labelbpm1);
		framsbs.add(filedbpm1);
		filedbpm1.setText(startbpm);

		framsbs.add(labelbpm2);
		framsbs.add(filedbpm2);
		filedbpm2.setText(endbpm);
		
		framsbs.add(labelseg);
		framsbs.add(filedseg);
		
		framsbs.add(buttonsbsgo);
		buttonsbsgo.setBackground(Color.green);
		framsbs.add(buttonsbscancel);	
		buttonsbscancel.setBackground(Color.red);
		
		
		
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
	

	
	/*public static void main (String args[]){
		JFrame frame = new JFrame();
		OpticsPanel op=new OpticsPanel();
		frame.add(op);
		frame.setVisible(true);
		frame.setSize(new Dimension(800,800));
	}*/

}
