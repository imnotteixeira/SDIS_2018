package com.dbs.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkAddress {

    public String hostname;
    public int port;

    public NetworkAddress(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    public NetworkAddress(String address) {

        String ip_part = "(?:[01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

        String ADDR_REGEXP = "(" + ip_part + "\\." + ip_part+ "\\."
                + ip_part+ "\\." + ip_part + "):([0-9]+)";
        Pattern r = Pattern.compile(ADDR_REGEXP);

        Matcher m = r.matcher(address);
        m.find();
        this.hostname = m.group(1);
        this.port = Integer.valueOf(m.group(2));

    }

}
