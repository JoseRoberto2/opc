package testeleitura;

import javafish.clients.opc.JOpc;
import javafish.clients.opc.component.OpcGroup;
import javafish.clients.opc.component.OpcItem;
import javafish.clients.opc.exception.Asynch20ReadException;
import javafish.clients.opc.exception.Asynch20UnadviseException;
import javafish.clients.opc.exception.CoInitializeException;
import javafish.clients.opc.exception.CoUninitializeException;
import javafish.clients.opc.exception.ComponentNotFoundException;
import javafish.clients.opc.exception.ConnectivityException;
import javafish.clients.opc.exception.GroupActivityException;
import javafish.clients.opc.exception.GroupUpdateTimeException;
import javafish.clients.opc.exception.UnableAddGroupException;
import javafish.clients.opc.exception.UnableAddItemException;

public class AsynchReadAndGroupActivityExample {

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException, Asynch20ReadException, Asynch20UnadviseException {
        AsynchReadAndGroupActivityExample test = new AsynchReadAndGroupActivityExample();

        JOpc.coInitialize();

        JOpc jopc = new JOpc("localhost", "ElipseSCADA.OPCSvr.1", "JOPC1");

        OpcItem item1 = new OpcItem("tagSP", true, "");
        OpcItem item2 = new OpcItem("tagPV", true, "");
        OpcItem item3 = new OpcItem("tagMV", true, "");
        OpcGroup group = new OpcGroup("group1", true, 2000, 0.0f);

        group.addItem(item1);
        group.addItem(item2);
        group.addItem(item3);

        jopc.addGroup(group);

        try {
            jopc.connect();
            System.out.println("OPC client is connected...");

            jopc.registerGroups();
            System.out.println("OPC groups are registered...");

            jopc.asynch20Read(group);
            System.out.println("OPC asynchronous reading is applied...");

            OpcGroup downGroup;

            long start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - start) < 10000) {
                jopc.ping();
                downGroup = jopc.getDownloadGroup();
                if (downGroup != null) {
                    System.out.println(downGroup);
                }

                if ((System.currentTimeMillis() - start) >= 6000) {
                    jopc.setGroupActivity(group, false);
                }

                synchronized (test) {
                    test.wait(50);
                }
            }

            // change activity
            jopc.setGroupActivity(group, true);

            // change updateTime
            jopc.setGroupUpdateTime(group, 100);

            start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - start) < 10000) {
                jopc.ping();
                downGroup = jopc.getDownloadGroup();
                if (downGroup != null) {
                    System.out.println(downGroup);
                }

                synchronized (test) {
                    test.wait(50);
                }
            }

            jopc.asynch20Unadvise(group);
            System.out.println("OPC asynchronous reading is unadvise...");

            JOpc.coUninitialize();
            System.out.println("Program terminated...");

            System.out.println("");
        } catch (ConnectivityException | UnableAddGroupException | UnableAddItemException | ComponentNotFoundException | GroupUpdateTimeException | GroupActivityException | CoUninitializeException e) {
        }
    }

}
