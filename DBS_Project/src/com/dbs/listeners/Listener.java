package com.dbs.listeners;

import java.net.MulticastSocket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Listener {

    public static final int BUF_SIZE = 65622;

    MulticastSocket socket;
    ScheduledExecutorService threadPool;

    public Listener(MulticastSocket socket, ScheduledExecutorService threadPool) {
        this.socket = socket;
        this.threadPool = threadPool;
    }

    public abstract void listen();
}
