package cern.lhc.betabeating.jmad;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Logger;

import cern.accsoft.steering.jmad.domain.ex.JMadModelException;
import cern.accsoft.steering.jmad.domain.result.tfs.TfsResultRequestImpl;
import cern.accsoft.steering.jmad.domain.var.enums.MadxTwissVariable;
import cern.accsoft.steering.jmad.model.JMadModel;
import cern.accsoft.steering.jmad.model.JMadModelStartupConfiguration;
import cern.accsoft.steering.jmad.modeldefs.domain.JMadModelDefinition;
import cern.accsoft.steering.jmad.modeldefs.domain.OpticsDefinition;
import cern.accsoft.steering.jmad.service.JMadService;
import cern.accsoft.steering.jmad.service.JMadServiceFactory;

import cern.lhc.betabeating.constants.madx_const;

public class JmadClass {
    private static final Logger log = Logger.getLogger(JmadClass.class);
	private JMadService service = JMadServiceFactory.createJMadService();
	
	private madx_const mcon = new madx_const();
	
	public JMadModelDefinition getLHCOptics(){
	    JMadModelDefinition modelDefinition = null;
	//	modelDefinition=service.getModelDefinitionManager().getModelDefinition(LhcUtil.MODEL_DEFINITION_NAME);
	
		return modelDefinition;
	}
	
	public void runModelWithSelectedOptics(JMadModelDefinition modelDefinition,
			OpticsDefinition selectedOpticsDefinition,File filename) throws JMadModelException{
	
		JMadModel model = service.createModel(modelDefinition);			
    	JMadModelStartupConfiguration startupConfiguration = new JMadModelStartupConfiguration();
    	
    	startupConfiguration.setInitialOpticsDefinition(selectedOpticsDefinition);
    	model.setStartupConfiguration(startupConfiguration);
    	
    	model.init();
    	WriteTwissToFile("monitors",filename,model);
    	model.cleanup();
	}
	
	
	private void WriteTwissToFile(String option,File filename,JMadModel model){
		String[] var=mcon.madxvariables();
		MadxTwissVariable[] variables = new MadxTwissVariable[var.length];
		
		for(int i=0;i<var.length;i++){
			variables[i]=MadxTwissVariable.fromMadxName(var[i].toUpperCase());
			log.info(variables[i]);
		}
		
		String filter=null;
		if(option.equals("monitors")){
			filter="BPM";
		}else{
			filter=".*";
		}
		
		TfsResultRequestImpl requestImpl = new TfsResultRequestImpl();
        requestImpl.addElementFilter(filter);
        requestImpl.addVariables(Arrays.asList(variables));
        try {
            model.twissToFile(requestImpl, filename);
        } catch (JMadModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
}
