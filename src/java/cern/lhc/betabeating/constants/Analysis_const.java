package cern.lhc.betabeating.constants;

import java.util.HashMap;

public class Analysis_const {
	
	private HashMap<String,String> mapsps =new HashMap<String, String>();
	private HashMap<String,String> maplhc =new HashMap<String, String>();
	
	public Analysis_const(){
		
	}
	
	public HashMap<String,String> LHC(){
		
		maplhc.put("tunex", "0.31");
		maplhc.put("tuney", "0.32");
		maplhc.put("kick", "1");
		maplhc.put("istun", "0.01");
		maplhc.put("sbpm", "0");	
		maplhc.put("ebpm", "538");
		maplhc.put("a1", "0.04");
		maplhc.put("a2", "0.1");
		maplhc.put("b1", "0.4");
		maplhc.put("b2", "0.45");
		maplhc.put("eturn", "1900");	
		maplhc.put("tunexd", "0.304");
		maplhc.put("tuneyd", "0.326");
		
		
		return maplhc;
		
	}
	
	public HashMap<String,String> SPS(){
		mapsps.put("tunex", "0.31");
		mapsps.put("tuney", "0.32");
		mapsps.put("kick", "1");
		mapsps.put("istun", "0.01");
		mapsps.put("sbpm", "0");	
		mapsps.put("ebpm", "538");
		mapsps.put("a1", "0.04");
		mapsps.put("a2", "0.1");
		mapsps.put("b1", "0.4");
		mapsps.put("b2", "0.45");
		mapsps.put("eturn", "1900");		
		
		return mapsps;
	}

}
