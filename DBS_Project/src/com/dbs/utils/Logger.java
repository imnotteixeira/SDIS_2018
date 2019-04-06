package com.dbs.utils;

import com.dbs.PeerController;

public class Logger {

    public static void log(String msg) {
        System.out.println("[" + PeerController.getInstance().getConnectionInfo().getSenderId() + "] " + msg);
    }
}
