package com.dbs.listeners;

import com.dbs.Communicator;
import com.dbs.utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ScheduledExecutorService;

public abstract class Listener {

    Communicator communicator;
    ScheduledExecutorService threadPool;

    public Listener(Communicator communicator, ScheduledExecutorService threadPool) {
        this.communicator = communicator;
        this.threadPool = threadPool;
    }

    public void listen() {


        while(true) {
            try {
                DatagramPacket packet = communicator.receive();
                threadPool.submit(() -> processPacket(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    protected abstract void processPacket(DatagramPacket packet);
}
