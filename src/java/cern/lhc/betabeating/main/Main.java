package cern.lhc.betabeating.main;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Logger;

import cern.lhc.betabeating.frames.Controller;

/**
 * Entry point for the application as specified in product.xml .
 * 
 * @author Glenn Vanbavinckhove, tbach
 */
public class Main { //if class name changed, product.xml has to be changed
    private static final Logger log = Logger.getLogger(Main.class);
    public static void main(String[] args) { //if main method changed, product.xml has to be changed
        startBeamSelection();
    }
    
    public static void startBeamSelection()
    {
//        activateNimbus();
        if (Test.doOnlyTestingAndDontLoadGui())
            return;
        Controller controller = new Controller();
        controller.showBeamSelection();
    }
    
    @SuppressWarnings("unused")
    private static void activateNimbus() //just for testing reasons
    {
//      javax.swing.UIManager$LookAndFeelInfo[Metal javax.swing.plaf.metal.MetalLookAndFeel]
//      Metal
//      javax.swing.UIManager$LookAndFeelInfo[Nimbus com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel]
//      Nimbus
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("error in nimbus activation?", e);
        }
    }
}
