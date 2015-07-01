package cern.lhc.betabeating.constants;

public class MagnetsClassification {
	
	
	private String[] temp; 
	
	/*
	 * normal quad lhc
	 */	
	public String[] normalquadsLHC(){
		temp=new String[9]; 		
		temp[0]="MQ";
		temp[1]="MQM";
		temp[2]="MQT";
		temp[3]="MQTL";
		temp[4]="MQW";
		temp[5]="MQY";
		temp[6]="MQX";
		temp[7]="Q";
		temp[8]="bumps";	
		
		return temp;
	}
	
	/*
	 * skew quad LHC
	 */
	public String[] skewquadsLHC(){
		temp=new String[7];
		temp[0]="MQS";
		temp[1]="MQSX";
		temp[2]="Qs";
		temp[3]="MQSl";
		temp[4]="MQSr";
		temp[5]="MQSa";
		temp[6]="bumps";
		
		return temp;		
	}
	
	/*
	 * normal quad SPS
	 */
	public String[] normalquadsSPS(){
		temp=new String[1]; 	
		temp[0]="varsSPS";
		
		return temp;
	}	
	
	/*
	 * skew quad SPS
	 */
	public String[] skewquadsSPS(){	
		temp=new String[1]; 
		temp[0]="squadvarsSPS";	
		
		return temp;		
	}
	
	/*
	 * settings LHC coupling
	 */
	public String[] settingsLHCcoupling(){
		
		temp = new String[8];
		
		temp[0]="0";
		temp[1]="0.02";
		temp[2]="0.1";
		temp[3]="0.00003";
		temp[4]="0.01";
		temp[5]="0.1";
		temp[6]="0.01";
		temp[7]="5";		
		
		return temp;
	}
	
	/*
	 * settings LHC normal phase
	 */
	public String[] settingsLHCnormalphase(){
		
		temp = new String[8];
		
		temp[0]="3";
		temp[1]="0.02";
		temp[2]="0.1";		
		temp[3]="0.00003";
		temp[4]="0.01";
		temp[5]="0.1";		
		temp[6]="0.01";
		temp[7]="5";		
		
		return temp;
	}
	
	/*
	 * settings LHC normal beta
	 */
	public String[] settingsLHCnormalbeta(){
		
		temp = new String[8];
		
		temp[0]="3";
		temp[1]="20";
		temp[2]="0.1";		
		temp[3]="0.00003";
		temp[4]="3";
		temp[5]="0.1";		
		temp[6]="0.01";
		temp[7]="5";		
		
		return temp;
	}

}
