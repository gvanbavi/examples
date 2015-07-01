package cern.lhc.betabeating.frames.exterior;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

//not used anymore
public class WaitingWindow extends JFrame {
    private static final long serialVersionUID = -7629392996881533337L;
    private static final Logger log = Logger.getLogger(WaitingWindow.class);

    public WaitingWindow() {
        super("Loading window");
    }

    private JLabel label2 = new JLabel("Welcome to beta-beat loading window");

    public void Loadgraphics(String path) {

        setLayout(new BorderLayout());
        String curDir = path + "CoreFiles/Images/";

        ImageIcon icon2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(curDir + "CERN_LHC_tunnel.jpg"));
        log.info("imagePath: " + curDir + "/CERN_LHC_tunnel.jpg");
        JLabel label = new JLabel(icon2);

        add(label, BorderLayout.CENTER);
        add(label2, BorderLayout.SOUTH);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) dim.getWidth() / 5, (int) dim.getHeight() / 5);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 400);
        setVisible(true);

    }

    /*
     * creating structure
     */

    private File File4dir;
    private String file4optics;
    public String modelpath;

    public void createstructure(String accel, String output, String optics, String date) {
        try {
//            Thread.sleep(2000);
            label2.setText("In the next minute the data structure will be created ...");
//            Thread.sleep(4000);
            label2.setText("Soooo sit back and relax :-)");
//            Thread.sleep(2000);

            modelpath = output + "/" + date + "/models/" + accel + "/";
            // creating model dir
            if (!optics.equals("External")) {
                File4dir = new File(output + "/" + date + "/models/" + accel + "/" + optics);
                file4optics = output + "/" + date + "/models/" + accel + "/" + optics;
                log.info("not equals: " + file4optics);
                File4dir.mkdirs();
                if (File4dir.exists()) {
                    label2.setText("INFO: model dir created");
                } else {
                    label2.setText("ERROR: failed to create model dir ... contact expert");
                    Thread.sleep(4000);
                    System.exit(0);
                }
            } else {
                File4dir = new File(output + "/" + date + "/models/" + accel);
                file4optics = output + "/" + date + "/models/" + accel;
                log.info("equals: " + file4optics);
                File4dir.mkdirs();
                if (File4dir.exists()) {
                    label2.setText("INFO: model dir created");
                } else {
                    label2.setText("ERROR: failed to create model dir ... contact expert");
                    Thread.sleep(4000);
                    System.exit(0);
                }
            }
//            Thread.sleep(2000);
            // creating measurements
            File4dir = new File(output + "/" + date + "/" + accel + "/Measurements");
            File4dir.mkdirs();
            if (File4dir.exists()) {
                label2.setText("INFO: measurements dir created");
            } else {
                label2.setText("ERROR: failed to create measurements dir ... contact expert");
                Thread.sleep(4000);
                System.exit(0);
            }
//            Thread.sleep(2000);
            // creating results
            File4dir = new File(output + "/" + date + "/" + accel + "/Results");
            File4dir.mkdirs();
            if (File4dir.exists()) {
                label2.setText("INFO: results dir created");
            } else {
                label2.setText("ERROR: failed to create results dir ... contact expert");
                Thread.sleep(4000);
                System.exit(0);
            }
//            Thread.sleep(2000);
            label2.setText("INFO: Finished creating dir... will create models");
            createoptics(optics);
            // creating models

            label2.setText("INFO: Model dir created! Will load main window");
//            Thread.sleep(2000);
            setVisible(false);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * create optics
     */
    public String Opticsfilename;

    public void createoptics(String optics) {

        if (optics.equals("External")) {
            Opticsfilename = file4optics + "/";

        }
    }
}
