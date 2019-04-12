package com.dbs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

    public static void main(String[] args){



//        The testing application should be invoked as follows:
//
//        $ java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
//                where:
//
//<peer_ap>
//                Is the peer's access point. This depends on the implementation. (See the previous section)
//<operation>
//                Is the operation the peer of the backup service must execute. It can be either the triggering of the subprotocol to test, or the retrieval of the peer's internal state. In the first case it must be one of: BACKUP, RESTORE, DELETE, RECLAIM. In the case of enhancements, you must append the substring ENH at the end of the respecive subprotocol, e.g. BACKUPENH. To retrieve the internal state, the value of this argument must be STATE
//<opnd_1>
//                Is either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks. In the latter case, the peer should execute the RECLAIM protocol, upon deletion of any chunk. The STATE operation takes no operands.
//<opnd_2>
//                This operand is an integer that specifies the desired replication degree and applies only to the backup protocol (or its enhancement)
//        E.g., by invoking:
        String peer_ap = "";
        String operation = "";
        String op1 = "";
        String op2 = "";

        if(args.length != 3 && args.length != 4) {
            System.out.println("WRONG USAGE!");
            System.out.println("usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;

        }

        peer_ap = args[0];
        operation = args[1];
        op1 = args[2];

        if(args.length == 4) {
            op2 = args[3];
        }


        try {
            Registry reg = LocateRegistry.getRegistry("localhost");

            IPeerInterface stub = (IPeerInterface) reg.lookup(peer_ap);

            processArgs(stub, operation, op1, op2);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static void processArgs(IPeerInterface stub, String operation, String op1, String op2) throws RemoteException {

        switch(operation) {
            case "BACKUP": case "BACKUPENH"://vale??

                if(op2.equals("")) {
                    System.out.println("Replication Degree not specified");
                    return;
                }

                stub.backup(op1, Integer.parseInt(op2));
                break;

            case "RESTORE":
                stub.recover(op1);
                break;
            case "RESTOREENH":
                stub.recover_enhanced(op1);
                break;
            case "DELETE":
                stub.delete(op1);
                break;
            case "RECLAIM":
                stub.reallocateSpace(Integer.parseInt(op1));
                break;
            case "STATE":
                break;


        }
    }
}
