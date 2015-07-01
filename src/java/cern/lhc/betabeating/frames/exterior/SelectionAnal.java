package cern.lhc.betabeating.frames.exterior;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.lhc.betabeating.Tools.FileIO;
import cern.lhc.betabeating.constants.Analysis_const;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.frames.Controller;
import cern.lhc.betabeating.frames.interior.AnalysisPanel;

public class SelectionAnal extends JFrame {
    private static final Logger log = Logger.getLogger(SelectionAnal.class);
    private static final long serialVersionUID = 5840008091420473693L;
	private Analysis_const analconst= new Analysis_const();
	private AnalysisPanel apanel;
	private Controller controller;
	/**
	 * @param args
	 */
	public SelectionAnal(Controller controller){
	    this.controller = controller;
		createGUI();
		listeners();
		server=controller.getPathDataForKey("server");
		putInfo();
	}
	
	/*
	 * Setting env and paths
	 */
	private String[] env;
	private String[] env2=new String[1];
	public void setenv(String[] envin){
		env=envin;
	}
	private HashMap<String,String> analMap = new HashMap<String, String>();
	

	/**
	 * listeners
	 */
	//private String svdmode="1,1,0,0";
	private String couplingmode="0";
	private void listeners(){
		//go button
		buttongo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			if(!(Files4Anal.size()==0)){
				if(!checksussix.isSelected() && !checksvd.isSelected() && !jCheckBoxWAnalysis.isSelected()){
					MessageManager.getConsoleLogger().warn("AnalysisPanel => You did not selected any algorithm \n NOTHING will happen");
					return;
				}
                if ((checksussix.isSelected() || checksvd.isSelected()) && isInputOk()) {
                    Task task = new Task() {
                        protected Object construct() {
                            try {
                                for (int i = 0; i < Files4Anal.size(); i++) {
                                    String command;
                                    if (checksussix.isSelected()) {
                                        command = controller.getPathDataForKey("harpro") + "  " + new File(Files4Anal.get(i)).getParent().toString();
                                        WriteTerms(new File(Files4Anal.get(i)).getParent().toString(), Files4Anal.get(i));
                                        MessageManager.getConsoleLogger().info(
                                                "AnalysisPanel => Will trigger harmonic analysis for file " + (i + 1) + " of " + Files4Anal.size() + "\n"
                                                        + command);
                                        syscall(command, "Harmonic analysis", Files4Anal.get(i), "SUSSIX");
                                    }
                                    if (checksvd.isSelected()) {
                                        command = controller.getPathDataForKey("python") + " " + controller.getPathDataForKey("svdpro") + "  -f " + Files4Anal.get(i) + " -a  "
                                                + controller.getPathDataForKey("accel") + "   -m  " + couplingmode + "  -p  "
                                                + new File(Files4Anal.get(i)).getParent().toString() + " -u " + controller.getPathDataForKey("labelmsvd");
                                        MessageManager.getConsoleLogger().info(
                                                "AnalysisPanel => Will trigger svd analysis for file " + (i + 1) + " of " + Files4Anal.size() + "\n"
                                                        + command);
                                        WriteTerms(new File(Files4Anal.get(i)).getParent().toString(), Files4Anal.get(i));
                                        syscall(command, "Singular value decomposition", Files4Anal.get(i), "SVD");
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Task failed", e);
                                MessageManager.error("AnalysisPanel => Caused by exception: ", e, null);
                                MessageManager.info("AnalysisPanel => Error", null);
                            }

                            return null;
                        }

                    };
                    task.setName("Running SVD/SUSSIX for " + Files4Anal.size() + " files");
                    task.setCancellable(false);
                    task.start();
                    setVisible(false);

                }
				if (jCheckBoxWAnalysis.isSelected())
				{
//			        Programs.wAnalysis.execute(WAnalysisData.prepareObject()
//	                .setTwissPath("hitwiss")
//	                .setOutputPath("hioutputpath")
//	                .setAccelerator("hiaccel")
//	                .setAlgorithm("hialgorithm")
//	                .setQdx("hiqdx")
//	                .setQdy("hiqdy")
//	                .setQx("hidx")
//	                .setQy("hidy")
//	                .setData("hidata")
//	                .create());
				    MessageManager.error("AnalysisPanel => WAnalysis not yet implemented! ", null, null);
	                MessageManager.getConsoleLogger().error("AnalysisPanel => FATAL error, contact expert");
				}
			}else{
				MessageManager.error("AnalysisPanel => Seleted files have not been passed! ", null, null);
				MessageManager.getConsoleLogger().error("AnalysisPanel => FATAL error, contact expert");
			}
		}});
		
		//cancel button
		buttoncancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			setVisible(false);
			MessageManager.getConsoleLogger().warn("AnalysisPanel => Cancel button has been pressed nothing will happen");
		}});	
		
		/// parrallel settings
		parallelSettings.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			paraGUI();
		}});	
		
	}
	
	/**
	 * Handling data
	 */
	private List<String> Files4Anal=new ArrayList<String>();
	public void setFiles(List<String> toAnal){
		Files4Anal = toAnal;
	}
	
	private void putInfo(){
	    if(controller.getBeamSelectionData().getAccelerator().contains("LHC")){
	        analMap=analconst.LHC();
	    }else{
	        analMap=analconst.SPS();
	    }
	    controller.setKeyWithPathData("tunex",analMap.get("tunex"));
	    controller.setKeyWithPathData("tuney",analMap.get("tuney"));
	    controller.setKeyWithPathData("sbpm",analMap.get("sbpm"));
	    controller.setKeyWithPathData("ebpm",analMap.get("ebpm"));
	    controller.setKeyWithPathData("istun",analMap.get("istun"));
	    controller.setKeyWithPathData("kick",analMap.get("kick"));
	    controller.setKeyWithPathData("a1",analMap.get("a1"));
	    controller.setKeyWithPathData("a2",analMap.get("a2"));
	    controller.setKeyWithPathData("b1",analMap.get("b1"));
	    controller.setKeyWithPathData("b2",analMap.get("b2"));
	    controller.setKeyWithPathData("eturn",analMap.get("eturn"));
	    
		tunexfield.setText(controller.getPathDataForKey("tunex"));
		tuneyfield.setText(controller.getPathDataForKey("tuney"));
		startfield.setText(controller.getPathDataForKey("sbpm"));
		endfield.setText(controller.getPathDataForKey("ebpm"));
		sepfield.setText(controller.getPathDataForKey("istun"));
		kickfield.setText(controller.getPathDataForKey("kick"));
		a1field.setText(controller.getPathDataForKey("a1"));
		a2field.setText(controller.getPathDataForKey("a2"));	
		b1field.setText(controller.getPathDataForKey("b1"));
		b2field.setText(controller.getPathDataForKey("b2"));
		turn1field.setText(controller.getPathDataForKey("eturn"));
	}
	
	private boolean isInputOk(){
	    boolean result = true;
	    ArrayList<JTextField> test4fields=new ArrayList<JTextField>();
	    test4fields.add(tunexfield);
        test4fields.add(tuneyfield);
        test4fields.add(startfield);
        test4fields.add(endfield);
        test4fields.add(sepfield);
        test4fields.add(kickfield);
        test4fields.add(a1field);
        test4fields.add(a2field);
        test4fields.add(b1field);
        test4fields.add(b2field);
        test4fields.add(turn1field);
	    for (JTextField test4fieldsItem : test4fields) {
	        String value = test4fieldsItem.getText();
	        try{
                Double.parseDouble(value);
            } catch(Exception e) {
                result = false;
                MessageManager.error("AnalysisPanel => Please input Numerical value",null,null);
                MessageManager.getConsoleLogger().error("AnalysisPanel => " + value);
            }
        }
		return result;
	}
	
	/*
	 * terms
	 */
	private void WriteTerms(String path,String file2run){
        // String Driveinp = ("KICK ="+kickd + "\n"
        // + "CASE(1[H], 0[V])=1 \n"
        // + "KPER(KICK PERCE.)=0.5 \n"
        // +"TUNE X ="+tunexfield.getText()+"\n"
        // +"TUNE Y ="+ tuneyfield.getText()+"\n"
        // +"PICKUP START="+startfield.getText()+ "\n"
        // +"PICKUP END  ="+endfield.getText()+"\n"
        // +"NORMALISATION(1[yes])=1\n"
        // + "ISTUN ="+sepfield.getText() +"\n"
        // + "BETABEATING(1[yes])=0\n"
        // + "IR(1[Real], 0[Comp])=0\n"
        // + "LABEL RUN (1[yes])=0\n"
        // + "FORMAT (0[SPS],1[HERA],2[RHIC])=2\n"
        // + "NOISEPATH =noisefiles/\n"
        // + "WINDOWa1="+a1field.getText()+"\n"
        // + "WINDOWa2="+a2field.getText()+"\n"
        // + "WINDOWb1="+b1field.getText()+"\n"
        // + "WINDOWb2="+b2field.getText()+"\n");
        StringBuilder stringBuilderDriveInp = new StringBuilder();
        stringBuilderDriveInp.append("KICK =").append(kickfield.getText()).append("\n");
        stringBuilderDriveInp.append("CASE(1[H], 0[V])=1 \n");
        stringBuilderDriveInp.append("KPER(KICK PERCE.)=0.5 \n");
        stringBuilderDriveInp.append("TUNE X =").append(tunexfield.getText()).append("\n");
        stringBuilderDriveInp.append("TUNE Y =").append(tuneyfield.getText()).append("\n");
        stringBuilderDriveInp.append("PICKUP START=").append(startfield.getText()).append("\n");
        stringBuilderDriveInp.append("PICKUP END  =").append(endfield.getText()).append("\n");
        stringBuilderDriveInp.append("NORMALISATION(1[yes])=1\n");
        stringBuilderDriveInp.append("ISTUN =").append(sepfield.getText()).append("\n");
        stringBuilderDriveInp.append("BETABEATING(1[yes])=0\n");
        stringBuilderDriveInp.append("IR(1[Real], 0[Comp])=0\n");
        stringBuilderDriveInp.append("LABEL RUN (1[yes])=0\n");
        stringBuilderDriveInp.append("FORMAT (0[SPS],1[HERA],2[RHIC])=2\n");
        stringBuilderDriveInp.append("NOISEPATH =noisefiles/\n");
        stringBuilderDriveInp.append("WINDOWa1=").append(a1field.getText()).append("\n");
        stringBuilderDriveInp.append("WINDOWa2=").append(a2field.getText()).append("\n");
        stringBuilderDriveInp.append("WINDOWb1=").append(b1field.getText()).append("\n");
        stringBuilderDriveInp.append("WINDOWb2=").append(b2field.getText()).append("\n");

        FileIO.writeContentToFile(stringBuilderDriveInp.toString(), new File(path + "/Drive.inp"));

        String DrivingTerms = (file2run + " 1 " + turn1field.getText()); // changed from variable to 1, because this is not used by drive
        FileIO.writeContentToFile(DrivingTerms, new File(path + "/DrivingTerms"));

        controller.setKeyWithPathData("tunex", tunexfield.getText());
        controller.setKeyWithPathData("tuney", tuneyfield.getText());
        controller.setKeyWithPathData("sbpm", startfield.getText());
        controller.setKeyWithPathData("ebpm", endfield.getText());
        controller.setKeyWithPathData("istun", sepfield.getText());
        controller.setKeyWithPathData("kick", kickfield.getText());
        controller.setKeyWithPathData("a1", a1field.getText());
        controller.setKeyWithPathData("a2", a2field.getText());
        controller.setKeyWithPathData("b1", b1field.getText());
        controller.setKeyWithPathData("b2", b2field.getText());
        controller.setKeyWithPathData("eturn", turn1field.getText());
	}
	
	private void syscall(String command,final String title,final String filee,final String method){
		
				MessageManager.info("AnalysisPanel => Triggering command "+title, null);
				MessageManager.getConsoleLogger().info("AnalysisPanel => Command "+command);
				
				if(method.equals("SUSSIX")){
					if(parallel.isSelected() && serverbox.isSelected()){
						
						env2[0]="OMP_NUM_THREADS="+not;//for sussix
						command="ssh "+server+" "+command+" ";					
					}else if(!parallel.isSelected() && serverbox.isSelected()){
						env2[0]="OMP_NUM_THREADS=1";//for sussix
						command="ssh "+server+" "+command+" ";	
					}else if(parallel.isSelected() && !serverbox.isSelected()){
						env2[0]="OMP_NUM_THREADS="+not;//for sussix	
					}else{
						env2[0]="OMP_NUM_THREADS=1";//for sussix	
					}
					
					log.info("systemcall sussix");
					Systemcall.execute(command, title, null, new File(filee).getParent(), true);
					log.info("systemcall sussix finished, add to panel");
					apanel.addFile(filee, method);
					log.info("systemcall sussix finished, add to panel finshed");
				}else{
				    log.info("systemcall not sussix");
				    Systemcall.execute(command, title, env, new File(filee).getParent(), true);
					apanel.addFile(filee, method);
				}
	}
	
	/**
	 *  graphics
	 */
	// window for parallel settings
	private JFrame framePara;
	private GridLayout prid = new GridLayout(5,2,20,20);
	private JComboBox comboServer = new JComboBox();
	private JButton outServer = new JButton();
	private JButton testbutton = new JButton("Test server");
	private JComboBox comboThreads = new JComboBox(new String[] {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"});
	private JButton applybutton = new JButton("Apply");
	private JButton cancelbutton = new JButton("Cancel");
	private JCheckBox serverbox = new JCheckBox("",true);
	
	private String not="24";
	private String server;
	
	private Process process;
	private String sReadIn;
	private String sReadErr;
	
	private ArrayList<String> serverlist;
	
	private void paraGUI(){
		
		framePara = new JFrame();
		
		framePara.setTitle("Parallel SUSSIX");
		framePara.setLayout(prid);
		
		framePara.add(new JLabel("Server :"));
		framePara.add(comboServer);
		
		framePara.add(outServer);
		framePara.add(testbutton);
		
		framePara.add(new JLabel("Number of threads :"));
		framePara.add(comboThreads);
		
		framePara.add(new JLabel("Run on server ?"));
		framePara.add(serverbox);	
		
		framePara.add(applybutton);
		framePara.add(cancelbutton);	
		
		serverlist = new ArrayList<String>();
		for(int i =0;i<comboServer.getItemCount();i++){
			serverlist.add(comboServer.getItemAt(i).toString());
		}
		
		if(!serverlist.contains(server)){
			comboServer.addItem(server);
		}
		
		if(!serverlist.contains("New")){		
			comboServer.addItem("New");
		}
		
		
		comboServer.setSelectedItem(server);
		comboThreads.setSelectedItem("24");
		
		comboServer.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {			
			if(comboServer.getSelectedItem().equals("New")){
				String response = JOptionPane.showInputDialog( "Enter a server name:" ,"server");
				if(!response.equals("server")){
					if(!serverlist.contains(server)){
						comboServer.addItem(response);
					}
					comboServer.setSelectedItem(response);
				}
			}		
		}});
		
		testbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){
			try {
				process = Runtime.getRuntime().exec("ssh "+comboServer.getSelectedItem().toString()+" pwd");
			
			
			BufferedReader iStream = new BufferedReader (new InputStreamReader(process.getInputStream()));
				BufferedReader eStream = new BufferedReader (new InputStreamReader(process.getErrorStream()));
				
					while ((sReadIn = iStream.readLine()) != null || (sReadErr =
					eStream.readLine()) != null) {					
					
						if(sReadIn!=null){
							outServer.setBackground(Color.green);
							outServer.setText("OK");
							
							log.info(sReadIn);
							
						}else if(sReadErr!=null){
							outServer.setBackground(Color.red);
							outServer.setText("FAIL");
							
							log.info(sReadErr);
					}
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}});	
			
		applybutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){

			server=comboServer.getSelectedItem().toString();
			not=comboThreads.getSelectedItem().toString();
			
			MessageManager.getConsoleLogger().info("AnalysisPanel => Servername "+server);
			MessageManager.getConsoleLogger().info("AnalysisPanel => Maximum number of threads "+not);
			MessageManager.getConsoleLogger().info("AnalysisPanel => Parallel computing "+parallel.isSelected());
			MessageManager.getConsoleLogger().info("AnalysisPanel => Running on server  "+serverbox.isSelected());	
			MessageManager.info("AnalysisPanel => Values updated",null);	
			
			framePara.setVisible(false);
		}});
		
		cancelbutton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent actionEvent){

			framePara.setVisible(false);
		}});
		
		framePara.setSize(new Dimension(400,250));
		framePara.setVisible(true);
		framePara.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		applybutton.setBackground(Color.GREEN);
		cancelbutton.setBackground(Color.RED);
		
		outServer.setSelected(true);
		outServer.setBackground(Color.orange);
		outServer.setText("NOT TESTED");
		
	}
	
	
	//main
	private GridBagLayout gridBag0 = new GridBagLayout();
	private JPanel panelabove = new JPanel();
	private JPanel paneltools = new JPanel();
	private JPanel panelinput = new JPanel();
	private JPanel panelturns = new JPanel();
	
	//panelabove
	private JCheckBox checksussix = new JCheckBox("SUSSIX",true);	
	private JCheckBox checksvd = new JCheckBox("SVD");
	private JCheckBox jCheckBoxWAnalysis = new JCheckBox("WAnalysis");
	private GridBagLayout gridBagLayoutAlgorithms = new GridBagLayout();
	
	//panel tools
	private GridBagLayout gridBagt= new GridBagLayout();
	private JLabel tunelabel = new JLabel("Find tunes :");
	private JButton tunebutton = new JButton("Tunes");
	private JCheckBox kicklabel = new JCheckBox("Find kick automatic :");
	private JButton kickbutton = new JButton("Kick");
	private JCheckBox parallel = new JCheckBox("Parallel computing");
	private JButton parallelSettings = new JButton("Settings");	
	private JCheckBox automatic = new JCheckBox("Automatic cleaner");	
	private JButton autamaticSettings = new JButton("Settings");	
	
	//panel input
	private GridBagLayout gridBagi= new GridBagLayout();	
	private JLabel tunexlabel = new JLabel("Horizontal Tune :");
	private JTextField tunexfield = new JTextField();
	private JLabel tuneylabel = new JLabel("Vertical Tune :");
	private JTextField tuneyfield = new JTextField();
	private JLabel startlabel = new JLabel("Starting at BPM :");
	private JTextField startfield = new JTextField();
	private JLabel endlabel = new JLabel("Ending at BPM :");
	private JTextField endfield = new JTextField();
	private JLabel seplabel = new JLabel("Tune window :");
	private JTextField sepfield = new JTextField();
	private JLabel a1label = new JLabel("window a1 :");
	private JTextField a1field = new JTextField();
	private JLabel a2label = new JLabel("window a2 :");
	private JTextField a2field = new JTextField();	
	private JLabel b1label = new JLabel("window b1 :");
	private JTextField b1field = new JTextField();	
	private JLabel b2label = new JLabel("window b2 :");
	private JTextField b2field = new JTextField();	
	
	//panel turn
	private GridBagLayout gridBagtu= new GridBagLayout();
	private JLabel labelturn0 = new JLabel("From turn ");
	private JTextField kickfield = new JTextField();	
	private JLabel labelturn1 = new JLabel(" to ");	
	private JTextField turn1field = new JTextField();
	
	//buttons
	private JButton buttongo = new JButton("Go");
	private JButton buttoncancel = new JButton("Cancel");
	
	private void createGUI(){
		setLayout(gridBag0);
		gridBag0.rowWeights = new double[] {1,1,1,1,1};
		gridBag0.rowHeights = new int[] {50,150,200,50,50};
		gridBag0.columnWeights = new double[] {1,1};
		gridBag0.columnWidths = new int[] {175,175};
		
		// panel above
		panelabove.setBorder(new TitledBorder("Algorithms"));	
		gridBag0.setConstraints(panelabove, gridconstrainer(5,5,5,5,2,1,0,0));
		panelabove.setLayout(gridBagLayoutAlgorithms);
		gridBagLayoutAlgorithms.rowWeights = new double[] {1,1};
		gridBagLayoutAlgorithms.rowHeights = new int[] {30,30};
		gridBagLayoutAlgorithms.columnWeights = new double[] {1,1};
		gridBagLayoutAlgorithms.columnWidths = new int[] {150, 150};
		
		gridBagLayoutAlgorithms.setConstraints(checksussix, gridconstrainer(0,10,10,10,1,1,0,0));
		panelabove.add(checksussix);
        gridBagLayoutAlgorithms.setConstraints(jCheckBoxWAnalysis, gridconstrainer(0,10,10,10,1,1,1,0));
        panelabove.add(jCheckBoxWAnalysis);
        gridBagLayoutAlgorithms.setConstraints(checksvd, gridconstrainer(10,10,10,10,1,1,0,1));
        panelabove.add(checksvd);
        
		add(panelabove);
		
		
		//panel tools
		paneltools.setBorder(new TitledBorder("Tools"));
		gridBag0.setConstraints(paneltools, gridconstrainer(5,5,5,5,2,1,0,1));
		paneltools.setLayout(gridBagt);
		gridBagt.rowWeights = new double[] {1,1,1,1};
		gridBagt.rowHeights = new int[] {30,30,30,30,30};
		gridBagt.columnWeights = new double[] {1,1};
		gridBagt.columnWidths = new int[] {150,150};
		
		gridBagt.setConstraints(tunelabel, gridconstrainer(0,10,10,10,1,1,0,0));
		paneltools.add(tunelabel);
		gridBagt.setConstraints(tunebutton, gridconstrainer(0,10,10,10,1,1,1,0));
		paneltools.add(tunebutton);
		gridBagt.setConstraints(kicklabel, gridconstrainer(10,10,10,10,1,1,0,1));
		paneltools.add(kicklabel);
		gridBagt.setConstraints(kickbutton, gridconstrainer(10,10,10,10,1,1,1,1));
		paneltools.add(kickbutton);	
		gridBagt.setConstraints(parallel, gridconstrainer(10,10,10,10,1,1,0,2));
		paneltools.add(parallel);
		gridBagt.setConstraints(parallelSettings, gridconstrainer(10,10,10,10,1,1,1,2));
		paneltools.add(parallelSettings);		
		gridBagt.setConstraints(automatic, gridconstrainer(10,10,0,10,1,1,0,3));
		paneltools.add(automatic);
		gridBagt.setConstraints(autamaticSettings, gridconstrainer(10,10,0,10,1,1,1,3));
		paneltools.add(autamaticSettings);		
		
		add(paneltools);
		
		// panel input
		panelinput.setBorder(new TitledBorder("Input"));
		gridBag0.setConstraints(panelinput, gridconstrainer(5,5,5,5,2,1,0,2));
		panelinput.setLayout(gridBagi);
		gridBagi.rowWeights = new double[] {1,1,1,1,1};
		gridBagi.rowHeights = new int[] {30,30,30,30,30,30};
		gridBagi.columnWeights = new double[] {1,1,1,1};
		gridBagi.columnWidths = new int[] {75,75,75,75};
		
		gridBagi.setConstraints(tunexlabel, gridconstrainer(0,10,10,0,1,1,0,0));
		panelinput.add(tunexlabel);
		gridBagi.setConstraints(tunexfield, gridconstrainer(0,0,10,10,1,1,1,0));
		panelinput.add(tunexfield);
		gridBagi.setConstraints(tuneylabel, gridconstrainer(10,10,10,0,1,1,0,1));
		panelinput.add(tuneylabel);
		gridBagi.setConstraints(tuneyfield, gridconstrainer(10,0,10,10,1,1,1,1));
		panelinput.add(tuneyfield);
		gridBagi.setConstraints(startlabel, gridconstrainer(10,10,10,0,1,1,0,2));
		panelinput.add(startlabel);
		gridBagi.setConstraints(startfield, gridconstrainer(10,0,10,10,1,1,1,2));
		panelinput.add(startfield);
		gridBagi.setConstraints(endlabel, gridconstrainer(10,10,10,0,1,1,0,3));
		panelinput.add(endlabel);
		gridBagi.setConstraints(endfield, gridconstrainer(10,0,10,10,1,1,1,3));
		panelinput.add(endfield);	
		gridBagi.setConstraints(seplabel, gridconstrainer(10,10,0,0,1,1,0,4));
		panelinput.add(seplabel);
		gridBagi.setConstraints(sepfield, gridconstrainer(10,0,0,10,1,1,1,4));
		panelinput.add(sepfield);

		gridBagi.setConstraints(a1label, gridconstrainer(10,10,10,0,1,1,2,1));
		panelinput.add(a1label);
		gridBagi.setConstraints(a1field, gridconstrainer(10,0,10,10,1,1,3,1));
		panelinput.add(a1field);
		gridBagi.setConstraints(a2label, gridconstrainer(10,10,10,0,1,1,2,2));
		panelinput.add(a2label);
		gridBagi.setConstraints(a2field, gridconstrainer(10,0,10,10,1,1,3,2));
		panelinput.add(a2field);
		gridBagi.setConstraints(b1label, gridconstrainer(10,10,10,0,1,1,2,3));
		panelinput.add(b1label);
		gridBagi.setConstraints(b1field, gridconstrainer(10,0,10,10,1,1,3,3));
		panelinput.add(b1field);	
		gridBagi.setConstraints(b2label, gridconstrainer(10,10,0,0,1,1,2,4));
		panelinput.add(b2label);
		gridBagi.setConstraints(b2field, gridconstrainer(10,0,0,10,1,1,3,4));
		panelinput.add(b2field);
		add(panelinput);		
		
		//panel turns
		panelturns.setBorder(new TitledBorder("Turns"));	
		gridBag0.setConstraints(panelturns, gridconstrainer(5,5,5,5,2,1,0,3));
		panelturns.setLayout(gridBagtu);
		gridBagtu.rowWeights = new double[] {1};
		gridBagtu.rowHeights = new int[] {30};
		gridBagtu.columnWeights = new double[] {1,1,1,1};
		gridBagtu.columnWidths = new int[] {75,75,75,75};
		gridBagtu.setConstraints(labelturn0, gridconstrainer(5,0,5,0,1,1,0,0));
		panelturns.add(labelturn0);
		gridBagtu.setConstraints(kickfield, gridconstrainer(5,0,5,0,1,1,1,0));
		panelturns.add(kickfield);	
		gridBagtu.setConstraints(labelturn1, gridconstrainer(5,0,5,0,1,1,2,0));
		panelturns.add(labelturn1);
		gridBagtu.setConstraints(turn1field, gridconstrainer(5,0,5,0,1,1,3,0));
		panelturns.add(turn1field);		
		add(panelturns);
		
		// panel button	
		gridBag0.setConstraints(buttongo, gridconstrainer(5,5,5,5,1,1,0,4));
		add(buttongo);
		buttongo.setBackground(Color.GREEN);
		gridBag0.setConstraints(buttoncancel, gridconstrainer(5,5,5,5,1,1,1,4));
		add(buttoncancel);
		buttoncancel.setBackground(Color.RED);
		
		parallel.setSelected(true);
		
		
		setSize(new Dimension(450,700));
		setTitle("Analysis Selection Panel");
	}
	
	private GridBagConstraints gridconstrainer(int top,int left,int bottom,int right,int gridwidth,int gridheight,int gridx,int gridy){
		GridBagConstraints gridCons = new GridBagConstraints();
		gridCons.insets = new Insets(top,left,bottom,right);
		gridCons.anchor = GridBagConstraints.CENTER;
		gridCons.gridwidth = gridwidth;
		gridCons.gridheight =gridheight;
		gridCons.gridx = gridx;
		gridCons.gridy = gridy;
		gridCons.fill=GridBagConstraints.BOTH;
		
		return gridCons;
	}
	
	public void showGUI(){
		setVisible(true);
	}
	
	public void setAnalysis(AnalysisPanel temppanel){
		apanel=temppanel;
	}
}
