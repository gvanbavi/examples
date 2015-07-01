package cern.lhc.betabeating.constants;

import java.util.HashMap;

public class Segment {
	
	// LHCB1
	private HashMap<String,String[]> mapb1 = new HashMap<String, String[]>();
	public HashMap<String,String[]> LHCB1(){
		
		mapb1.put("IP1", new String[]{"BPM.8L1.B1","BPM.8R1.B1"});
		mapb1.put("IP2", new String[]{"BPM.8L2.B1","BPM.8R2.B1"});
		mapb1.put("IP3", new String[]{"BPM.8L3.B1","BPM.8R3.B1"});
		mapb1.put("IP4", new String[]{"BPM.8L4.B1","BPM.8R4.B1"});
		mapb1.put("IP5", new String[]{"BPM.8L5.B1","BPM.8R5.B1"});
		mapb1.put("IP6", new String[]{"BPM.8L6.B1","BPM.8R6.B1"});
		mapb1.put("IP7", new String[]{"BPM.8L7.B1","BPM.8R7.B1"});
		mapb1.put("IP8", new String[]{"BPM.8L8.B1","BPM.8R8.B1"});
		
		return mapb1;
	}
	
	// LHCB2
	private HashMap<String,String[]> mapb2 = new HashMap<String, String[]>();
	public HashMap<String,String[]> LHCB2(){
		
		mapb2.put("IP1", new String[]{"BPM.8L1.B2","BPM.8R1.B2"});
		mapb2.put("IP2", new String[]{"BPM.8L2.B2","BPM.8R2.B2"});
		mapb2.put("IP3", new String[]{"BPM.8L3.B","BPM.8R3.B2"});
		mapb2.put("IP4", new String[]{"BPM.8L4.B2","BPM.8R4.B1"});
		mapb2.put("IP5", new String[]{"BPM.8L5.B2","BPM.8R5.B2"});
		mapb2.put("IP6", new String[]{"BPM.8L6.B2","BPM.8R6.B2"});
		mapb2.put("IP7", new String[]{"BPM.8L7.B2","BPM.8R7.B2"});
		mapb2.put("IP8", new String[]{"BPM.8L8.B2","BPM.8R8.B2"});
		
		return mapb2;
	}

}
