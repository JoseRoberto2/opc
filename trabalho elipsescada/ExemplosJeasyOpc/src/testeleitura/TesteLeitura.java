/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testeleitura;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.*;

/**
 *
 * @author ufrnUsr
 */
public class TesteLeitura {

    public static void main(String[] args) throws InterruptedException, ConnectivityException {
        // TODO code application logic here

        JOpc.coInitialize();

        JOpc jopc = new JOpc("localhost", "ElipseSCADA.OPCSvr.1", "JOPC1");
        OpcItem item1 = new OpcItem("tagSP", true, "");
        OpcItem item2 = new OpcItem("tagPV", true, "");
        OpcItem item3 = new OpcItem("tagMV", true, "");
        OpcGroup group = new OpcGroup("nuex", true, 500, 0.0f);

        group.addItem(item1);
        group.addItem(item2);
        group.addItem(item3);
        jopc.addGroup(group);

        try {
            jopc.connect();
            System.out.println("JOPC client conectado..");
            jopc.registerGroups();
            System.out.println("OPCGroup registrado...");
        } catch (UnableAddGroupException | UnableAddItemException e2) {
            System.out.println("Erro de conexção!");
        }
        try {
             
            OpcGroup responsegroup;
            for (int i = 0; i < 10; i++) {
            responsegroup = jopc.synchReadGroup(group);
            System.out.println("Valor SP: "+responsegroup.getItems().get(0).getValue());
            System.out.println("Valor PV: "+responsegroup.getItems().get(1).getValue());
            System.out.println("Valor MV: "+responsegroup.getItems().get(2).getValue());
            }
            Thread.sleep(1000);
        } catch (ComponentNotFoundException | SynchReadException ex) {
            Logger.getLogger(TesteLeitura.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOpc.coUninitialize();
        System.out.println("JOPC cliente finalizado...");

    }

}
