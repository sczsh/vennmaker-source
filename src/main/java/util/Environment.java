package util;

import de.module.IModule;
import files.FileOperations;
import files.VMPaths;
import gui.ErrorUncaughtExceptionHandler;
import gui.Messages;
import gui.VennMaker;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    public static List<IModule> loadPlugins() {

        List<IModule> modules = new ArrayList<>();
        String currentRelativePath = Paths.get("").toAbsolutePath().toString();

        String modulPath = currentRelativePath + "/module/";

        String[] entries = new File(modulPath).list();

        if (entries != null)
            for (String moduleDateiName : entries) {
                if (moduleDateiName.endsWith(".jar")) {

                    File f = new File(modulPath + moduleDateiName);

                    String modulName = moduleDateiName.substring(0,
                            moduleDateiName.indexOf(".jar"));

                    URLClassLoader classLoader;
                    try {
                        classLoader = new URLClassLoader(
                                new URL[]{f.toURI().toURL()},
                                ClassLoader.getSystemClassLoader());
                        Class moduleC;
                        try {
                            /**
                             * Namenskonvention: Der Packetname ist gleich dem Modulname
                             * (z.B. vennmakerhist = vennmakerhist.jar) Main Klasse heisst
                             * immer Main (Achtung: grosses M)
                             */
                            moduleC = classLoader.loadClass("de.vennmakermodule."
                                    + modulName + ".Main");
                            IModule module = (IModule)moduleC.newInstance();
                            modules.add(module);
                            System.out.println("---------------------");
                            System.out.println("Modul: " + module.getModuleName());
                            System.out.println("Version: " + module.getVersion());

                        } catch (ClassNotFoundException e) {

                            System.out.println("Module: " + e);
                        } catch (InstantiationException e) {
                            System.out.println("Module: " + e);
                        } catch (IllegalAccessException e) {
                            System.out.println("Module: " + e);
                        }
                    } catch (MalformedURLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            }

            return modules;
    }
}
