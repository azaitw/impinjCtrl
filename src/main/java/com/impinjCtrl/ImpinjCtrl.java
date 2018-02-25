package com.impinjCtrl;

public class ImpinjCtrl {
    public static void main(String[] args) {
        ReaderController rc = ReaderController.getInstance();
        rc.initialize();
        rc.initTerminalInterface(); // Command-line reader control interface
    }
}
