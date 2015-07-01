package cern.lhc.betabeating.modelcreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.accsoft.gui.frame.Task;
import cern.lhc.betabeating.external.Systemcall;
import cern.lhc.betabeating.frames.Controller;

public class Madx_class {
    private static final Logger log = Logger.getLogger(Madx_class.class);
	
    private Controller controller = null;
	private String bbdirpath;
	private String fulldirpath;
	
	public Madx_class(Controller controller){
		this.controller = controller;
		bbdirpath = controller.getBeamSelectionData().getProgramLocation() + "/MODEL/"+ controller.getBeamSelectionData().getAccelerator().replace("LHCB1", "LHCB").replace("LHCB2", "LHCB")+"/model/";
		fulldirpath = controller.getBeamSelectionData().getProgramLocation() +"/MODEL/"+ controller.getBeamSelectionData().getAccelerator().replace("LHCB1", "LHCB").replace("LHCB2", "LHCB")+"/fullresponse/";
	}
	
	/*
	 * checker for madx
	 */
	private String checker(String str){
		str=str.replace("%QMX", controller.getPathDataForKey("%QMX"));
		str=str.replace("%QMY", controller.getPathDataForKey("%QMY"));		
		str=str.replace("%ACCEL", controller.getBeamSelectionData().getAccelerator());
		str=str.replace("%PATH", controller.getPathDataForKey("%PATH"));
		str=str.replace("%BEAM", controller.getPathDataForKey("%BEAM"));
		str=str.replace("%QDY", controller.getPathDataForKey("%QDY"));
		str=str.replace("%QY", controller.getPathDataForKey("%QY"));
		str=str.replace("%QDX", controller.getPathDataForKey("%QDX"));
		str=str.replace("%QX", controller.getPathDataForKey("%QX"));
		str=str.replace("%STOP", controller.getPathDataForKey("%STOP"));
		str=str.replace("%INCLUDE", controller.getPathDataForKey("%INCLUDE"));
		str=str.replace("%DPP", controller.getPathDataForKey("%DPP"));
		str=str.replace("%MATCHER", controller.getPathDataForKey("%MATCHER"));
		
		return str;
	}
	/*
	 * checker for full response
	 */
	private String checker2(String str){
		
		str=str.replace("%PATH", controller.getPathDataForKey("%PATH"));
		str=str.replace("%BEAM", controller.getPathDataForKey("%BEAM"));
		str=str.replace("%ACCEL", controller.getBeamSelectionData().getAccelerator());
		str=str.replace("%MATCHER", controller.getPathDataForKey("%MATCHER"));
		
		return str;
	}	
	
	private Task task;
	private BufferedWriter out;
	private BufferedReader in;
	private String command;
	
	public void writemadx(String path,String[] env){
			try {
				// main madx
				out = new BufferedWriter(new FileWriter(path+"/job.twiss.madx"));
			    in = new BufferedReader(new FileReader(bbdirpath+"/job.twiss_java.madx"));
			    String str;
			    while ((str = in.readLine()) != null) {
			    	str=checker(str);
			    	out.write(str+"\n");
			    }
			    in.close();
			    out.close();
			    
			    // modifiers
					out = new BufferedWriter(new FileWriter(path+"/modifiers.madx"));
					out.write("call file=\""+controller.getPathDataForKey("STR")+"\";\n");
					if(!controller.getPathDataForKey("IP1").contains("injection")){
						out.write("call file=\""+controller.getPathDataForKey("IP1")+"\";\n");
					}
					
					if(!controller.getPathDataForKey("IP2").contains("injection")){					
						out.write("call file=\""+controller.getPathDataForKey("IP2")+"\";\n");
					}
					
					if(!controller.getPathDataForKey("IP5").contains("injection")){						
						out.write("call file=\""+controller.getPathDataForKey("IP5")+"\";\n");
					}
					
					if(!controller.getPathDataForKey("IP8").contains("injection")){
						out.write("call file=\""+controller.getPathDataForKey("IP8")+"\";\n");
					}
					out.close();
			  
				//command
				out = new BufferedWriter(new FileWriter(path+"/command"));
				out.write(controller.getPathDataForKey("madx")+" < "+path+"/job.twiss.madx\n");				
				out.close();
				
				command="chmod 777 "+path+"/command";
				
				runMadx(path+"/command",command,env,path);
				controller.setKeyWithPathData("%INCLUDE","!");
				
			} catch (IOException e) {
				log.info(e);
			}
			
	}
		
	private void runMadx(final String madXCommand,final String madXCommand2,final String[] env,final String pathpath){
		task = new Task() {
			protected Object construct() {
			try{
					MessageManager.getConsoleLogger().info("Model Creator => command send to the machine "+command);
					Systemcall.execute(madXCommand2,"madx for model creator",env,pathpath,false); //order is important! :/ (tbach)
					Systemcall.execute(madXCommand,"madx for model creator",env,pathpath,true);
			}catch(Exception ex){
				ex.printStackTrace();
				MessageManager.error("Model Creator => Caused by exception: ",ex,null);
				MessageManager.error("Model Creator => Error",null,null);
			}
			
			return null;
			}
			
			};
			task.setName("Model creator!");
			task.setCancellable(false);
			task.start();
	}
	
	/*
	 * run full response
	 */
	private Task task2;
	private BufferedWriter out2;
	private BufferedReader in2;
	private String command2;
	
	public void runFullresponse(final String path, final String[] env, final String incrementK){
		try {
			
			// main madx
			out2 = new BufferedWriter(new FileWriter(path+"/job.iterate.madx"));
		    in2 = new BufferedReader(new FileReader(fulldirpath+"/job.iterate_java.madx"));
		    String str;
		    while ((str = in2.readLine()) != null) {
		    	str=checker2(str);
		    	out2.write(str+"\n");
		    }
		    in2.close();
		    out2.close();
		    		    			
			command2=controller.getPathDataForKey("python")+" "+fulldirpath+"/generateFullResponse.py"+" -a " + controller.getBeamSelectionData().getAccelerator() + " -p "+path+"/ -c "+fulldirpath + " -k " + incrementK;
			
			task2 = new Task() {
				protected Object construct() {
				try{
						MessageManager.getConsoleLogger().info("Model Creator => command send to the machine (full response) "+command);
						Systemcall.execute(command2,"madx for model creator",env,path,true);
						MessageManager.info("Model Creator => Creating full response finished!",null);
				}catch(Exception ex){
					ex.printStackTrace();
					MessageManager.error("Model Creator => Caused by exception: ",ex,null);
					MessageManager.error("Model Creator => Error",null,null);
				}
				
				return null;
				}
				
				};
				task2.setName("Model creator! => full response");
				task2.setCancellable(false);
				task2.start();
			
			
		} catch (IOException e) {
			log.info(e);
		}
	}
}
