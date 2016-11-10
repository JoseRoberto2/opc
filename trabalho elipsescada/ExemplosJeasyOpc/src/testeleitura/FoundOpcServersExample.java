package testeleitura;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafish.clients.opc.browser.JOpcBrowser;
import javafish.clients.opc.exception.HostException;
import javafish.clients.opc.exception.NotFoundServersException;

public class FoundOpcServersExample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        FoundOpcServersExample teste = new FoundOpcServersExample();

    // init COM components
        JOpcBrowser.coInitialize();
        synchronized (teste) {
            try {
                teste.wait(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FoundOpcServersExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            String[] servers = JOpcBrowser.getOpcServers("localhost");
            if (servers != null) {
                System.out.println(Arrays.asList(servers));
            } else {
                System.out.println("Array Servers is null.");
            }
        } catch (HostException e) {
            e.printStackTrace();
        } catch (NotFoundServersException e) {
            e.printStackTrace();
        }

        // uninitialize COM components
        JOpcBrowser.coUninitialize();
    }

}
