package com.impinjCtrl;

import lib.Logging;

public class ImpinjCtrl {
    public static void main(String[] args) {
        ReaderController rc = ReaderController.getInstance();
        Logging logger = Logging.getInstance();
        logger.createLogFolderIfUnavailable(); // Create log folder if not yet exist
        rc.initialize();
    }
}
