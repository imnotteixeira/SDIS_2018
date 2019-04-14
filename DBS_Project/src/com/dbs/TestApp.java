package com.dbs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

    public static void main(String[] args){

        String peer_ap = "";
        String operation = "";
        String op1 = "";
        String op2 = "";

        if(args.length != 3 && args.length != 4 && !(args.length == 2 && args[1].equals("STATE"))) {
            System.out.println("WRONG USAGE!");
            System.out.println("usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        peer_ap = args[0];
        operation = args[1];

        if(args.length >= 3){
            op1 = args[2];
        }

        if(args.length == 4) {
            op2 = args[3];
        }

        try {
            Registry reg = LocateRegistry.getRegistry("localhost");

            IPeerInterface stub = (IPeerInterface) reg.lookup(peer_ap);

            System.out.println(processArgs(stub, operation, op1, op2));

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static String processArgs(IPeerInterface stub, String operation, String op1, String op2) throws RemoteException {

        switch(operation) {
            case "BACKUP":

                if(op2.equals("")) {
                    System.out.println("Replication Degree not specified");
                    return "";
                }

                stub.backup(op1, Integer.parseInt(op2));
                return "";
            case "BACKUPENH":
                if(op2.equals("")) {
                    System.out.println("Replication Degree not specified");
                }
                return stub.backup_enhanced(op1, Integer.parseInt(op2));
            case "RESTORE":
                stub.recover(op1);
                return "";
            case "RESTOREENH":
                return stub.recover_enhanced(op1);
            case "DELETE":
                stub.delete(op1);
                return "";
            case "RECLAIM":
                stub.reallocateSpace(Integer.parseInt(op1));
                return "";
            case "STATE":
                System.out.println(stub.getState());
                break;
        }
        return "";
    }
}
