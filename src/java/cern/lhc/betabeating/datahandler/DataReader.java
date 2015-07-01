package cern.lhc.betabeating.datahandler;

import java.util.ArrayList;

public abstract class DataReader {

	public abstract void loadTable(String path);
	public abstract double[] getDoubleData(String key);
	public abstract String[] getStringData(String key);
	public abstract ArrayList<String> getKeys();
	public abstract int getKeyType(String key);
	public abstract int getGlobalParameterType(String key);
	public abstract String[] getGlobalParameterNames();
	public abstract double getGlobalParameterDouble(String name);
	public abstract String getGlobalParameter(String name);
	
	
	public abstract int getRowCount();
	public abstract String[] getRow(int nRow);
	
	public static final int DOUBLE_TYPE = 0;
	public static final int STRING_TYPE = 1;
	public static final int UNKNOWN_TYPE = -1;
	
}
