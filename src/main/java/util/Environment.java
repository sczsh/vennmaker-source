package util;

import files.FileOperations;
import files.VMPaths;
import gui.ErrorUncaughtExceptionHandler;
import gui.Messages;
import gui.VennMaker;

import javax.swing.*;
import java.io.File;
import java.util.Locale;

public class Environment {

    public static void initializeWorkspace() {
        Thread.setDefaultUncaughtExceptionHandler(new ErrorUncaughtExceptionHandler());

        Locale.setDefault(Locale.ENGLISH);

        VennMaker.getInstance().createConfigDialogLayer();
        validateIconFolders();
        createVennMakerFolders();
        setUIManager();
    }

    private static void validateIconFolders() {
        String missingFolders = FileOperations.getMissingFolders();
        if (!missingFolders.equals("")) {
            JOptionPane.showMessageDialog(
                    null,
                    Messages.getString("VennMaker.MissingFolders.1")
                            + missingFolders
                            + Messages.getString("VennMaker.MissingFolders.2"),
                    Messages.getString("VennMaker.MissingFoldersTitle"),
                    JOptionPane.ERROR_MESSAGE);
            VennMaker.exit();
        }
    }

    private static void createVennMakerFolders() {
        File folder = new File(VMPaths.getCurrentWorkingRoot()); //$NON-NLS-1$ //$NON-NLS-2$
        folder.mkdirs();
        File tempProjects = new File(VMPaths.getCurrentWorkingDirectory());
        tempProjects.mkdir();
        FileOperations.createSubfolders(tempProjects.getAbsolutePath());
    }

    private static void setUIManager() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException exn) {
            exn.printStackTrace();
        }
    }
}
