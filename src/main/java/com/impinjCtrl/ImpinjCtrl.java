package com.impinjCtrl;

import lib.Logging;

public class ImpinjCtrl {
    public static void main(String[] args) {
        ReaderController rc = new ReaderController();
        Logging.initLogPath(); // Create log folder if not yet exist
        rc.initialize();
    }
}
