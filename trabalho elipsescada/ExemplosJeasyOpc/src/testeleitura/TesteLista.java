/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testeleitura;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafish.clients.opc.JEasyOpc;
import javafish.clients.opc.JOpc;
import javafish.clients.opc.browser.JOpcBrowser;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.*;
import javafish.clients.opc.variant.Variant;

/**
 *
 * @author ufrnusr
 */
public class TesteLista {
    
    
     public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
         String x = "a";
         Variant test = new Variant(x);
         System.out.println("String\n");
         System.out.println(test.getVariantType()+"\n");
         
         int x2 = 5;
         test = new Variant(x2);
         System.out.println("inteiro\n");
         System.out.println(test.getVariantType()+"\n");
         
         short x3 = 5;
         test = new Variant(x3);
         System.out.println("short\n");
         System.out.println(test.getVariantType()+"\n");
         
         float x4 = 1;
         test = new Variant(x4);
         System.out.println("float\n");
         System.out.println(test.getVariantType()+"\n");
         
         byte x5 = 1;
         test = new Variant(x5);
         System.out.println("byte\n");
         System.out.println(test.getVariantType()+"\n");
         
         double x6 = 1;
         test = new Variant(x6);
         System.out.println("double\n");
         System.out.println(test.getVariantType()+"\n");
         
         boolean x7 = true;
         test = new Variant(x7);
         System.out.println("booleant\n");
         System.out.println(test.getVariantType()+"\n");
//         Variant test = new Variant("a");
//         System.out.println("Texto\n");
//         System.out.println(test.getVariantType()+"\n");
//        JOpc.coInitialize();
//    
//        //JOpc jopc = new JOpc("localhost", "National Instruments.Variable Engine.1", "JOPC1");
//        JOpc opcteste = new JOpc(null, null, null);
//        JOpcBrowser jbrowser = new JOpcBrowser(null, null, null);
//        
//
//        OpcItem item1 = new OpcItem("\\\\.\\nuex\\C12136", true, "");
//        OpcItem item2 = new OpcItem("\\\\.\\nuex\\C9709", true, "");
//        OpcItem item3 = new OpcItem("\\\\.\\nuex\\C2425", true, "");
//        OpcItem item4 = new OpcItem("\\\\.\\nuex\\C8677", true, "");
//        OpcGroup group = new OpcGroup("group1", true, 100, 0.0f);
//        String cont = null;
//    
//        group.addItem(item1);
//        group.addItem(item2);
//        group.addItem(item3);
//        group.addItem(item4);
//        //jopc.addGroup(group);
//    
//       // try {
//          //  jopc.connect();
//            String serve = null;
//        try {
//            serve = jbrowser.getOpcBranch("").toString();
//        } catch (UnableBrowseBranchException | UnableIBrowseException ex) {
//            Logger.getLogger(TesteLista.class.getName()).log(Level.SEVERE, null, ex);
//        }
//            System.out.println("JOPC client conectado.."+serve);
//    //    }
//            //catch (ConnectivityException e2) {
//        //}
//
//     /*   try {
//            //jopc.registerGroups();
//            System.out.println("OPCGroup registrado...");
//        }
//        catch (UnableAddGroupException | UnableAddItemException e2) {
//        }*/
//        
//       
//        
//     /*   try {
//            
//          
//            
//            
//          /*  for (int j=0;j<2000;j++)
//            {
//                //Thread.sleep(500);
//                //for (int i=0;i<group.getItemCount();i++)
//                  //  {
//                //Thread.sleep(1000);
//                OpcGroup responsegroup = jopc.synchReadGroup(group);
//            
//                //OpcItem responseItem = jopc.synchReadItem(group, item1);
//                //OpcItem responseItem1 = jopc.synchReadItem(group, item2);
//                //System.out.println(responsegroup);
//                //System.out.println(responsegroup.getItems().get(i).getItemName()+" : "+responsegroup.getItems().get(i).getValue());
//                //System.out.println(Variant.getVariantName(responseItem.getDataType()) + " 1: " + responseItem.getValue());
//                //System.out.println(Variant.getVariantName(responseItem1.getDataType()) + " 2: " + responseItem1.getValue());
//                    //}
//             cont = String.valueOf(j+1);
//            }
//            
//            System.out.println("Lidos "+cont+" vezes o "+group.getGroupName()+" com "+group.getItemCount()+" Itens no Grupo");
//              }
//        catch (ComponentNotFoundException | SynchReadException e1) {
//        }
//        */
//        JOpc.coUninitialize();
//        System.out.println("JOPC cliente finalizado...");

    }
    
}
