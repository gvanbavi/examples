package cern.lhc.betabeating.datahandler;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * class to read in TFS tables
 * 
 * @author iagapov
 *
 */
public class TFSReader extends DataReader {
    private static final Logger log = Logger.getLogger(TFSReader.class);
    
    private HashMap<String, Integer> keys;
    private ArrayList<String> keyNames;
    private ArrayList<Integer> columnTypesMap;
    
    private HashMap<String, Integer> types;
    private HashMap<String,String> globalParameters;
    private ArrayList<double[]> doubleData; // data stored in rows
    private ArrayList<String[]> stringData; // data stored in rows
    
	public TFSReader() {
		keys = new HashMap<String, Integer>();
		keyNames = new ArrayList<String>();
		columnTypesMap = new ArrayList<Integer>();
		doubleData = new ArrayList<double[]>();
		stringData = new ArrayList<String[]>();
		types = new HashMap<String, Integer>();
		globalParameters = new HashMap<String,String>();
	}
	
	
	/** list all tfs files in a directory */
	public static String[] getTfsTableNames(String path) {
		
		File f = new File(path);
		String[] names = f.list( new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return dir.isDirectory() && name.toLowerCase().endsWith(".tfs");
			}
		});
		
		
		return names;
	}
	
	
	/** read in a tfs table */
	public void loadTable(String path) {
	    File file = new File(path);
	    if (!file.exists())
	    {
	        if (log.isInfoEnabled())
	            log.warn("-- file not found, skip this one: " + path); //not important, happens for different reasons (tbach)
	        return;
	    }
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			
			keys = new HashMap<String, Integer>();
	        keyNames = new ArrayList<String>();
	        columnTypesMap = new ArrayList<Integer>();
	        doubleData = new ArrayList<double[]>();
	        stringData = new ArrayList<String[]>();
	        types = new HashMap<String, Integer>();
	        globalParameters = new HashMap<String,String>();

			int nColumnsD = 0;
			int nColumnsS = 0;
			
			boolean keysRead = false;
			boolean typesRead = false;
			
			// get column keys and types
			while( line != null) {
				line = br.readLine();
				
				if( line == null) break;
				
				String[] tokens = line.trim().split("[ \t\r]+");
				
				if( tokens.length > 0) {
					if( tokens[0].equalsIgnoreCase("*")) { // keys column
						
						for( int i = 1 ; i < tokens.length; i++) {
							
								keyNames.add(tokens[i]);
							
							}
			
						keysRead = true;
					}
					
					if( tokens[0].equalsIgnoreCase("$")) { // types column
						
						for( int i = 1 ; i < tokens.length; i++) {
							
							
							if (tokens[i].equalsIgnoreCase("%le")) {
								types.put(keyNames.get(i-1), DataReader.DOUBLE_TYPE);
								columnTypesMap.add(DataReader.DOUBLE_TYPE);
							}
							else {
								types.put(keyNames.get(i-1), DataReader.STRING_TYPE);
								columnTypesMap.add(DataReader.STRING_TYPE);
							}
						}
						
						typesRead = true;
					}
					
				}
				
				if( tokens.length > 2) {
					if( tokens[0].equalsIgnoreCase("@")) { // keys column
						this.addGlobalParameter(tokens[1], tokens[3]);
					}
				}
				
				if (keysRead && typesRead) break;
				
			}
			// create the type mapping
			int sid = 0;
			int did = 0;
			
			
			for( String key: keyNames) {
				
				int type = types.get(key);
				
				if ( type == DataReader.DOUBLE_TYPE ) {
					keys.put(key,did);
					did++;
				}
				
				if ( type == DataReader.STRING_TYPE ) {								
					keys.put(key,sid);
					sid++;
				}
				
					
			}

			
			
			nColumnsS = sid;
			nColumnsD = did;
			
			
			int nColumns = keyNames.size();
			
			// read in data
			
			while ( line != null ) {
				line = br.readLine();
				if( line == null) break;
				String[] tokens = line.trim().split("[ \t\r]+");
				
				
				if ( tokens.length != nColumns) {
					log.error("ERROR: number of columns " + tokens.length +  
							" does not match the number of keys " + nColumns);
				} else {
					
					double[] rowD = new double[nColumnsD];
					String[] rowS = new String[nColumnsS];
					
					sid = 0;
					did = 0;
					
					for( int i = 0; i < nColumnsD + nColumnsS; i++ ) {
													
						if (columnTypesMap.get(i) == DataReader.DOUBLE_TYPE) {
						
							try {
								rowD[did] = Double.parseDouble(tokens[i]);
							} catch (Exception e) {
								rowD[did] = 0;
							}
							
							did++;
							
						}
					
						if (columnTypesMap.get(i) == DataReader.STRING_TYPE) {
							rowS[sid] = tokens[i];
					
							sid++;
						}
					
					}
					
					doubleData.add(rowD);
					stringData.add(rowS);
				}
				
			}
			
			
		} catch (Exception e) {
			log.warn("problemo loading table from file " + path, e);
		} 
	}
	
	
	public double[] getDoubleData(String key) {
		double[] res = new double[ doubleData.size()];
		
		int idx = -1;
		int type = DataReader.UNKNOWN_TYPE;
		
		if (keys.containsKey(key) ) {
			idx = keys.get(key); 
			type = types.get(key);			
		}
			
		if ( type == DataReader.DOUBLE_TYPE) {
		
			if ( idx >= 0 ) {
		
				for( int i = 0; i < doubleData.size(); i++) {
					res[i] = doubleData.get(i)[idx];
				}
				
			}
		}
		
		return res;
	}
	
	public String[] getStringData(String key) {
		String[] res = new String[ stringData.size()];
		
		int idx = -1;
		int type = DataReader.UNKNOWN_TYPE;
		
		if (keys.containsKey(key) ) {
			idx = keys.get(key); 
			type = types.get(key);			
		}
			
		if ( type == DataReader.STRING_TYPE) {
		
			if ( idx >= 0 ) {
		
				for( int i = 0; i < stringData.size(); i++) {
					res[i] = stringData.get(i)[idx].replace("\"","");
				}
				
			}
		}
		
		return res;
	}
	
	public ArrayList<String> getKeys() {
		//return new ArrayList<String>(keys.keySet());
		return keyNames;
	}
	
	public int getKeyType(String key) {
		return types.get(key);
	}
	
	
	public int getGlobalParameterType(String param) {
		log.info("parameter in reader : "+ param);
		int type = -1;
		if(param.equals("STRENGTH")){
			 type=DOUBLE_TYPE;
		}else if(param.equals("DPP")){
			 type= DOUBLE_TYPE;
		}else if(param.equals("Q1")){
			 type= DOUBLE_TYPE;
		}else if(param.equals("Q1RMS")){
			 type= DOUBLE_TYPE;
		}else if(param.equals("Q2")){
			 type= DOUBLE_TYPE;
		}else if(param.equals("Q2RMS")){
			 type= DOUBLE_TYPE;
		}else{
			 type=STRING_TYPE;
		}
		return type;
		
	}
	
	
	public void addGlobalParameter(String param, String val) {
		globalParameters.put(param, val);
	}
	
	public String getGlobalParameter(String name) {
		return globalParameters.get(name).replace("\"", "");
	}
	
	public double getGlobalParameterDouble(String name) {
	    return Double.parseDouble(globalParameters.get(name));
	}
	
	public String[] getGlobalParameterNames() {
		return globalParameters.keySet().toArray( new String[globalParameters.size()] );
	}
	
	public int getRowCount() {
		if (doubleData.size() < 1 )
		    return stringData.size();
		else
		    return doubleData.size();
	}
	
	public String[] getRow(int nRow) {
		String[] stringPart = stringData.get(nRow);
		double[] doublePart = doubleData.get(nRow);
		
		String[] res = new String[stringPart.length + doublePart.length];
		
		for (int i=0; i < stringPart.length; i++) {
			res[i] = stringPart[i];
		}
		
		for (int i=stringPart.length; i < stringPart.length + doublePart.length; i++) {
			res[i] = String.valueOf( doublePart[i-stringPart.length]);
		}
		return res;
	}
}