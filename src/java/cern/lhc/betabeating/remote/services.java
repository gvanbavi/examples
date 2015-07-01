package cern.lhc.betabeating.remote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;
import cern.lhc.betabeating.constants.Correctors;
import cern.lsa.client.ClientContextController;
import cern.lsa.client.ClientException;
import cern.lsa.client.ClientGenerationController;
import cern.lsa.client.ClientKnobController;
import cern.lsa.client.ClientOpticsController;
import cern.lsa.client.ClientParameterController;
import cern.lsa.client.ClientRunControlController;
import cern.lsa.client.ServiceLocator;
import cern.lsa.optics.domain.KnobComponent;
import cern.lsa.optics.domain.KnobComponents;
import cern.lsa.optics.domain.OpticsTableItem;
import cern.lsa.settings.domain.Knob;
import cern.lsa.settings.domain.KnobValues;
import cern.lsa.settings.domain.Parameter;
import cern.lsa.settings.domain.StandAloneBeamProcess;
import cern.lsa.settings.domain.factory.KnobValuesBuilder;


public class services {
    private static final Logger log = Logger.getLogger(services.class);
	
	/**
	 * lsa controllers
	 */
	private static ClientRunControlController lsaClientRunControlController;
	private ClientOpticsController lsaClientOpticsController;
	private static ClientContextController contextController;
	private static ClientGenerationController lsaClientGenerationController;
	private ClientParameterController lsaClientParameterController;
	private ClientKnobController knobController;
	
	private Correctors cor = new Correctors();
	
	/*
	 * 
	 */
	private String accel;
	
	public services(String acceltmp){
		accel=acceltmp;
		if(accel.contains("LHC")){
			accel="LHC";
		}else{
			accel="SPS";
		}
		
		System.setProperty("lsa.server",accel);
		contextController = ServiceLocator.getService(ClientContextController.class);
		lsaClientRunControlController= ServiceLocator.getService(ClientRunControlController.class);
		lsaClientOpticsController = ServiceLocator.getService(ClientOpticsController.class);
		lsaClientParameterController = ServiceLocator.getService(ClientParameterController.class);
		lsaClientGenerationController= ServiceLocator.getService(ClientGenerationController.class);
		knobController = cern.lsa.client.ServiceLocator.getService(ClientKnobController.class);
		
		if(accel.equals("SPS")){
			prefix="SPSBEAM/";
		}else{
			prefix="LHCBEAM/";
		}
		
	}
	
	/**
	 * General
	 */

	public int getFillNumber(){
		
		return lsaClientRunControlController.findCurrentFillNumber();
	}
	
	/*
	 * Find beam process
	 */
	private ArrayList<StandAloneBeamProcess> beamprocesses;
	
	public ArrayList<StandAloneBeamProcess> getbeamprocesses(){
		
		beamprocesses = new ArrayList<StandAloneBeamProcess>();
		
		StandAloneBeamProcess[] process=contextController.findBeamProcesses(accel,null);

		for(int i=0;i<process.length;i++){
			//if(process[i].isActual() &&  process[i].isResident()){
			//  if(process[i].isActual()){
				beamprocesses.add(process[i]);
			//}
		}
		
		Collections.sort(beamprocesses);
		
		return beamprocesses;
	}
	
	/*
	 * get optic
	 */
	private OpticsTableItem[] opticstable=null;
	
	public OpticsTableItem[] getOptic(String name){
		
		for(int i=0;i<beamprocesses.size();i++){
			if(beamprocesses.get(i).getName().equals(name)){
				actualbeamProcess=beamprocesses.get(i);
				break;
			}
		}
		
		
		if(actualbeamProcess!=null){
		
		    if (actualbeamProcess.isActual() == true) {
		    	sourceBeampro = actualbeamProcess.getActualBeamProcessInfo().getSourceBeamProcess();
			} else {
			    sourceBeampro = actualbeamProcess;
			}
						
			opticstable=lsaClientGenerationController.findBeamProcessTypeOpticTable(sourceBeampro.getTypeName());
		
		}else{
			MessageManager.error("Services => Beam process not found for optics "+name, null, null);
			MessageManager.getConsoleLogger().error("Services => Contact expert : beam process not found in internal list");
		}
		
		return opticstable;
		
	}
	
	/*
	 * create knob
	 */
	public String prefix;
	private Map<String,String> translatedmap;
	
	//protected KnobPersister knobPersister;
	
	public void createKnob(String selbp,String knobname, Object[] opticname, String[] names, double[] values,String method){
		
		//adding prefix
		knobname=prefix+knobname;
		
		
		//translating the knob
		translatedmap=translateknobs(names,method);
		
		//check
		if(names.length==translatedmap.size()){
			
			KnobValuesBuilder builder =new KnobValuesBuilder();
			
			KnobValues knobValues;
			for(Object optic:opticname){
				MessageManager.getConsoleLogger().info("Services =>  Adding the optics function "+optic.toString());
				for(int i=0;i<names.length;i++){
					builder.addValue(translatedmap.get(names[i]),  optic.toString(),     values[i]);
				}
			}
			knobValues=builder.build();
			
			MessageManager.info("Services => Creating knob "+knobname,null);
			Knob knob = knobController.createKnob(knobname,"BETA-BEATING" , knobValues);
			MessageManager.info("Services => Creating knob finished :-)",null);

		
		
		}else{
			MessageManager.error("Services => Number of correctors is not correct", null, null);
			MessageManager.getConsoleLogger().error("Services => The number of translated correctors is not equal to input \n Contact expert \n" +
					" Knob will not be created !");
		}
		
	}
	
	/*
	 * generate knob
	 */
	private StandAloneBeamProcess sourceBeampro;
	private Parameter  parametergen;
	private Parameter[] parameterlist;
	private StandAloneBeamProcess actualbeamProcess=null;
	
	
	public void generateknob(String selbp,String knobName){
		
		for(int i=0;i<beamprocesses.size();i++){
			if(beamprocesses.get(i).getName().equals(selbp)){
				actualbeamProcess=beamprocesses.get(i);
				break;
			}
		}
		
		
		if(actualbeamProcess!=null){
		    
		    parametergen=lsaClientParameterController.findParameterByName(knobName);
		    
			parameterlist= new Parameter[1];
			parameterlist[0]=parametergen;
			
        	try {
        		MessageManager.info("Services => Will Generate knob",null);
        		MessageManager.getConsoleLogger().info("Services => Will generate knob : "+knobName);
				lsaClientGenerationController.generateSettings(sourceBeampro,parameterlist , true, true, false);
				
			} catch (ClientException e) {
				e.printStackTrace();
				MessageManager.error("Services => Cannot generate knob", null, null);
				MessageManager.getConsoleLogger().error("Services => Cannot generate knob", e);
			}
			
		}else{
			MessageManager.error("Services => Beam process not found "+selbp, null, null);
			MessageManager.getConsoleLogger().error("Services => Contact expert : beam process not found in internal list");
		}
	}
		
	/**
	 * Knob business
	 */
	/*
	 * translate knobs
	 */
    public Map<String, String> translateknobs(String[] names, String method) {
        Map<String, String> translationmap = new HashMap<String, String>();

        Map<String, String> specialmap;
        String ext;
        if (method.equals("coupling")) {
            specialmap = cor.TriSkewquads();
            ext = "/K1S";
        } else {
            specialmap = cor.Triquads();
            ext = "/K1";
        }

        ArrayList<String> send2lsa = new ArrayList<String>();

        for (int i = 0; i < names.length; i++) {
            MessageManager.getConsoleLogger().info("Services => Looking for name  " + names[i]);
            if (specialmap.containsKey(names[i])) {
                translationmap.put(names[i], specialmap.get(names[i]));
                MessageManager.getConsoleLogger().info("Services => Home translated  " + specialmap.get(names[i]));
            } else {
                send2lsa.add(names[i]);
            }
        }

        Map<String, String> maps = lsaClientParameterController.findLogicalNamesByMadStrengthNames(send2lsa.toArray(new String[] {}));

        if (log.isInfoEnabled())
            for (Entry<String, String> entry : maps.entrySet())
                log.info("key: " + entry.getKey() + ", value: " + entry.getValue());
        for (String send2lsaItem : send2lsa) {
            if (log.isInfoEnabled())
                log.info("-- string2lsaItem: " + send2lsaItem);
            if (maps.get(send2lsaItem).contains("RCO")) {
                translationmap.put(send2lsaItem, maps.get(send2lsaItem) + "/K3");
                MessageManager.getConsoleLogger().info("Services => " + send2lsaItem + " octupolar");
            } else if (maps.get(send2lsaItem).contains("RCD")) {
                translationmap.put(send2lsaItem, maps.get(send2lsaItem) + "/K4");
                MessageManager.getConsoleLogger().info("Services => " + send2lsaItem + " decapole");
            } else {
                translationmap.put(send2lsaItem, maps.get(send2lsaItem) + ext);
            }
        }
        return translationmap;
    }
	
	/*
	 * find knobs
	 */
	public ArrayList<String> getknobs(){
	    ArrayList<String> knobs=new ArrayList<String>();
		
	    String[] names=lsaClientOpticsController.getKnobNames(accel);
		
		for(int i=0;i<names.length;i++){
			
		    Parameter para =  lsaClientParameterController.findParameterByName(names[i]);
			
			if(Arrays.asList(para.getSystems()).contains("BETA-BEATING")){
				knobs.add(names[i]);
			}
		}
		return knobs;
	}
	
	/*
	 * remove knobs
	 */
	public void removeknob(String knobknob){
		Parameter knob= lsaClientParameterController.findParameterByName(knobknob);
		lsaClientParameterController.deleteParameters(knob.getName());
	}
	
	/*
	 * view knob
	 */
	public HashMap<String,Double> viewknob(String knobname){
	    HashMap<String,Double> knobvalues= new HashMap<String, Double>();
		String zone;
		if(accel.equals("SPS")){
			zone="SPSRING";
		}else{
			zone="LHCRING";
		}
		
		
		 Parameter knob4setting= lsaClientParameterController.findParameterByName(prefix+knobname);
		 KnobComponents knobComponents = lsaClientOpticsController.findParticleTransferKnobComponents(zone);
		// knobComponents.getKnobNameOpticNameKnobComponents(knob4setting.getName(),knobComponents.getKnobOptics(knobname)[0]);
		 KnobComponent[] setting=knobComponents.getKnobNameKnobComponents(knob4setting.getName());
		 
		 for(int i=0;i<setting.length;i++){
			 knobvalues.put(setting[i].getComponentName(),setting[i].getValue());
		 }
		
		return knobvalues;
	}
	
	public static void main (String[] args){
		new services("LHC");
	}
	
}
