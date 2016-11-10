package testeleitura;

import javafish.clients.opc.JCustomOpc;
import javafish.clients.opc.JEasyOpc;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.asynch.AsynchEvent;
import javafish.clients.opc.asynch.OpcAsynchGroupListener;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.CoInitializeException;
import javafish.clients.opc.exception.CoUninitializeException;
import javafish.clients.opc.exception.ComponentNotFoundException;
import javafish.clients.opc.exception.GroupActivityException;
import javafish.clients.opc.exception.GroupUpdateTimeException;

public class JEasyOpcExample implements OpcAsynchGroupListener {
  
  public static void main(String[] args) throws InterruptedException {
    JEasyOpcExample test = new JEasyOpcExample();
    
    try {
      JOpc.coInitialize();
    }
    catch (CoInitializeException e1) {
    }
    
    JEasyOpc jopc = new JEasyOpc("localhost", "ElipseSCADA.OPCSvr.1", "JOPC1");
    
    OpcItem item1 = new OpcItem("tagSP", true, "");
    OpcItem item2 = new OpcItem("tagPV", true, "");
    OpcItem item3 = new OpcItem("tagMV", true, "");
    
    OpcGroup group = new OpcGroup("group1", true, 2000, 0.0f);
    
    group.addItem(item1);
    group.addItem(item2);
    group.addItem(item3);
    
    jopc.addGroup(group);
    
    group.addAsynchListener(test);
    
    jopc.start();
    
    synchronized(test) {
      test.wait(3000);
    }
    
    System.out.println("JOPC active: " + jopc.ping());
    
    synchronized(test) {
      test.wait(8000);
    }
    
    try {
      jopc.setGroupActivity(group, false);
    }
    catch (GroupActivityException | ComponentNotFoundException e) {
    }
    
    synchronized(test) {
      test.wait(4000);
    }
    
    try {
      jopc.setGroupActivity(group, true);
    }
    catch (GroupActivityException | ComponentNotFoundException e) {
    }

    synchronized(test) {
      test.wait(4000);
    }
    
    try {
      jopc.setGroupUpdateTime(group, 100);
    }
    catch (GroupUpdateTimeException | ComponentNotFoundException e) {
    }
    
//    try {
//      jopc.setItemActivity(group, item8, false);
//    }
//    catch (ComponentNotFoundException e1) {
//      e1.printStackTrace();
//    }
//    catch (ItemActivityException e1) {
//      e1.printStackTrace();
//    }
    
    synchronized(test) {
      test.wait(8000);
    }
    
    jopc.terminate();

    synchronized(test) {
      test.wait(2000);
    }
    
    try {
      JOpc.coUninitialize();
    }
    catch (CoUninitializeException e) {
      e.printStackTrace();
    }
  }

  public void getAsynchEvent(AsynchEvent event) {
    System.out.println(((JCustomOpc)event.getSource()).getFullOpcServerName() + "=>");
    System.out.println("Package: " + event.getID());
    System.out.println(event.getOPCGroup());
  }

}
