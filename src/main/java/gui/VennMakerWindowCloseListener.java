package gui;

import files.IO;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VennMakerWindowCloseListener<T> extends WindowAdapter {

    private VennMakerCallback<T> vennMakerCallback;

    public VennMakerWindowCloseListener(VennMakerCallback<T> vennMakerCallback) {
        this.vennMakerCallback = vennMakerCallback;
    }

    public void windowClosing(WindowEvent event) {

        // Bei ungespeicherten Daten nachfragen, ob gespeichert werden
        // soll
        if (!VennMaker.getInstance().isChangesSaved())
            switch (JOptionPane.showConfirmDialog(null,
                    Messages.getString("VennMaker.ConfirmQuit"), Messages //$NON-NLS-1$
                            .getString("VennMaker.ConfirmQuitTitel"), //$NON-NLS-1$
                    JOptionPane.YES_NO_CANCEL_OPTION)) {

                // zurueck zu Vennmaker
                case JOptionPane.CANCEL_OPTION:
                    break;

                case JOptionPane.CLOSED_OPTION:
                    break;

                // VennMaker ohne Speichern verlassen
                case JOptionPane.NO_OPTION:
                    vennMakerCallback.callback(null);
                    break;

                // Speicherdialog vorm Beenden aufrufen
                default:
                    if (IO.save() == 1) {
                        vennMakerCallback.callback(null);
                    }
            }
        else
            vennMakerCallback.callback(null);
    }
}

