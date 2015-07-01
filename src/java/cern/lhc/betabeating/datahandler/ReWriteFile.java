package cern.lhc.betabeating.datahandler;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cern.accsoft.gui.frame.MessageManager;
import cern.lhc.betabeating.Tools.FileIO;

/**
 * Handles file rewriting
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */

public class ReWriteFile {
    public static void executeForFilePathDPPMethod(String filePath, String DPP, ReWriteFileMethod reWriteFileMethod) {
        String extension = reWriteFileMethod.getExtension();
        List<String> lines = null;

        // horizontal
        String filePathX = filePath + "_" + extension + "x";
        lines = getLinesFromFilePathWithDPP(filePathX, DPP);
        writeLinesToFilePath(lines, filePathX);

        // vertical
        String filePathY = filePath + "_" + extension + "y";
        lines = getLinesFromFilePathWithDPP(filePathY, DPP);
        writeLinesToFilePath(lines, filePathY);
    }

    private static List<String> getLinesFromFilePathWithDPP(String filePath, String DPP) {
        List<String> lines = new LinkedList<String>();
        BufferedReader bufferedReader = null;
        String line = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            lines.add("@ DPP %le " + DPP);
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("DPP"))
                    lines.add(line);
            }
        } catch (IOException e) {
            lines = Collections.emptyList();
            if (MessageManager.getConsoleLogger().isErrorEnabled())
                MessageManager.getConsoleLogger().error("Reading failed for path: " + filePath, e);
        } finally {
            FileIO.tryToCloseCloseable(bufferedReader);
        }
        return lines;
    }

    private static void writeLinesToFilePath(List<String> lines, String filePath) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            for (String linesItem : lines) {
                bufferedWriter.write(linesItem);
                bufferedWriter.write("\n");
            }
        } catch (IOException e) {
            if (MessageManager.getConsoleLogger().isErrorEnabled())
                MessageManager.getConsoleLogger().error("Writing failed for path: " + filePath, e);
        } finally {
            FileIO.tryToCloseCloseable(bufferedWriter);
        }
    }

    public static enum ReWriteFileMethod
    {
        SUSSIX, SVD;
        
        public String getExtension()
        {
            String result = null;
            switch (this) {
            case SUSSIX:
                result = "lin";
                break;
            case SVD:
                result = "svd";
                break;
            default:
                MessageManager.getConsoleLogger().error("default case");
            }
            return result;
        }
    }
}