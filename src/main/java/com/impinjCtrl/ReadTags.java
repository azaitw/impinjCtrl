package com.impinjCtrl;

public class ReadTags {

    public static void main(String[] args) {
        String readerHost = System.getProperty(Properties.readerHost);

        ReaderController rc = new ReaderController(readerHost);
        rc.initialize();
    }
}
