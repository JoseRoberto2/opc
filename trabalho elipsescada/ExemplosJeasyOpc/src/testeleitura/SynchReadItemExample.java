package testeleitura;

import javafish.clients.opc.JOpc;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.ComponentNotFoundException;
import javafish.clients.opc.exception.ConnectivityException;
import javafish.clients.opc.exception.SynchReadException;
import javafish.clients.opc.exception.UnableAddGroupException;
import javafish.clients.opc.exception.UnableAddItemException;

public class SynchReadItemExample {
  public static void main(String[] args) throws InterruptedException {
    SynchReadItemExample test = new SynchReadItemExample();
    
    JOpc.coInitialize();
   
    JOpc jopc = new JOpc("localhost", "ELIPSESCADA.OPCSvr.1", "JOPC1");

    OpcItem item1 = new OpcItem("tagMV", true, "");
   
    OpcGroup group = new OpcGroup("group1", true, 500, 0.0f);
    
    group.addItem(item1);
    jopc.addGroup(group);
    
    try {
      jopc.connect();
      System.out.println("JOPC client is connected...");
    }
    catch (ConnectivityException e2) {
      e2.printStackTrace();
    }
    
    try {
      jopc.registerGroups();
      System.out.println("OPCGroup are registered...");
    }
    catch (UnableAddGroupException e2) {
      e2.printStackTrace();
    }
    catch (UnableAddItemException e2) {
      e2.printStackTrace();
    }
    
    synchronized(test) {
      test.wait(50);
    }
    
    // Synchronous reading of item
    int cycles = 7;
    int acycle = 0;
    while (acycle++ < cycles) {
      synchronized(test) {
        test.wait(1000);
      }
      
      try {
        OpcItem responseItem = jopc.synchReadItem(group, item1);
        
        System.out.println("Data e Hora: "+responseItem.getTimeStamp().getTime());
        System.out.println("Valor: "+ responseItem.getValue());
      }
      catch (ComponentNotFoundException | SynchReadException e1) {
        e1.printStackTrace();
      }
      
          }
    
    JOpc.coUninitialize();
  }
}
