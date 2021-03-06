/**
 *
 */
package data;

import files.FileOperations;
import gui.WaitDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Semaphore;

/**
 *
 */
public class OpenFileInBackground extends Thread {

    public static final int ZIP = 1;

    public static final int UNZIP = 2;

    private File fileToUnzip;

    private File destination;

    private Semaphore sem;

    private JDialog waitDialog;

    private int operation;

    private Window dialog;

    public OpenFileInBackground(File fileToUnzip, File destination,
                                Semaphore sem, Window dialog, int operation) {
        this.fileToUnzip = fileToUnzip;
        this.sem = sem;
        this.destination = destination;
        this.operation = operation;
        this.dialog = dialog;

    }

    public void startAction() {
        waitDialog = new WaitDialog(dialog, false);

        try {
            sem.acquire();
            start();
        } catch (InterruptedException exn) {
            // TODO Auto-generated catch block
            exn.printStackTrace();
        }
        waitDialog.setVisible(true);
    }

    public void run() {
        if (operation == UNZIP)
            FileOperations.unzip(fileToUnzip);
        else
            FileOperations.zip(fileToUnzip, destination);
        waitDialog.dispose();
        sem.release();
    }
}
