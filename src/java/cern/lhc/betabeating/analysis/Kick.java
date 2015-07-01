package cern.lhc.betabeating.analysis;

import javax.swing.JFrame;

import cern.lhc.betabeating.datahandler.TFSReader;


public class Kick extends JFrame{
	
	

	public Kick(){
		
	}
	
	/**
	 *  Data
	 */
	private TFSReader tfsread;
	
	private void getKick(String file){
		tfsread = new TFSReader();
		tfsread.loadTable(file);
		
		
	}
}
