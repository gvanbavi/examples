package cern.lhc.betabeating.datahandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cern.accsoft.gui.frame.MessageManager;

public class TFSFilter {
    private static final Logger log = Logger.getLogger(TFSFilter.class);

    private File fnew;
    private File fnewbad;
    private File f;
    private ArrayList<String> listOfKeys;
    private ArrayList<String> keys;
    private double[] data4clean;
    private ArrayList<String> data2write;
    private String[] oneRow;
    private int count;
    private PrintWriter out;
    private PrintWriter outbad;
    private String plane;

    public TFSFilter() {

    }

    public void Clean(String file, TFSReader tfs, double max, double min, String selname) {

        data4clean = tfs.getDoubleData(selname);
        count = 0;

        log.info(file.toString());

        if (file.contains("_linx")) {
            fnew = new File(file.replace("_linx", ".old_linx"));
            fnewbad = new File(file.replace("_linx", ".badM"));
            plane = "H";
        } else if (file.contains("_liny")) {
            fnew = new File(file.replace("_liny", ".old_liny"));
            fnewbad = new File(file.replace("_liny", ".badM"));
            plane = "V";
        } else if (file.contains("_svdx")) {
            fnew = new File(file.replace("_svdx", ".old_svdx"));
            fnewbad = new File(file.replace("_svdx", ".badM"));
            plane = "H";
        } else if (file.contains("_svdy")) {
            fnew = new File(file.replace("_svdy", ".old_svdy"));
            fnewbad = new File(file.replace("_svdy", ".badM"));
            plane = "V";
        } else {
            fnew = new File(file.replace("", ".old"));
        }

        f = new File(file);

        f.renameTo(fnew);
        try {

            out = new PrintWriter(new FileWriter(file));
            String[] globalpara = tfs.getGlobalParameterNames();

            for (int i = 0; i < globalpara.length; i++) {
                String Global = "@ " + globalpara[i] + "  %le  " + tfs.getGlobalParameter(globalpara[i]);
                out.write(Global + "\n");
            }

            listOfKeys = tfs.getKeys();
            keys = new ArrayList<String>();

            for (int i = 0; i < listOfKeys.size(); i++) {
                int type = tfs.getKeyType(listOfKeys.get(i));
                if (type == 1) {
                    keys.add("%s  ");
                } else if (type == 0) {
                    keys.add("%le  ");

                }
            }
            out.write("*  " + appendItems(listOfKeys) + "\n");
            out.write("$  " + appendItems(keys) + "\n");

            if (fnewbad.exists()) {
                outbad = new PrintWriter(new FileWriter(fnewbad, true));
            } else {
                outbad = new PrintWriter(new FileWriter(fnewbad, false));
                outbad.write("* NAME S PLANE");
                outbad.write("\n$ %s %le %le\n");
            }

            for (int i = 0; i < data4clean.length; i++) {

                // System.out.println((data4clean[i]<=max)+" "+(min<=data4clean[i])+" "+data4clean[i]+" "+max+" "+min+" "+data4clean[i]);
                oneRow = tfs.getRow(i);
                if ((data4clean[i] <= max) && (min <= data4clean[i])) {
                    data2write = new ArrayList<String>();
                    for (int j = 0; j < oneRow.length; j++) {
                        data2write.add(oneRow[j]);
                    }
                    out.write(appendItems(data2write) + "\n");
                } else {
                    count = count + 1;
                    outbad.write(oneRow[0] + " " + oneRow[1] + " " + plane + "\n");
                }
            }

            if (count > 0) {
                MessageManager.getConsoleLogger().info(
                        "DataCleaner => Removing " + count + " \n For the file " + new File(file).getName());
            } else {
                MessageManager.getConsoleLogger()
                        .warn("DataCleaner => Nothing to clean on " + new File(file).getName());
            }

            out.close();
            outbad.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
    }

    private static String appendItems(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list)
            stringBuilder.append(string).append(" ");
        return stringBuilder.toString();
    }
}
