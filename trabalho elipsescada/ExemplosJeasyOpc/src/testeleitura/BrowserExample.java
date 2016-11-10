package testeleitura;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafish.clients.opc.JEasyOpc;

import javafish.clients.opc.browser.JOpcBrowser;
import javafish.clients.opc.exception.CoInitializeException;
import javafish.clients.opc.exception.CoUninitializeException;
import javafish.clients.opc.exception.ConnectivityException;
import javafish.clients.opc.exception.HostException;
import javafish.clients.opc.exception.NotFoundServersException;
import javafish.clients.opc.exception.UnableAddGroupException;
import javafish.clients.opc.exception.UnableAddItemException;
import javafish.clients.opc.exception.UnableBrowseBranchException;
import javafish.clients.opc.exception.UnableBrowseLeafException;
import javafish.clients.opc.exception.UnableIBrowseException;

public class BrowserExample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            JOpcBrowser.coInitialize();
        } catch (CoInitializeException e1) {
            e1.printStackTrace();
        }

        // find opc-servers (OpcEnum interface)
        try {
            String[] opcServers = JOpcBrowser.getOpcServers("localhost");
            System.out.println(Arrays.asList(opcServers));

        } catch (HostException | NotFoundServersException e1) {
            e1.printStackTrace();
        }

        JOpcBrowser jbrowser = new JOpcBrowser("localhost", "ElipseSCADA.OPCSvr.1", "JOPCBrowser1");

        try {
            jbrowser.connect();
            String[] branches = jbrowser.getOpcBranch("");
            System.out.println(Arrays.asList(branches));
            System.out.println(branches[0].toString());

            String[] items = jbrowser.getOpcItems(branches[0].toString(), true);
            if (items != null) {
                for (int i = 0; i < items.length; i++) {
                    System.out.println(items[i]);
                }
            }
        } catch (ConnectivityException | UnableBrowseBranchException | UnableIBrowseException e) {
        } catch (UnableBrowseLeafException | UnableAddGroupException | UnableAddItemException ex) {
            Logger.getLogger(BrowserExample.class.getName()).log(Level.SEVERE, null, ex);
        }
        JOpcBrowser.coUninitialize();
//    try {
//      //String[] items = jbrowser.getOpcItems("Tags", true);
//      String[] items = jbrowser.getOpcItems(teste, true);
//      if (items != null) {
//        for (int i = 0; i < items.length; i++) {
//          System.out.println(items[i]);
//        }
//      }
//      // disconnect server
//      JOpcBrowser.coUninitialize();
//    }
//    catch (UnableBrowseLeafException | UnableIBrowseException | UnableAddGroupException | UnableAddItemException | CoUninitializeException e) {
//      e.printStackTrace();
//    }

//    try {
//      //String[] items = jbrowser.getOpcItems("Tags", true);
//      String[] items = jbrowser.getOpcItems(teste, true);  
//      if (items != null) {
//        for (int i = 0; i < items.length; i++) {
//          System.out.println(items[i]);
//        }
//      }
//      // disconnect server
//      JOpcBrowser.coUninitialize();
//    }
//    catch (UnableBrowseLeafException | UnableIBrowseException | UnableAddGroupException | UnableAddItemException | CoUninitializeException e) {
//      e.printStackTrace();
//    }
    }

}
