/**
 * Diese Datei baut das Hauptgeruest des VennMakers zusammen
 * und von hier aus wird auch auf Aktionen reagiert.
 */
package gui;

import com.vennmaker.Version;
import com.thoughtworks.xstream.converters.ConversionException;
import data.*;
import data.AttributeType.Scope;
import data.EventListener;
import data.TemplateBackgroundOperations.TemplateOperation;
import de.module.IModule;
import de.module.ModuleData;
import events.*;
import exception.VennMakerMapNotFoundException;
import files.*;
import gui.configdialog.ConfigDialog;
import gui.configdialog.ConfigDialogElement;
import gui.configdialog.ConfigDialogLayer;
import gui.configdialog.elements.*;
import gui.sidemenu.VennMakerSideMenu;
import gui.utilities.StringUtilities;
import interview.InterviewController;
import interview.InterviewLayer;
import interview.elements.alter.AlterMultiAttributeOneActorElement;
import interview.elements.alter.AlterSingleAttributeDragDropElement;
import interview.elements.alter.AlterSingleAttributeFreeAnswerElement;
import interview.elements.alter.AlterSingleAttributeRadioElement;
import interview.elements.ego.*;
import interview.elements.message.DataProtectionElement;
import interview.elements.message.MaximumAlteriReachedElement;
import interview.elements.message.MinimumAlteriReachedElement;
import interview.elements.message.TextElement;
import interview.elements.meta.*;
import interview.elements.namegenerator.ExistingActorsNameGenerator;
import interview.elements.namegenerator.NameGenerator;
import interview.elements.relation.RelationGeneratorAlteriPairElement;
import interview.elements.relation.RelationGeneratorListElement;
import interview.elements.relation.RelationGeneratorPairElement;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jvnet.flamingo.common.icon.DecoratedResizableIcon;
import org.jvnet.flamingo.common.icon.ResizableIcon;
import org.jvnet.flamingo.ribbon.JRibbon;
import org.jvnet.flamingo.ribbon.RibbonTask;
import org.jvnet.flamingo.svg.SvgBatikResizableIcon;
import util.Environment;
import wizards.WizardController;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 *
 *
 *         VennMaker main window and main model class...
 *
 *
 *
 */
public class VennMaker extends JFrame {

    /**
     *
     */
    private static final int MAX_TASK_PANE_LABEL_LENGTH = 14;

    /**
     * The valid operation modes of a network's view. This influences the
     * behaviour of mouse operations in the drawing area.
     *
     *
     *
     */
    public static enum ViewMode {
        /**
         * Refer to interactions as node changes.
         */
        ALTER_NODES,

        /**
         * Refer to interactions as edge changes.
         */
        ALTER_EDGES;
    }

    /**
     * Sind wir gerade im Interview-Modus und verstecken einige GUI-Elemente?
     */
    private boolean interviewMode = false;

    /**
     * A pointer to the currently selected actor that could be added to the
     * network
     */
    private Akteur akteur;

    /**
     * A pointer to the currently selected relation type and its subtype.
     */
    private AttributeType relationTyp;

    private Object relationSubType;

    /**
     * The current interaction mode for any of the views.
     */
    private ViewMode actualButton = ViewMode.ALTER_NODES;

    private static final long serialVersionUID = 1L;

    private VennMakerView view;

    private Vector<VennMakerView> views = new Vector<VennMakerView>();

    private static JPanel visPanel;

    private Projekt projekt;

    private Config config;

    private boolean changesSaved = true;

    private OpenFileDialog openFileDialog;

    private SaveFileDialog saveFileDialog;

    // Einstellungen für den Export von Netzwerkkarten als Bild
    // Wird vor jedem Export neu gesetzt
    private int imageExportWidth,
            imageExportHeight;

    /**
     * um VennMaker als EventListener nur 1 mal zu registrieren (Sonst tritt
     * komisches Verhalten auf) (siehe Ticket #375)
     */
    private boolean eventListenerIsSet = false;

    /**
     * Die Listen enthalten die Elemente die am ConfigDialog angemeldet sind
     */

    private ArrayList<ConfigDialogElement> allElements;

    private JRibbon ribbon;

    /**
     * Gruppe der Buttons zur Akteurswahl.
     */
    private ButtonGroup akteurButtonGroup;

    /**
     * Gruppe der Buttons zur Akteurstypwahl.
     */
    private ButtonGroup akteurTypButtonGroup;

    /**
     * Gruppe der Buttons zur Relationtypwahl.
     */
    private ButtonGroup relationTypButtonGroup;

    /**
     * Der Button zum Next-Klick auf den nächsten Wizard.
     */
    private JButton nextButton;

    private Boolean nextButtonVisible = false;

    /**
     * Das halbtransparente Panel unten rechts, auf dem ggf. Next-Button und
     * Infotext angezeigt wird.
     */
    private JPanel nextPanel;

    public static String VERSION = Version.VENNMAKER_VERSION;                            //$NON-NLS-1$

    public static String REVISION = "";                                            //$NON-NLS-1$

    private static VennMaker vennMakerInstance;

    private JTabbedPane tabbedPane;

    private Map<Netzwerk, JComponent> tabs = new HashMap<Netzwerk, JComponent>();

    private Map<JComponent, Netzwerk> tabIndex = new HashMap<JComponent, Netzwerk>();

    public static final String[] LANGUAGES = {
            "English", //$NON-NLS-1$
            "Deutsch", //$NON-NLS-1$
            "\u0440\u0443\u0441\u0441\u043A\u0438\u0439", /* russian *///$NON-NLS-1$
            "Espa\u00F1ol", /* spanish *///$NON-NLS-1$
            "\u4E2D\u56FD\u7684", /* chinese *///$NON-NLS-1$
            "fran\u00e7ais"};

    /** the corresponding Locale Settings to the different languages */
    public static final Map<String, Locale> MESSAGELOCALES = new HashMap<String, Locale>() {
        {
            put("English", Locale.ENGLISH); //$NON-NLS-1$
            put("Deutsch", Locale.GERMAN); //$NON-NLS-1$
            put("\u0440\u0443\u0441\u0441\u043A\u0438\u0439", new Locale("ru", "RU")); //$NON-NLS-1$
            put("Espa\u00F1ol", new Locale("sp", "SP")); //$NON-NLS-1$
            put("\u4E2D\u56FD\u7684", new Locale("zh", "ZH")); //$NON-NLS-1$
            put("fran\u00e7ais",
                    Locale.FRENCH);
        }
    };

    /**
     * selectedRibbonTask speichert den zuletzt ausgewählten Ribbon, sodass
     * dieser bei einem refresh() des VennMakers automatisch wieder ausgewählt
     * wird
     */
    private RibbonTask selectedRibbonTask;

    /**
     * Die Listener, die nach Netzwerk-Wechsel ausgeführt werden.
     */
    private final List<VennListener> netzwerkChangeListeners;

    private MenuAction menuaction;

    private VennMakerMenu menu;

    private List<UncaughtExceptionListener> uncaughtExceptionListeners;

    private String exportDefaultPath = "";

    private static List<IModule> module = new ArrayList<>();

    /**
     * Get module
     *
     * @return IModule
     */
    public List<IModule> getModule() {
        return this.module;
    }

    public static VennMaker getInstance() {
        if (vennMakerInstance == null) {
            vennMakerInstance = new VennMaker();
        }
        return vennMakerInstance;
    }

    public static boolean isInitialized() {
        return vennMakerInstance != null;
    }

    /**
     * Checks whether the given event modifies any of the visible elements. If
     * so, the proper component will be selected and made visible. Otherwise
     * nothing happens.
     *
     * @param event
     *           Any valid VennMakerEvent, must not be <code>null</code>.
     */
    public void checkForVisibility(VennMakerEvent event) {
        if (event instanceof ActorInNetworkEvent) {
            setCurrentNetwork(((ActorInNetworkEvent) event).getNetzwerk());
        }
    }

    /**
     * Returns the export path
     *
     * @return exportPath
     */
    public String getExportPath() {
        return this.exportDefaultPath;
    }

    /**
     * Set export path
     *
     * @param path
     */
    public void setExportPath(String path) {
        this.exportDefaultPath = path;
    }

    /**
     * Makes the given network visible in the tabbed pane viewer. If no such
     * network is currently available in the tab pane then nothing happens!
     *
     * @param netz
     *           The network that should be made visible.
     */
    public void setCurrentNetwork(Netzwerk netz) {
        try {
            tabbedPane.setSelectedComponent(tabs.get(netz));
            /**
             * Needed for shortcut handling while Interview
             */
            if (nextButtonVisible)
                nextButton.requestFocusInWindow();
        } catch (IllegalArgumentException exn) {
            System.err.println(Messages.getString("Projekt.Unavailable_Network")); //$NON-NLS-1$
        }
    }

    /**
     * Creates a new main window for VennMaker
     *
     */
    private VennMaker() {
        super(Messages.getString("VennMaker.VennMaker") + VERSION); //$NON-NLS-1$

        netzwerkChangeListeners = new LinkedList<>();
        uncaughtExceptionListeners = new ArrayList<>();

        // Audiorecorder stoppen
        MediaEventList.getInstance().notify(
                new MediaEvent(this, new MediaObject(MediaObject.STOP)));

        this.addWindowListener(new VennMakerWindowCloseListener<Void>(cb -> exitVennMaker()));
        this.projekt = new Projekt();
        config = new Config();
    }

    public void exitVennMaker() {
        File tmp = new File(VMPaths.VENNMAKER_TEMPDIR);
        if (tmp.exists())
            FileOperations.deleteFolder(tmp);
        System.exit(0);
    }

    /**
     * Initialisiert das Programm mit einer Konfiguration. Dies geschieht
     * üblicherweise nach Laden einer <code>.vennEn</code>-Datei.
     *
     * @param c
     *           Eine gueltige Konfiguration.
     */
    public void setConfig(Config c) {
        config = c;

        VennMaker.getInstance().getProject().setInterviewConfig(config);

        /**
         * Initialisiere den QuestionController mit der Liste konfigurierter
         * Fragen.
         */
        QuestionController.getInstance().init(
                VennMaker.getInstance().getConfig().getQuestions());

        for (Question question : VennMaker.getInstance().getConfig()
                .getQuestions()) {
            // TODO
            if (question.getVisualMappingMethod() == Question.VisualMappingMethod.ACTOR_SIZE) {

                Integer[] sizes = new Integer[question.getVisualMapping().length];
                for (int i = 0; i < question.getVisualMapping().length; i++)
                    sizes[i] = (Integer) question.getVisualMapping()[i];

                ActorSizeVisualizer visualizer = VennMaker.getInstance()
                        .getProject().getCurrentNetzwerk().getActorSizeVisualizer();
                Vector<AttributeType> attypes = VennMaker.getInstance()
                        .getProject().getAttributeTypes();
                Object[] obj = question.getPredefinedAnswers();
                HashMap<Object, Integer> sizeMap = new HashMap<Object, Integer>();
                if (obj != null) {
                    for (int i = 0; i < obj.length; i++) {
                        sizeMap.put(obj[i], sizes[i]);
                    }
                }

                visualizer.setAttributeType(question.getAttributeType());
                visualizer.setSizes(sizeMap);
            } else if (question.getVisualMappingMethod() == Question.VisualMappingMethod.ACTOR_TYPE) {
                ActorImageVisualizer visualizer = VennMaker.getInstance()
                        .getProject().getCurrentNetzwerk().getActorImageVisualizer();
                Object[] types = question.getVisualMapping();

                Map<Object, String> images = visualizer.getImages();
                visualizer.setAttributeType(question.getAttributeType());

                for (int i = 0; i < types.length; i++) {
                    String label = ((AkteurTyp) types[i]).getBezeichnung();
                    String path = ((AkteurTyp) types[i]).getImageFile();
                    String wd = VMPaths.getCurrentWorkingDirectory();

                    if (!wd.endsWith("/")) //$NON-NLS-1$
                    {
                        wd += "/"; //$NON-NLS-1$
                    }

                    String completePath = wd + path;

                    images.put(label, completePath);
                }

                visualizer.setImages(images);
            } else if (question.getVisualMappingMethod() == Question.VisualMappingMethod.RELATION_TYPE) {
                // VennMaker.getInstance().getProject().getRelationTypen().clear();
                HashMap<Object, Color> colors = new HashMap<Object, Color>();
                HashMap<Object, float[]> dashArray = new HashMap<Object, float[]>();
                HashMap<Object, Integer> sizes = new HashMap<Object, Integer>();
                AttributeType newRelationAttribute = new AttributeType();
                Vector<String> preValues = new Vector<String>();

                newRelationAttribute.setType(question.getLabel());

                newRelationAttribute.setDescription(question.getQuestion());
                newRelationAttribute.setLabel(question.getLabel());
                newRelationAttribute.setScope(Scope.NETWORK);
                newRelationAttribute.setQuestion(question.getQuestion());

                for (Object o : question.getVisualMapping()) {
                    preValues.add(((RelationTyp) o).getBezeichnung());
                    colors.put(((RelationTyp) o).getBezeichnung(),
                            ((RelationTyp) o).getColor());
                    sizes.put(((RelationTyp) o).getBezeichnung(),
                            (int) ((RelationTyp) o).getStroke().getLineWidth());
                    dashArray.put(((RelationTyp) o).getBezeichnung(),
                            ((RelationTyp) o).getStroke().getDashArray());

                    // VennMaker.getInstance().getProject().getRelationTypen().add(
                    // (RelationTyp) o);

                }
                newRelationAttribute.setPredefinedValues(preValues.toArray());
                VennMaker.getInstance().getProject().getAttributeTypes()
                        .add(newRelationAttribute);

                VennMaker
                        .getInstance()
                        .getProject()
                        .setMainGeneratorType(question.getLabel(),
                                newRelationAttribute);
                VennMaker
                        .getInstance()
                        .getProject()
                        .setMainAttributeType(question.getLabel(),
                                newRelationAttribute);

                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .setRelationColorVisualizer(question.getLabel(),
                                newRelationAttribute, new RelationColorVisualizer());
                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .setRelationSizeVisualizer(question.getLabel(),
                                newRelationAttribute, new RelationSizeVisualizer());
                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .setRelationDashVisualizer(question.getLabel(),
                                newRelationAttribute, new RelationDashVisualizer());

                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationColorVisualizer(question.getLabel(),
                                newRelationAttribute)
                        .setAttributeType(newRelationAttribute);
                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationColorVisualizer(question.getLabel(),
                                newRelationAttribute).setColors(colors);

                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationSizeVisualizer(question.getLabel(),
                                newRelationAttribute)
                        .setAttributeType(newRelationAttribute);
                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationSizeVisualizer(question.getLabel(),
                                newRelationAttribute).setSizes(sizes);

                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationDashVisualizer(question.getLabel(),
                                newRelationAttribute)
                        .setAttributeType(newRelationAttribute);
                VennMaker
                        .getInstance()
                        .getProject()
                        .getCurrentNetzwerk()
                        .getRelationDashVisualizer(question.getLabel(),
                                newRelationAttribute).setDasharrays(dashArray);

            } else if (question.getVisualMappingMethod() == Question.VisualMappingMethod.SECTOR) {
                VennMaker.getInstance().getProject().getCurrentNetzwerk()
                        .getHintergrund()
                        .setSectorAttribute(question.getAttributeType());
            } else if (question.getVisualMappingMethod() == Question.VisualMappingMethod.CIRCLE) {
                VennMaker.getInstance().getProject().getCurrentNetzwerk()
                        .getHintergrund()
                        .setCircleAttribute(question.getAttributeType());
            }
        }
    }

    /**
     * Sets the currently selected relation type that will be used when creating
     * new relations.
     *
     * @param nummer
     *           A valid relationtype.
     */
    public void setBeziehungsart(AttributeType nummer) {
        relationTyp = nummer;
    }

    /**
     * Returns the currently selected RelationType or <code>null</code> if none
     * is defined.
     *
     * @return A valid object or <code>null</code>.
     */
    public AttributeType getBeziehungsart() {
        return relationTyp;
    }

    /**
     * Sets the currently selected relation subtype that will be used when
     * creating new relations.
     *
     * @param object
     *           A valid subtype of relationtypes.
     */
    public void setBeziehungsauspraegung(Object object) {
        relationSubType = object;
    }

    /**
     * Returns the currently selected SubRelationType or <code>null</code> if
     * none is defined.
     *
     * @return A valid object or <code>null</code>.
     */
    public Object getBeziehungsauspraegung() {
        return relationSubType;
    }

    /**
     * Sets the currently selected working mode of views.
     *
     * @param nummer
     */
    public void setActualButton(ViewMode nummer) {
        actualButton = nummer;
    }

    /**
     * Returns the currently selected working mode of views.
     *
     * @see VennMaker.ViewMode
     * @return A valid ViewMode.
     */
    public ViewMode getActualButton() {
        return actualButton;
    }

    /**
     * Returns the currently selected actor or <code>null</code> if none is
     * selected. Note that if the actor was added to the network, this value will
     * be reset to <code>null</code>.
     *
     * @return A valid actor or <code>null</code>
     */
    public Akteur getCurrentActor() {
        return akteur;
    }

    /**
     * Sets the currently selected actor that will be added if a correspondent
     * interaction is performed. Note that if the actor was added to the network,
     * this value will be reset to <code>null</code>.
     *
     * @param akt
     *           The selected actor
     */
    public void setCurrentActor(Akteur akt) {
        akteur = akt;
    }

    /**
     * Initialisiert das Programm mit einem vollständig definierten Projekt. Dies
     * erfolgt üblicherweise nach Laden einer <code>.venn</code>-Datei.
     *
     * @param p
     *           Ein gültiges Projekt.
     */
    public void setProjekt(Projekt p) {
        VennMaker.getInstance().setProject(p);

    }

    /**
     * Registriert den angegebenen Listener. Er wird informiert, sobald ein
     * anderes Netzwerk aufgerufen wurde.
     *
     * @param listener
     *           Der Listener der informiert wird, darf nicht <code>null</code>
     *           sein.
     */
    public void addNetzwerkSwitchListener(VennListener listener) {
        assert (listener != null);
        this.netzwerkChangeListeners.add(listener);
    }

    /**
     * Entfernt den angegebenen Listener.
     *
     * @param listener
     *           Der Listener, der entfernt werden soll.
     */
    public void removeNetzwerkSwitchListener(VennListener listener) {
        assert (listener != null);
        this.netzwerkChangeListeners.remove(listener);
    }

    public void refresh() {
        if (ribbon != null) {
            selectedRibbonTask = ribbon.getSelectedTask();
        }
        getContentPane().removeAll();
        createPanels();
        createMenuAndToolbar();

    }

    /**
     * Create Menu, Toolbar and the menu and toolbar action
     */
    public void createMenuAndToolbar() {

        if (menuaction == null)
            menuaction = new MenuAction();

        if (menu == null) {
            menu = new VennMakerMenu();
        }

        try {
            this.setJMenuBar(menu.createVennMakerMenu());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.remove(VennMakerMenuToolbar.getInstance().create());
        this.add(VennMakerMenuToolbar.getInstance().create(),
                BorderLayout.PAGE_START);

    }

    public static void vennmakerWindowEnabled(boolean s) {
        VennMaker.getInstance().setEnabled(s);
    }

    /**
     * load a project from a file
     *
     * @param fileToOpen
     *           file with project data
     */
    public void performLoadProject(File fileToOpen) {
        performLoadProject(fileToOpen, Projekt.load(fileToOpen.getAbsolutePath()));
    }

    /**
     * performLoadProject, if p already set
     *
     * @param fileToOpen
     * @param p
     */
    public void performLoadProject(File fileToOpen, Projekt p) {
        eventListenerIsSet = false;

        InterviewLayer.getInstance().reset();

        VennMaker.getInstance().setProject(p);
        try {
            config = Config.load(fileToOpen.getAbsolutePath().substring(0,
                    fileToOpen.getAbsolutePath().lastIndexOf(".venn")) //$NON-NLS-1$
                    + ".vennEn"); //$NON-NLS-1$
        } catch (Exception ex) {
            config = new Config();
        }

        /**
         * Verlinkungen aktualisieren
         */

        String directory = fileToOpen.getParent();
        String separator = System.getProperty("file.separator"); //$NON-NLS-1$

        if (!directory.endsWith(separator))
            directory += separator;

        VennMaker.getInstance().setCurrentWorkingDirectory(directory);
        FileOperations.changeRootFolder(VMPaths.getCurrentWorkingDirectory());

        // if (!new File(currentWorkingDirectory)
        //				.equals(new File("./projects/temp"))) //$NON-NLS-1$
        // FileOperations.resetTempFolder();

        getContentPane().remove(visPanel);
        File vmpFile = VMPaths.getVmpFile();

        if (vmpFile != null && fileToOpen.getName().endsWith(".venn")) //$NON-NLS-1$
        {
            VennMaker
                    .getInstance()
                    .setTitle(
                            Messages.getString("VennMaker.VennMaker") + VERSION //$NON-NLS-1$
                                    + " [" + vmpFile.getAbsolutePath() + " - " + fileToOpen.getName() + "]");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            VennMaker.getInstance().setTitle(
                    Messages.getString("VennMaker.VennMaker") + VERSION //$NON-NLS-1$
                            + " [" + fileToOpen.getName() + "]");//$NON-NLS-1$ //$NON-NLS-2$
        }

        VennMaker
                .getInstance()
                .getProject()
                .setCurrentNetzwerk(
                        VennMaker.getInstance().getProject().getNetzwerke().get(0));
        refresh();
        sideMenu.resetUndoRedoControls();
        VMPaths.setLastFileName(fileToOpen.getAbsolutePath());
        // if (vmpFile != null)
        // lastVisitedDirectory = vmpFile.getParent();
        // else
        //			lastVisitedDirectory = System.getProperty("user.home"); //$NON-NLS-1$
        // lastVisitedDirectory = vmpFile.getParent();

        if (VennMaker.getInstance().getProject().getCurrentInterviewConfig() != null)
            InterviewLayer.getInstance().loadInterviewFromProject(
                    VennMaker.getInstance().getProject());

        VennMaker.getInstance().setChangesSaved(true);

        //---Module---

        loadModuleData();
    }

    /**
     *
     */
    public void resetUndoRedoControls() {
        sideMenu.resetUndoRedoControls();
    }

    private JButton redoButton;

    private JButton undoButton;

    final JXTaskPane actorTypesPane = new JXTaskPane();

    final JXTaskPane relationTypesPane = new JXTaskPane();

    /**
     * Erstellt die Seitliche Ansicht mit aktuell verfügbaren und nicht im
     * aktuellen Netzwerk vorhandenen Akteuren und den zusätzlichen Icons für
     * neue Akteure aus bestimmten Typen.
     *
     * @return Ein JXTaskPaneContainer, der die Icons enthält.
     */
    VennMakerSideMenu sideMenu = new VennMakerSideMenu();

    boolean useNewSideMenu = false;

    private JXTaskPaneContainer createActorsTaskBar() {
        if (useNewSideMenu) {
            this.addNetzwerkSwitchListener(new VennListener() {
                public void update() {
                    sideMenu.update();
                }
            });
            JXTaskPaneContainer r = sideMenu.retrieve();

            return r;
        } else {
            final JXTaskPaneContainer container = new JXTaskPaneContainer();

            final JXTaskPane actionPane = new JXTaskPane();
            actionPane.setTitle(Messages.getString("VennMaker.MenuEditAction")); //$NON-NLS-1$
            actionPane.setLayout(new GridLayout(0, 2));
            container.add(actionPane);

            try {
                SvgBatikResizableIcon icon = SvgBatikResizableIcon
                        .getSvgIcon(
                                new FileInputStream(FileOperations
                                        .getAbsolutePath(Messages
                                                .getString("VennMaker.Icons_EditUndo"))), new Dimension(32, //$NON-NLS-1$
                                        32));
                undoButton = new JButton(
                        Messages.getString("VennMaker.MenuEditUndo"), icon); //$NON-NLS-1$
                undoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        EventProcessor.getInstance().undoEvent();
                    }
                });
                actionPane.add(undoButton);
                // undoButton.setState(ElementState.MEDIUM, false);

                icon = SvgBatikResizableIcon
                        .getSvgIcon(
                                new FileInputStream(FileOperations
                                        .getAbsolutePath(Messages
                                                .getString("VennMaker.Icons_EditRedo"))), new Dimension(32, 32)); //$NON-NLS-1$
                redoButton = new JButton(
                        Messages.getString("VennMaker.MenuEditRedo"), icon); //$NON-NLS-1$
                redoButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        EventProcessor.getInstance().redoEvent();
                    }
                });
                actionPane.add(redoButton);
                // redoButton.setState(ElementState.MEDIUM, false);

                resetUndoRedoControls();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

			/* Vektor zum Durchlaufen aller AttributeCollectors */
            final Vector<String> attributeTypeCollectors = VennMaker.getInstance()
                    .getProject().getAttributeCollectors();

            final JXTaskPane actorsPane = new JXTaskPane();
            final JXTaskPane actorTypesPane = new JXTaskPane();
            final JXTaskPane[] relationTypesPane = new JXTaskPane[attributeTypeCollectors
                    .size()];

            actorsContainer(actorsPane);
            container.add(actorsPane);

            actorTypesContainer(actorTypesPane);
            actorTypesPane.setCollapsed(false);

            if (VennMaker.getInstance().getProject().getMainGeneratorType("ACTOR") != null) //$NON-NLS-1$
                container.add(actorTypesPane);
            else
                container.remove(actorTypesPane);

            for (int i = 0; i < attributeTypeCollectors.size(); i++) {
                final int currentI = i;
                AttributeType a = VennMaker.getInstance().getProject()
                        .getMainGeneratorType(attributeTypeCollectors.get(currentI));
                if (a == null || a.getPredefinedValues() == null)
                    continue;
                relationTypesPane[currentI] = new JXTaskPane();
                relationTypesContainer(relationTypesPane[currentI],
                        attributeTypeCollectors.get(currentI));
                container.add(relationTypesPane[currentI]);

                EventProcessor.getInstance().addEventPerformedListener(
                        new EventPerformedListener() {
                            @Override
                            public void eventConsumed(VennMakerEvent event) {
                                VennMaker.getInstance().setChangesSaved(false);
                                if ((event instanceof ActorEvent)
                                        || (event instanceof ActorInNetworkEvent)
                                        || (event instanceof NetworkEvent)) {
                                    // Könnte noch feiner filtern...
                                    boolean unrollAgain = actorsPane.isCollapsed();
                                    actorsPane.removeAll();
                                    actorsContainer(actorsPane);
                                    container.invalidate();
                                    actorsPane.setCollapsed(unrollAgain);
                                } else if ((event instanceof RelationTypeEvent)) {
                                    boolean unrollAgain = relationTypesPane[currentI]
                                            .isCollapsed();
                                    relationTypesPane[currentI].removeAll();
                                    relationTypesContainer(relationTypesPane[currentI],
                                            attributeTypeCollectors.get(currentI));
                                    container.invalidate();
                                    relationTypesPane[currentI]
                                            .setCollapsed(unrollAgain);
                                }
                            }

                        });
            }

            // Beim Netzwerkwechsel muss die Liste der Akteure aktualisiert werden.
            this.addNetzwerkSwitchListener(new VennListener() {
                public void update() {
                    boolean unrollAgain = actorsPane.isCollapsed();
                    boolean unrollAgain2 = actorTypesPane.isCollapsed();
                    actorsPane.removeAll();
                    actorsContainer(actorsPane);
                    actorTypesPane.removeAll();
                    actorTypesContainer(actorTypesPane);
                    actorTypesPane.setCollapsed(unrollAgain2);
                    if (VennMaker.getInstance().getProject()
                            .getMainGeneratorType("ACTOR") != null) //$NON-NLS-1$
                        container.add(actorTypesPane);
                    else
                        container.remove(actorTypesPane);
                    container.invalidate();
                    actorsPane.setCollapsed(unrollAgain);
                }
            });

            container.setMaximumSize(new Dimension(100, 50));
            container.setMinimumSize(new Dimension(100, 50));

            container.setScrollableTracksViewportHeight(false);
            container.setScrollableTracksViewportWidth(false);

            return container;
        }
    }

    private Map<String, ResizableIcon> iconCache = new HashMap<String, ResizableIcon>();

    /**
     * @param actorTypesPane
     */
    @Deprecated
    private void actorTypesContainer(final JXTaskPane actorTypesPane) {
        assert VennMaker.getInstance().getProject().getMainGeneratorType("ACTOR") != null; //$NON-NLS-1$
        assert VennMaker.getInstance().getProject()
                .getMainGeneratorType("ACTOR").getPredefinedValues() != null; //$NON-NLS-1$

        int i;
        AttributeType mainGenType = VennMaker.getInstance().getProject()
                .getMainGeneratorType("ACTOR"); //$NON-NLS-1$
        if (mainGenType == null)
            return;
        actorTypesPane.setTitle(StringUtilities.truncate(mainGenType.getLabel()));
        actorTypesPane.setToolTipText(mainGenType.getLabel());
        actorTypesPane.setCollapsed(true);
        actorTypesPane.setLayout(new GridLayout(0, 1));
        akteurTypButtonGroup = new ButtonGroup();
        i = 0;

        String filename = "";
        if (VennMaker.getInstance().getProject().getMainGeneratorType("ACTOR")
                .getPredefinedValues() != null)
            for (Object value : VennMaker.getInstance().getProject()
                    .getMainGeneratorType("ACTOR").getPredefinedValues()) //$NON-NLS-1$
            {
                ResizableIcon finalIcon;
                JToggleButton jrb;
                try {
                    filename = VennMaker.getInstance().getProject()
                            .getCurrentNetzwerk().getActorImageVisualizer()
                            .getImage(value);
                    if (iconCache.containsKey(filename)) {
                        finalIcon = iconCache.get(filename);
                    } else {
                        // check if svg or jpg/png
                        String name = filename.toLowerCase();
                        if (name.endsWith(".svg"))
                            finalIcon = SvgBatikResizableIcon
                                    .getSvgIcon(new FileInputStream(filename),
                                            new Dimension(32, 32));
                        else if (!name.equals(""))
                            finalIcon = new ResizableImageIcon(new ImageIcon(
                                    ImageOperations.loadActorImage(filename, 32, 1)));
                        else
                            finalIcon = new ResizableImageIcon(new ImageIcon(
                                    ImageOperations.createActorIcon(null, 32, 1)));

                        if (finalIcon == null)
                            throw new IOException("Cant load Icon");
                    }
                } catch (Exception exn) {
                    BufferedImage img = new BufferedImage(32, 32,
                            BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D g = img.createGraphics();
                    g.setColor(Color.gray);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setStroke(new BasicStroke(3.0f));
                    g.draw(new Ellipse2D.Double(4, 4, 24, 24));
                    g.drawLine(4, 16, 28, 16);
                    g.drawLine(16, 4, 16, 28);
                    final ImageIcon icon = new ImageIcon(img);
                    finalIcon = new ResizableImageIcon(icon);
                }
                jrb = new JToggleButton(StringUtilities.truncate(value.toString()),
                        finalIcon);
                jrb.setHorizontalAlignment(SwingConstants.LEFT);
                jrb.setToolTipText(value.toString());
                final int j = i;
                final String fileNameIcon = filename;
                jrb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setCurrentActor(null);
                        if (VennMaker.getInstance().getProject()
                                .getMainGeneratorType("ACTOR").getPredefinedValues()[j] != null) //$NON-NLS-1$
                            setMainGeneratorValue(VennMaker.getInstance().getProject()
                                    .getMainGeneratorType("ACTOR") //$NON-NLS-1$
                                    .getPredefinedValues()[j]);
                        setActualButton(ViewMode.ALTER_NODES);

                        // Akteurbuttons deaktivieren
                        akteurButtonGroup.clearSelection();

                        // Relationtyp-Buttons deaktivieren
                        if (relationTypButtonGroup != null)
                            relationTypButtonGroup.clearSelection();

                        MenuObject actorObject = new MenuObject();
                        actorObject.setMessage("ButtonActorAttributeSelected");
                        MenuEventList.getInstance().notify(
                                new MenuEvent(this, actorObject));
                    }
                });

                akteurTypButtonGroup.add(jrb);
                actorTypesPane.add(jrb);
                if (i == 0)
                    jrb.doClick();
                i++;
            }
    }

    /**
     * Um "changesSaved" bei Aenderungen die kein Event haben anzusprechen. (zb.
     * bei geanderten Meta Informationen)
     **/
    public void setChangesUnsaved() {
        VennMaker.getInstance().setChangesSaved(false);
    }

    public void updateRelationsPanel() {
        repaint();
    }

    @Deprecated
    private void relationTypesContainer(final JXTaskPane relationTypesPane,
                                        String attributeCollector) {
        int i;

		/*
         * Wenn generatorAttributeType noch existiert, dann uebernimm dieses,
		 * falls nicht, nimm erstes Attribut im aktuellen Collector
		 */
        final AttributeType generator = VennMaker
                .getInstance()
                .getProject()
                .getAttributeTypes(attributeCollector)
                .contains(
                        VennMaker.getInstance().getProject()
                                .getMainGeneratorType(attributeCollector)) ? VennMaker
                .getInstance().getProject()
                .getMainGeneratorType(attributeCollector) : VennMaker.getInstance()
                .getProject().getAttributeTypes(attributeCollector).firstElement();

        String attributeLabel = VennMaker.getInstance().getProject()
                .getMainGeneratorType(attributeCollector).getLabel();
        String paneTitel = attributeLabel + " (";

        if (attributeCollector.equals("STANDARDRELATION"))
            paneTitel += Messages.getString("AttributeCollector.StandardRelation");//$NON-NLS-1$
        else
            paneTitel += attributeCollector;

        paneTitel += ")";

        relationTypesPane.setTitle(StringUtilities.truncate(paneTitel)); //$NON-NLS-1$
        relationTypesPane.setCollapsed(false);
        relationTypesPane.setLayout(new GridLayout(0, 1));
        relationTypesPane.setToolTipText(paneTitel);

		/* verhindert mehrere ButtonGroups */
        if (relationTypButtonGroup == null)
            relationTypButtonGroup = new ButtonGroup();
        i = 0;
        if (generator.getPredefinedValues() != null)
            for (final Object relationTyp : generator.getPredefinedValues()) {
                try {
                    ResizableIcon fontIcon;
                    fontIcon = SvgBatikResizableIcon
                            .getSvgIcon(
                                    new FileInputStream(FileOperations
                                            .getAbsolutePath(Messages
                                                    .getString("VennMaker.Icon_Null"))), new Dimension(32, //$NON-NLS-1$
                                            32));
                    ResizableIcon finalIcon = new DecoratedResizableIcon(fontIcon,
                            new DecoratedResizableIcon.IconDecorator() {
                                public void paintIconDecoration(Component c,
                                                                Graphics g, int x, int y, int width, int height) {

                                    AttributeType currentGenerator = null;

                                    if (VennMaker.getInstance().getProject()
                                            .getAttributeTypes().size() == 0)
                                        return;

                                    for (AttributeType att : VennMaker.getInstance()
                                            .getProject().getAttributeTypes()) {
                                        if (att.getLabel().equals(generator.getLabel())) {
                                            currentGenerator = att;
                                            break;
                                        }
                                    }

                                    if (currentGenerator == null)
                                        currentGenerator = VennMaker.getInstance()
                                                .getProject().getAttributeTypes().get(0);

                                    Graphics2D g2d = (Graphics2D) g.create();
                                    g2d.setRenderingHint(
                                            RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);
                                    g2d.setPaint(VennMaker
                                            .getInstance()
                                            .getProject()
                                            .getCurrentNetzwerk()
                                            .getRelationColorVisualizer(
                                                    currentGenerator.getType(),
                                                    currentGenerator).getColor(relationTyp));

                                    float size = VennMaker
                                            .getInstance()
                                            .getProject()
                                            .getCurrentNetzwerk()
                                            .getRelationSizeVisualizer(
                                                    currentGenerator.getType(),
                                                    currentGenerator).getSize(relationTyp);
                                    float[] dashArray = VennMaker
                                            .getInstance()
                                            .getProject()
                                            .getCurrentNetzwerk()
                                            .getRelationDashVisualizer(
                                                    currentGenerator.getType(),
                                                    currentGenerator)
                                            .getDasharray(relationTyp);

                                    float strokeWidth = VennMakerView.getVmcs()
                                            .toJava2D(
                                                    size / VennMakerView.LINE_WIDTH_SCALE);

                                    BasicStroke stroke = new BasicStroke();

                                    // BUGFIX (unter MacOS kommt es oefter zu einer
                                    // negativen strokeWidth
                                    // und damit zu einer Exception)
                                    if (strokeWidth > 0)
                                        stroke = new BasicStroke(strokeWidth, stroke
                                                .getEndCap(), stroke.getLineJoin(), stroke
                                                .getMiterLimit(), dashArray, stroke
                                                .getDashPhase());

                                    g2d.setStroke(stroke);
                                    if (VennMaker.getInstance().getProject()
                                            .getIsDirected(currentGenerator.getType())) {
                                        ArrowLine lineA = new ArrowLine(60, 32, 10, 10);
                                        g2d.draw(lineA);
                                    } else {
                                        Line2D.Double line = new Line2D.Double(10, 10,
                                                60, 32);
                                        g2d.draw(line);
                                    }
									/*
									 * switch (typ.getDirectionType()) { case DIRECTED:
									 * ArrowLine lineA = new ArrowLine(60, 32, 10, 10);
									 * g2d.draw(lineA); break; case UNDIRECTED:
									 * Line2D.Double line = new Line2D.Double(10, 10, 60,
									 * 32); g2d.draw(line); }
									 */

                                }
                            });
                    JToggleButton jrb = new JToggleButton(
                            StringUtilities.truncate((String) relationTyp), finalIcon);
                    jrb.setHorizontalAlignment(SwingConstants.LEFT);

                    AttributeType currentGenerator = null;

                    for (AttributeType att : VennMaker.getInstance().getProject()
                            .getAttributeTypes()) {
                        if (att.getLabel().equals(generator.getLabel())) {
                            currentGenerator = att;
                            break;
                        }
                    }

                    if (currentGenerator == null)
                        currentGenerator = VennMaker.getInstance().getProject()
                                .getAttributeTypes().get(0);

                    final AttributeType gen = currentGenerator;

                    jrb.setToolTipText((String) relationTyp);
                    final int j = i;
                    jrb.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            setBeziehungsart(gen);
                            setBeziehungsauspraegung(relationTyp);
                            setActualButton(ViewMode.ALTER_EDGES);

                            // Akteurtypbuttons deaktivieren
                            akteurTypButtonGroup.clearSelection();

                            // Akteurbuttons deaktivieren
                            akteurButtonGroup.clearSelection();

                            MenuEventList.getInstance().notify(
                                    new MenuEvent(this, new MenuObject(
                                            "ButtonRelationAttributeSelected")));
                        }
                    });
                    relationTypButtonGroup.add(jrb);
                    relationTypesPane.add(jrb);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                i++;
            }

    }

    /**
     * @param actorsPane
     */
    @Deprecated
    private void actorsContainer(final JXTaskPane actorsPane) {
        // Soll hervorgehoben werden.
        actorsPane.setSpecial(true);

        actorsPane.setLayout(new GridLayout(0, 1));
        int i = 0;
        int num = 0;
        akteurButtonGroup = new ButtonGroup();
        for (final Akteur akteur : VennMaker.getInstance().getProject()
                .getAkteure()) {
            if (!VennMaker.getInstance().getProject().getCurrentNetzwerk()
                    .getAkteure().contains(akteur)
                    && VennMaker.getInstance().getProject().getEgo() != akteur) {
                ResizableIcon finalIcon = null;
                try {
                    String filename = VennMaker
                            .getInstance()
                            .getProject()
                            .getCurrentNetzwerk()
                            .getActorImageVisualizer()
                            .getImage(
                                    akteur,
                                    VennMaker.getInstance().getProject()
                                            .getCurrentNetzwerk());

                    if (iconCache.containsKey(filename)) {
                        finalIcon = iconCache.get(filename);
                    } else {
                        // check if svg or jpg/png
                        String name = filename.toLowerCase();
                        if (name.endsWith(".svg")) {
                            finalIcon = SvgBatikResizableIcon
                                    .getSvgIcon(new FileInputStream(filename),
                                            new Dimension(32, 32));
                        } else {
                            if (name.equals(""))
                                finalIcon = new ResizableImageIcon(new ImageIcon(
                                        ImageOperations.createActorIcon(null, 32, 1)));
                            else
                                finalIcon = new ResizableImageIcon(new ImageIcon(
                                        ImageOperations.loadActorImage(filename, 32, 1)));
                        }
                        iconCache.put(filename, finalIcon);
                    }
                } catch (FileNotFoundException exn) {
                    // exn.printStackTrace();
                }
                final JToggleButton jrb = new JToggleButton(
                        StringUtilities.truncate(akteur.getName()), finalIcon);
                jrb.setHorizontalAlignment(SwingConstants.LEFT);
                akteurButtonGroup.add(jrb);
                jrb.setToolTipText(akteur.getName());
                jrb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setCurrentActor(akteur);/* projekt.getAkteure().get(j) */
                        setActualButton(ViewMode.ALTER_NODES);

                        // Akteurtypbuttons deaktivieren
                        akteurTypButtonGroup.clearSelection();

                        // Relationtyp-Buttons deaktivieren
                        relationTypButtonGroup.clearSelection();
                    }
                });

				/*
				 * mouselistener to remove an actor from the interview
				 */
                jrb.addMouseListener(new MouseAdapter() {
                    /** create a Popup to delete the current actor from the project */
                    private void showDeletePopup(int x, int y) {
                        JPopupMenu popup = new JPopupMenu();
                        popup.setInvoker(jrb);
                        JMenuItem deleteActorItem = new JMenuItem(Messages
                                .getString("VennMaker.Removing_2")); //$NON-NLS-1$
                        popup.add(deleteActorItem);

                        deleteActorItem.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                ComplexEvent ce = new ComplexEvent(Messages
                                        .getString("VennMaker.Removing_2")); //$NON-NLS-1$
                                for (Netzwerk n : akteur.getNetzwerke())
                                    ce.addEvent(new RemoveActorEvent(akteur, n, akteur
                                            .getLocation(n)));
                                ce.addEvent(new DeleteActorEvent(akteur));
                                EventProcessor.getInstance().fireEvent(ce);
                            }

                        });

                        popup.setLocation(x, y);
                        popup.setVisible(true);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
						/* show popupwindow when the button is rightclicked */
                        if (e.isPopupTrigger()) {
                            showDeletePopup(e.getXOnScreen(), e.getYOnScreen());
                        }
                    }

                    @Override
                    /** codeclone for crossplatform compatibity
                     * Linux: mousePressed
                     * Windows: mouseReleased
                     */
                    public void mouseReleased(MouseEvent e) {
						/* show popupwindow when the button is rightclicked */
                        if (e.isPopupTrigger()) {
                            showDeletePopup(e.getXOnScreen(), e.getYOnScreen());
                        }
                    }
                });

                actorsPane.add(jrb);
                if (i == 0)
                    jrb.doClick();
                num++;
            }
            i++;
        }

        String actortitle = "";
        if (num > 0)
            actortitle = "" + num + " ";
        if (num == 1)
            actortitle += Messages.getString("VennMaker.Available_Actor");
        else
            actortitle += Messages.getString("VennMaker.Available_Actors");

        actorsPane.setTitle(actortitle); //$NON-NLS-1$

        // Alle Akteure gesetzt?
        if (num == 0) {
            // Akteurstypen ausklappen
            actorTypesPane.setCollapsed(false);

            actorsPane.setVisible(false);
        } else {
            actorTypesPane.setCollapsed(true);
            actorsPane.setVisible(true);
        }
    }

    private void createPanels() {
        /**
         * LayeredPane zum Übereinanderlegen des Zeichenebereichs und bestimmter
         * Oberflächenelemente
         */
        JLayeredPane layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane, BorderLayout.CENTER);

        ResizableIcon icon;
        GridBagConstraints gbc;
        GridBagLayout layerLayout = new GridBagLayout();
        layeredPane.setLayout(layerLayout);
        try {
            visPanel = createViewersTabbedPane();
        } catch (VennMakerMapNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.gridheight = 5;
        gbc.weightx = 2.0;
        gbc.weighty = 2.0;
        layerLayout.setConstraints(visPanel, gbc);
        layeredPane.add(visPanel);
        layeredPane.setLayer(visPanel, JLayeredPane.DEFAULT_LAYER);

        /**
         * Knopf zum Umschalten der Ribbon-Sichtbarkeit
         */
		/*
		 * icon = null; try { icon = SvgBatikResizableIcon .getSvgIcon( new
		 * FileInputStream(FileOperations .getAbsolutePath(Messages
		 * .getString("VennMaker.Icons_View_area"))), new Dimension(32, 32));
		 * //$NON-NLS-1$ } catch (IOException e) { e.printStackTrace(); }
		 * JCommandButton toggleRibbonButton = new JCommandButton(
		 * "Toggle visibility of ribbons", icon); //$NON-NLS-1$
		 * toggleRibbonButton.setToolTipText("Toggle visibility of ribbons");
		 * //$NON-NLS-1$ toggleRibbonButton.setFlat(false);
		 * toggleRibbonButton.setState(ElementState.SMALL, false);
		 * toggleRibbonButton.setPreferredSize(new Dimension(40, 40));
		 * toggleRibbonButton.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent event) { if (ribbon.isVisible())
		 * ribbon.setVisible(false); else ribbon.setVisible(true); } }); gbc = new
		 * GridBagConstraints(); gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
		 * gbc.gridheight = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.insets =
		 * new Insets(20, 0, 0, 30); gbc.anchor = GridBagConstraints.NORTHEAST;
		 * layerLayout.setConstraints(toggleRibbonButton, gbc);
		 * layeredPane.add(toggleRibbonButton);
		 * layeredPane.setLayer(toggleRibbonButton, JLayeredPane.MODAL_LAYER);
		 */
        /**
         * halbtransparentes Panel mit Next-Button und Infotext
         */
        nextPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Composite alphaComp = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.6f);
                g2d.setComposite(alphaComp);
                g2d.setColor(getBackground());
                Rectangle tBounds = g2d.getClip().getBounds();
                g2d.fillRect((int) tBounds.getX(), (int) tBounds.getY(),
                        (int) tBounds.getWidth(), (int) tBounds.getHeight());
                super.paintComponent(g2d);
            }
        };
        nextPanel.setOpaque(true);
        nextPanel.setPreferredSize(new Dimension(180, 50));
        nextPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 50, 30);
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        layerLayout.setConstraints(nextPanel, gbc);
        layeredPane.add(nextPanel);
        layeredPane.setLayer(nextPanel, JLayeredPane.MODAL_LAYER);

        GridBagLayout nextPanelLayout = new GridBagLayout();
        nextPanel.setLayout(nextPanelLayout);

        /**
         * Next-Button
         */
        icon = null;
        try {
            icon = SvgBatikResizableIcon
                    .getSvgIcon(
                            new FileInputStream(FileOperations
                                    .getAbsolutePath(Messages
                                            .getString("VennMaker.Icon_Next"))), new Dimension(48, 48)); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
        }

        String nextText = "<html><b>" + Messages.getString("InterviewController.Next") + " </b><br/><i>(F3)</html>"; //$NON-NLS-1$
        String prevText = "<html><b> " + Messages.getString("InterviewController.Prev") + "</b><br/><i>(F2)</html>"; //$NON-NLS-1$

        nextButton = new JButton(nextText, //$NON-NLS-1$
                icon);
        nextButton.setHorizontalTextPosition(JButton.LEFT);
        nextButton.setVisible(nextButtonVisible);

        nextPanel.setVisible(nextButtonVisible);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.SOUTH;
        nextPanelLayout.setConstraints(nextButton, gbc);
        nextPanel.add(nextButton);

        /**
         * Previous
         */

        JPanel previousPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Composite alphaComp = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.6f);
                g2d.setComposite(alphaComp);
                g2d.setColor(getBackground());
                Rectangle tBounds = g2d.getClip().getBounds();
                g2d.fillRect((int) tBounds.getX(), (int) tBounds.getY(),
                        (int) tBounds.getWidth(), (int) tBounds.getHeight());
                super.paintComponent(g2d);
            }
        };
        previousPanel.setLayout(new GridBagLayout());
        previousPanel.setOpaque(true);
        previousPanel.setPreferredSize(new Dimension(180, 50));
        previousPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 50, 30);
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        layerLayout.setConstraints(previousPanel, gbc);
        layeredPane.add(previousPanel);
        layeredPane.setLayer(previousPanel, JLayeredPane.MODAL_LAYER);

        /**
         * Prev
         */
        icon = null;
        try {
            icon = SvgBatikResizableIcon
                    .getSvgIcon(
                            new FileInputStream(FileOperations
                                    .getAbsolutePath("icons/intern/go-previous.svg")), new Dimension(48, 48)); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton previousButton = new JButton(prevText, icon);
        previousButton.setVisible(nextButtonVisible);
        previousButton.setHorizontalTextPosition(JButton.RIGHT);
        previousPanel.setVisible(nextButtonVisible);

        /**
         * Shortcuts an Next und Previous haengen
         */

        /**
         * add ActionListener and shortcuts to buttons
         */
        PrevListener pl = new PrevListener();
        previousButton.setText(prevText);
        previousButton.addActionListener(pl);
        previousButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F2"), prevText);
        previousButton.getActionMap().put(prevText, pl);

        NextListener nl = new NextListener();
        nextButton.setText(nextText);
        nextButton.addActionListener(nl);
        nextButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F3"), nextText);
        nextButton.getActionMap().put(nextText, nl);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.SOUTH;
        previousPanel.add(previousButton, gbc);

        final JXTaskPaneContainer actorsTypeBar = createActorsTaskBar();
        getContentPane().add(new JScrollPane(actorsTypeBar), BorderLayout.WEST);
        invalidate();
    }

    class NextListener extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (nextButtonVisible)
                InterviewController.getInstance().next();
        }
    }

    class PrevListener extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (nextButtonVisible)
                InterviewController.getInstance().previous();
        }
    }

    /**
     * Erzeugt eine <code>JTabbedPane</code> die alle Netzwerksichten beinhaltet.
     *
     * @return Ein Panel, dass die JTabbedPane enthält.
     * @throws VennMakerMapNotFoundException
     */
    public JPanel createViewersTabbedPane() throws VennMakerMapNotFoundException {
        JPanel panel = new JPanel();
        tabbedPane = new DDTabPane();

        // noetig um die alten Einstellungen der View zu übernehmen
        Vector<VennMakerView> oldViews = (Vector<VennMakerView>) views.clone();

        views.clear();
        tabIndex.clear();
        tabs.clear();

        int i = 1;
        if (VennMaker.getInstance().getProject().getNetzwerke().size() > 0) {
            for (Netzwerk n : VennMaker.getInstance().getProject().getNetzwerke()) {

				/*
				 * Alte Eigenschaften der View in die neue übernehmen (im Moment nur
				 * "drawLegend")**********************************************
				 */
                boolean oldSettingdrawLegend = true;
                boolean oldSettingDrawNetName = true;
                int oldSettingDrawNetNameOffset = -1;

                for (VennMakerView oldV : oldViews) {
                    if (oldV.getNetzwerk().equals(n)) {
                        oldSettingdrawLegend = oldV.drawLegend;
                        oldSettingDrawNetName = oldV.drawNetworkName;
                        oldSettingDrawNetNameOffset = oldV.drawNetworkNameOffset;
                    }
                }
                VennMakerView v = new VennMakerView(n);
                v.drawLegend = oldSettingdrawLegend;
                v.drawNetworkName = oldSettingDrawNetName;
                v.drawNetworkNameOffset = oldSettingDrawNetNameOffset;
                // ***********************************************

                JComponent p = createVisualizationPanel(v);
                tabbedPane.addTab(n.getBezeichnung(), p);

                tabs.put(n, p);
                tabIndex.put(p, n);

                // tabbedPane.setMnemonicAt(i, KeyEvent.VK_1);

                views.add(v);
                i++;
            }
        }
        oldViews.clear();

        try {
            // Auf die erste Netzwerkkarte wechseln
            setActualVennMakerView(views.get(0));
        } catch (ArrayIndexOutOfBoundsException exn) {
            System.err.println(Messages.getString("Projekt.Unavailable_Network")); //$NON-NLS-1$
            VennMakerActions.addNewMap();
        }

        ChangeListener listener = new VennMakerActualViewListener();
        tabbedPane.addChangeListener(listener);
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Listener für Kontextmenü
        tabbedPane.addMouseListener(new MouseListener() {
            public void mouseEntered(final MouseEvent e) {
            }

            public void mouseExited(final MouseEvent e) {
            }

            public void mouseClicked(final MouseEvent e) {
            }

            public void mouseReleased(final MouseEvent e) {
            }

            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    final int i = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (i == -1)
                        return; // falls kein Tab angeklickt wurde folgt return
                    final Component p = tabbedPane.getComponentAt(i);
                    JPopupMenu emptyMenu = new JPopupMenu(Messages
                            .getString("Projekt.Netzwerk")); //$NON-NLS-1$
                    final JMenuItem deleteItem = new JMenuItem(Messages
                            .getString("VennMaker.Delete_network")); //$NON-NLS-1$
                    if (VennMaker.getInstance().getProject().getNetzwerke().size() == 1)
                        deleteItem.setEnabled(false);
                    else
                        deleteItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                // entferne Netzwerk
                                Netzwerk n = tabIndex.get(p);
                                DeleteNetworkEvent event = new DeleteNetworkEvent(n);
                                EventProcessor.getInstance().fireEvent(event);
                                tabIndex.remove(p);
                                tabs.remove(n);
                            }
                        });
                    final JMenuItem renameItem = new JMenuItem(Messages
                            .getString("VennMaker.Rename_network")); //$NON-NLS-1$
                    renameItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // benenne Netzwerk um
                            // kein eigenes Event
                            Netzwerk n = tabIndex.get(p);
                            String newBezeichnung = (String) JOptionPane.showInputDialog(
                                    VennMaker.getInstance(),
                                    Messages.getString("VennMaker.network_name"), Messages //$NON-NLS-1$
                                            .getString("VennMaker.network_editName"), //$NON-NLS-1$
                                    JOptionPane.QUESTION_MESSAGE, null, null,
                                    n.getBezeichnung());

                            if (newBezeichnung != null) {
                                if (newBezeichnung.isEmpty()
                                        || newBezeichnung.equals("")) {
                                    JOptionPane.showMessageDialog(VennMaker
                                                    .getInstance(), Messages
                                                    .getString("VennMaker.Empty_Network_Name"),
                                            "Invalid network name",
                                            JOptionPane.ERROR_MESSAGE); // $NON-NLS-1$
                                    return;
                                }

                                // name already in use
                                if (!checkNetworkName(newBezeichnung)) {
                                    return;
                                }
                                n.setBezeichnung(newBezeichnung);
                                tabbedPane.setTitleAt(i, newBezeichnung);
                                tabbedPane.setToolTipTextAt(i, newBezeichnung);
                                return;
                            }
                        }
                    });

                    final JMenuItem cloneItem = new JMenuItem(Messages
                            .getString("VennMaker.Network_Clone")); //$NON-NLS-1$
                    cloneItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Netzwerk net = tabIndex.get(p);
                            String name = JOptionPane.showInputDialog(Messages
                                    .getString("ConfigDialog.30")); //$NON-NLS-1$
                            if (name != null) { //$NON-NLS-1$
                                if (name.equals("") && name.isEmpty()) {
                                    JOptionPane.showMessageDialog(
                                            VennMaker.getInstance(),
                                            Messages
                                                    .getString("VennMaker.Empty_Network_Name"),
                                            Messages
                                                    .getString("VennMaker.Empty_Network_Name_Title"),
                                            JOptionPane.ERROR_MESSAGE); // $NON-NLS-1$
                                    return;
                                }

                                // name already in use
                                if (!checkNetworkName(name)) {
                                    return;
                                }
                                net.cloneNetwork(name);
                            }
                        }
                    });

                    final JMenuItem mergeItem = new JMenuItem("Merge Network"); //$NON-NLS-1$
                    mergeItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Netzwerk netSource = tabIndex.get(tabbedPane
                                    .getComponentAt(0));

                            Netzwerk net = tabIndex.get(p);
                            String name = JOptionPane.showInputDialog(Messages
                                    .getString("ConfigDialog.30")); //$NON-NLS-1$
                            Netzwerk netDestination = net.cloneNetwork(name);

                            netDestination.mergeNetwork(netSource);
                        }
                    });

                    final JMenuItem addEmptyItem = new JMenuItem(Messages
                            .getString("VennMaker.Network_AddEmpty")); //$NON-NLS-1$
                    addEmptyItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String name = JOptionPane.showInputDialog(Messages
                                    .getString("ConfigDialog.30")); //$NON-NLS-1$

                            if (name != null) { //$NON-NLS-1$
                                if (name.equals("") && name.isEmpty()) {
                                    JOptionPane.showMessageDialog(
                                            VennMaker.getInstance(),
                                            Messages
                                                    .getString("VennMaker.Empty_Network_Name"),
                                            Messages
                                                    .getString("VennMaker.Empty_Network_Name_Title"),
                                            JOptionPane.ERROR_MESSAGE); // $NON-NLS-1$
                                    return;
                                }

                                if (!checkNetworkName(name))
                                    return;

                                Netzwerk netzwerk = tabIndex.get(p).getNewNetwork(name);
                            }
                        }
                    });
                    emptyMenu.add(renameItem);
                    emptyMenu.addSeparator();
                    emptyMenu.add(cloneItem);
                    emptyMenu.add(addEmptyItem);
                    // emptyMenu.add(mergeItem);
                    emptyMenu.addSeparator();
                    emptyMenu.add(deleteItem);

                    emptyMenu.show(tabbedPane, (int) e.getX(), (int) e.getY());
                }
            }
        });

        // Add the tabbed pane to this panel.
        panel.add(tabbedPane);
        panel.setLayout(new GridLayout(1, 1));

        if (!eventListenerIsSet) {
            // Event-Listener für GUI-Aktualisierungen
            EventProcessor.getInstance().addEventPerformedListener(
                    new EventPerformedListener() {
                        @Override
                        public void eventConsumed(VennMakerEvent event) {
                            // Neues Netzwerk: erzeuge zugehörigen View, mache
                            // ihn
                            // aktiv,
                            // füge Tab hinzu
                            if (event instanceof NewNetworkEvent) {
                                final NewNetworkEvent event2 = (NewNetworkEvent) event;
                                VennMakerView v = new VennMakerView(event2
                                        .getNetzwerk());
                                views.add(v);

								/*
								 * Wenn es sich um einen Clone handelt werden die
								 * Eigenschaften der View mitkopiert (im Moment nur
								 * "showLegend")
								 */
                                if (event2.isClone()) {
                                    Netzwerk father = event2.getCloneFather();

                                    for (VennMakerView vi : views)
                                        if (vi.getNetzwerk().equals(father))
                                            v.drawLegend = vi.drawLegend;
                                }

                                Vector<Akteur> actors = VennMaker.getInstance()
                                        .getProject().getAkteure();
                                Vector<AttributeType> atts = VennMaker.getInstance()
                                        .getProject().getAttributeTypes();

                                for (Akteur actor : actors) {
                                    for (AttributeType atype : atts) {
                                        if (atype.getScope() == Scope.PROJECT) {
                                            actor.setAttributeValue(
                                                    atype,
                                                    event2.getNetzwerk(),
                                                    actor.getAttributeValue(atype, VennMaker
                                                            .getInstance().getProject()
                                                            .getCurrentNetzwerk()));
                                        }
                                    }
                                }

                                setActualVennMakerView(v);
                                JComponent p = createVisualizationPanel(v);
                                tabs.put(event2.getNetzwerk(), p);
                                tabIndex.put(p, event2.getNetzwerk());
                                tabbedPane.addTab(
                                        event2.getNetzwerk().getBezeichnung(), p);
                                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                            } else if (event instanceof DeleteNetworkEvent)
                            // Netzwerk entfernt: entferne auch zugehöriges Tab
                            {
                                final NetworkEvent event2 = (NetworkEvent) event;
                                JComponent p = tabs.get(event2.getNetzwerk());
                                tabbedPane.remove(p);
                                tabs.remove(event2.getNetzwerk());
                                tabIndex.remove(p);
                            }
                        }
                    });
            eventListenerIsSet = true;
        }
        return panel;
    }

    private JPanel createVisualizationPanel(VennMakerView actualView) {
        JPanel visPan = new JPanel();
        visPan.setLayout(new GridLayout(1, 1));
        visPan.add(actualView);
        return visPan;
    }

    public JTextArea createLeftColumnText(String string) {
        JTextArea text = new JTextArea(string);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setBackground(null);
        text.setEditable(false);
        return text;
    }

    public void setActualVennMakerView(VennMakerView actualView) {
        this.view = actualView;
    }

    // Eventhandling des TabbedPane.
    class VennMakerActualViewListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            if (tabbedPane.getSelectedIndex() == -1)
                return;

            JTabbedPane tabPane = (JTabbedPane) event.getSource();
            Component p = tabbedPane.getComponentAt(tabPane.getSelectedIndex());
            Netzwerk n = tabIndex.get(p);

            for (VennMakerView view : views)
                if (view.getNetzwerk() == n) {
                    setActualVennMakerView(view);
                }
            VennMaker.getInstance().getProject().setCurrentNetzwerk(n);

            for (VennListener listener : VennMaker.getInstance().netzwerkChangeListeners)
                listener.update();
        }
    }

    /**
     * Liefert den Eventlogger zurück.
     *
     * @return nicht <code>null</code>.
     */
    public EventLogger getLogger() {
        return VennMaker.getInstance().getProject();
    }

    public Vector<VennMakerView> getViews() {
        return views;
    }

    /**
     * Legt fest ob der Next-Button angezeigt werden soll und aktualisiert ggf.
     * die Darstellung. Ein Klick auf den Next-Button ruft den WizardController
     * auf.
     *
     * @param visible
     *           Next-Button sichtbar
     */
    public void setNextVisible(boolean visible) {
        nextButton.setVisible(visible);
        nextPanel.setVisible(nextButton.isVisible());
        this.nextButtonVisible = visible;
    }

    /**
     * Load and set the PluginModule configuration from the vmp file
     */
    private static void loadModuleData() {

        if (VennMaker.getInstance().getProject().getModuleConfig() != null) {
            try {
                for (int q = 0; q < VennMaker.getInstance().getProject().getModuleConfig().size(); q++) {

                    ModuleData module = new ModuleData();

                    module.setModuleName(VennMaker.getInstance().getProject().getModuleConfig().get(q).getModuleName());
                    module.setModuleVersion(VennMaker.getInstance().getProject().getModuleConfig().get(q).getModuleVersion());
                    module.setModuleData(VennMaker.getInstance().getProject().getModuleConfig().get(q).getModuleData());

                    VennMaker.getInstance().getModule().get(q).setConfig(VennMaker.getInstance().getProject().getModuleConfig().get(q).getModuleData());
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("loadModuleData: IndexOutOfBoundsException");
            }
        } else {

        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        StartMode startMode;

        if(argsAreValid(args)) {
            processArgs(args);
            startMode = StartMode.FREE_DRAWING;
        } else {
            startMode = showStartChooserAndGetStartMode();
        }

        Environment.initializeWorkspace();
        startVennMakerInSelectedMode(startMode);
    }

    private static StartMode showStartChooserAndGetStartMode() {
        StartChooser sc = new StartChooser();
        sc.setVisible(true);
        if (sc.isClosedWithoutDecision()) {
            VennMaker.exit();
        }
        return sc.getStartMode();
    }

    private static void startVennMakerInSelectedMode(final StartMode startMode) {
        showMainWindow();

        if(startMode.equals(StartMode.LOAD_PROJECT)) {
            loadProject();
        } else {
            SwingUtilities.invokeLater(() -> selectNonFreeDrawingMode(startMode));
        }
    }

    private static void loadProject() {
        showMainWindow();
        OpenFileDialog chooser = new OpenFileDialog();
        chooser.show();

        if (chooser.getFilename() == null || chooser.getFilename().equals(""))
            return;

        FileOperations.openVmpFile(chooser.getVmpFile(),
                chooser.getFilename(), chooser.getLastVisitedDirectory());

        VennMaker.getInstance().refresh();
    }

    private static void selectNonFreeDrawingMode(StartMode mode) {
        if (mode.equals(StartMode.PERFORM_INTERVIEW)) {
            ConfigDialog.getInstance().showLoadTemplateDialog(true);
        } else if (mode.equals(StartMode.CREATE_QUESTIONAIRE)) {
            ConfigDialog diag = new ConfigDialog(
                    CDialogInterviewCreator.class, true);
        } else if (mode.equals(StartMode.LOAD_CONFIGURATION_FOR_EDIT)) {
            ConfigDialog diag = new ConfigDialog(
                    CDialogInterviewCreator.class, false);

            if (diag.showLoadTemplateDialog(false) == IO.OPERATION_SUCCEEDED)
                diag.setVisible(true);
        } else if (mode.equals(StartMode.EDIT_TEMPLATE)) {
            ConfigDialog diag = new ConfigDialog(null, false);

            if (diag.showLoadTemplateDialog(false) == IO.OPERATION_SUCCEEDED)
                diag.setVisible(true);
        }
    }

    private static void processArgs(String[] args) {
        String filename = args[0];
        if (filename.endsWith(".vmt")) {
            loadVennMakerFromVMTFile(filename);
        } else if(filename.endsWith("venn")) {
            loadVennMakerFromVennFile(filename);
        } else if(filename.endsWith("vennEn")) {
            loadVennMakerFromVennEnFile(filename);
        } else{
            loadVennMakerFromVMPFile(filename);
        }
    }

    private static void loadVennMakerFromVMPFile(String fileName) {
        showMainWindow();
        openVMPFileArgument(fileName);
    }

    private static void loadVennMakerFromVennEnFile(String fileName) {
        // lade Config-Datei
        try {
            Config c = Config.load(fileName);
            VennMaker.getInstance().setConfig(c);
            VennMaker.getInstance().setTitle(
                    Messages.getString("VennMaker.VennMaker") + VERSION); //$NON-NLS-1$
        } catch (FileNotFoundException exn) {
            JOptionPane
                    .showMessageDialog(
                            VennMaker.getInstance(),
                            Messages.getString("VennMaker.File_notFound") //$NON-NLS-1$
                                    + exn.getLocalizedMessage(),
                            Messages.getString("VennMaker.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        } catch (IOException exn) {
            JOptionPane
                    .showMessageDialog(
                            VennMaker.getInstance(),
                            Messages.getString("VennMaker.IO-Error") //$NON-NLS-1$
                                    + exn.getLocalizedMessage(),
                            Messages.getString("VennMaker.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        } catch (ConversionException exn) {
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Die Datei wurde mit einer alten Version von VennMaker erstellt und kann nicht geöffnet werden.\n\n" //$NON-NLS-1$
                                    + exn.getLocalizedMessage(),
                            Messages.getString("VennMaker.Error"), //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadVennMakerFromVennFile(String fileName) {
        Projekt p = Projekt.load(fileName);
        if (p == null)
            JOptionPane.showMessageDialog(null,
                    Messages.getString("VennMaker.ErrorLoading") //$NON-NLS-1$
                            + fileName, Messages.getString("VennMaker.Error"), //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
        else {
            VennMaker.getInstance().setTitle(
                    Messages.getString("VennMaker.VennMaker") + VERSION + " [" //$NON-NLS-1$ //$NON-NLS-2$
                            + fileName + "]"); //$NON-NLS-1$
            VennMaker.getInstance().setProjekt(p);
            // VennMaker.getInstance().resetUndoRedoControls();

            /**
             * Verlinkungen aktualisieren
             */
            VennMaker.getInstance().setCurrentWorkingDirectory(
                    new File(fileName).getParent());
            FileOperations.changeRootFolder(VMPaths
                    .getCurrentWorkingDirectory());


            loadModuleData();
        }

        showMainWindow();
    }

    private static void loadVennMakerFromVMTFile(String fileName) {
        showMainWindow();

        File openFile = new File(fileName);
        ConfigDialog.getInstance().setLastTemplateLocation(
                openFile.getParentFile().getAbsolutePath());

        TemplateBackgroundOperations tbo = new TemplateBackgroundOperations(
                openFile, VennMaker.getInstance(), true,
                TemplateOperation.LOAD);
        tbo.startAction();
    }


    private static boolean argsAreValid(String[] args) {
        return args.length == 1 && (args[0].endsWith(".vmt") || args[0].endsWith(".vennEn"));
    }

    /**
     * Der wirkliche VennMaker-Start: Das Hauptfenster wird aufgebaut und dem
     * WizardController die Chance gegeben, den ersten Wizard zu starten, falls
     * vorhanden.
     */
    public static void showMainWindow() {
        VennMaker.getInstance().setDefaultCloseOperation(
                JFrame.DO_NOTHING_ON_CLOSE);
        VennMaker.getInstance().setSize(1000, 700);
        VennMaker.getInstance().setMinimumSize(new Dimension(300, 270));
        VennMaker.getInstance().setExtendedState(JFrame.MAXIMIZED_BOTH);
        VennMaker.getInstance().setIconImage(
                new ImageIcon(FileOperations
                        .getAbsolutePath("icons/intern/icon.png")).getImage()); //$NON-NLS-1$

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int top = (screenSize.height - VennMaker.getInstance().getHeight()) / 2;
        int left = (screenSize.width - VennMaker.getInstance().getWidth()) / 2;
        VennMaker.getInstance().setLocation(left, top);

        VennMaker.getInstance().refresh();

        VennMaker.getInstance().setVisible(true);

        /**
         * Inititialisiere den WizardController mit der Liste konfigurierter
         * Wizards.
         */
        WizardController.getInstance().init(
                VennMaker.getInstance().config.getWizards());

        /**
         * Starte den ersten Wizard.
         */
        WizardController.getInstance().perform();

    }

    /**
     * Open the open file dialog if vennmaker starts with a vmp-file name
     * argument
     *
     * @param f
     *           fileName
     */
    public static void openVMPFileArgument(String f) {

        File tmp = new File(f);
        if (tmp.exists()) {
            String fileName = tmp.getAbsolutePath().toString();
            File vmpFile = new File(fileName);
            VMPaths.setVmpFile(vmpFile);

            String[] name = tmp.getName().split(".vmp");
            String rootFolder = VMPaths.VENNMAKER_TEMPDIR
                    + "projects" + VMPaths.SEPARATOR + name[0]; //$NON-NLS-1$
            //String rootFolder = VennMaker.TEMPDIR	+ "projects" + VennMaker.SEPARATOR + "test"; //$NON-NLS-1$

            tmp = new File(rootFolder);

            if (tmp.exists()) {
                tmp.delete();
                tmp.mkdir();
            } else
                tmp.mkdir();

            FileOperations.unzip(vmpFile);

            Vector<String> files = FileOperations.getFolderContents(rootFolder,
                    ".venn"); //$NON-NLS-1$

            Vector<ListFile> filesToList = new Vector<ListFile>();

            for (String str : files) {
                filesToList.add(new ListFile(rootFolder + VMPaths.SEPARATOR + str));
            }

            SelectInterviewDialog interviewDialog = new SelectInterviewDialog(
                    filesToList, vmpFile.toString());

            File file = interviewDialog.getFileToOpen();

            if (file != null) {
                FileOperations.openVmpFile(vmpFile, file.getAbsolutePath(),
                        rootFolder);
            } else {
                exit();
            }
        } else {
            ErrorCenter
                    .manageException(
                            null,
                            Messages.getString("OpenFileDialog.FileNotFound") + ": " + f, ErrorCenter.ERROR, false, true); //$NON-NLS-1$
            exit();
        }
    }

    /**
     * durchläuft alle Relationen in allen Netzwerken, und überprüft, ob sie noch
     * bestehende Attribute enthalten - wenn nicht, werden die entsprechenden
     * Relationen gelöscht
     */
    public void updateRelations() {
        for (Netzwerk n : this.getProject().getNetzwerke()) {
            for (Akteur a : n.getAkteure()) {
                Vector<Relation> removeRelations = new Vector<Relation>();
                for (Relation r : a.getRelations(n)) {
                    Vector<AttributeType> removeAttributes = new Vector<AttributeType>();
                    for (AttributeType at : r.getAttributes(n).keySet()) {
                        if (!this.getProject().getAttributeTypes().contains(at))
                            removeAttributes.add(at);
                    }
                    for (AttributeType at : removeAttributes) {
                        r.getAttributes(n).remove(at);
                    }
                    if (r.getAttributes(n).size() == 0)
                        removeRelations.add(r);
                }
                for (Relation r : removeRelations) {
                    a.getRelations(n).remove(r);
                }
            }
            n.refreshRelationAttributes();
        }
    }

    /**
     * @return the config
     */
    public final Config getConfig() {
        return config;
    }

    /**
     * Beendet VennMaker schnell, leise und sauber(?).
     */
    public static void exit() {
        System.exit(0);
    }

    public boolean isInterviewMode() {
        return interviewMode;
    }

    public void setCurrentWorkingDirectory(String newWorkingDirectory) {
        if (!newWorkingDirectory.endsWith(VMPaths.SEPARATOR))
            newWorkingDirectory += VMPaths.SEPARATOR;

        VMPaths.setCurrentWorkingDirectory(newWorkingDirectory);
    }

    public void setInterviewMode(boolean interviewMode) {
        this.interviewMode = interviewMode;
    }

    /**
     * Der gerade gewählte Wert des MainGenerators.
     */
    private Object mainGeneratorValue;

    public Object getMainGeneratorValue() {
        return mainGeneratorValue;
    }

    public void setMainGeneratorValue(Object mainGeneratorValue) {
        this.mainGeneratorValue = mainGeneratorValue;
    }

    /**
     * Am ConfigDialogLayer müssen sich die Objekte anmelden die im ConfigDialog
     * dargestellt werden sollen
     */
    public void createConfigDialogLayer() {
        createInterviewLayer();

        ConfigDialogLayer cdLayer = ConfigDialogLayer.getInstance();

        cdLayer.addUniqueNetElement(CDialogNetworkClone.class);

        cdLayer.addProjectElement(CDialogInterviewNotes.class);
        cdLayer.addProjectElement(CDialogEditAttributeTypes.class);
        cdLayer.addProjectElement(CDialogEditRelationalAttributeTypes.class);
        cdLayer.addProjectElement(CDialogZoom.class);
        cdLayer.addProjectElement(CDialogRatio.class);
        cdLayer.addProjectElement(CDialogTrigger.class);
        cdLayer.addProjectElement(CDialogActorLabel.class);
        cdLayer.addProjectElement(CDialogRelationLabel.class);
        cdLayer.addProjectElement(CDialogInterviewCreator.class);

        cdLayer.addNetworkElement(CDialogNetworkNotes.class);
        cdLayer.addNetworkElement(CDialogActorPie.class);
        cdLayer.addNetworkElement(CDialogActorSize.class);
        cdLayer.addNetworkElement(CDialogActorImage.class);
        cdLayer.addNetworkElement(CDialogRelationColorTable.class);
        cdLayer.addNetworkElement(CDialogRelationDashTable.class);
        cdLayer.addNetworkElement(CDialogRelationSizeTable.class);

        cdLayer.addNetworkElement(CDialogSector.class);
        cdLayer.addNetworkElement(CDialogCircle.class);
        cdLayer.addNetworkElement(CDialogLegend.class);
        cdLayer.addNetworkElement(CDialogBackgroundImage.class);
        cdLayer.addNetworkElement(CDialogBackgroundNetworkCard.class);
    }

    private void createInterviewLayer() {
        InterviewLayer layer = new InterviewLayer();

        /**
         * the order of categories is defined by the order elements with these
         * categories get registered...
         *
         * 1. register(TextElement) -> 1. category is MetaElements and so on..
         */
        // First category should be ego
        layer.registerElement(EgoEnhancedFreeAnswerElement.class);
        layer.registerElement(EgoSingleAttributeDragDropElement.class);
        layer.registerElement(EgoSingleAttributeRadioElement.class);
        layer.registerElement(EgoSingleAttributeFreeAnswerElement.class);
        layer.registerElement(EgoMultiAttributeOneActorElement.class);

        // second Name Generator
        layer.registerElement(NameGenerator.class);
        layer.registerElement(ExistingActorsNameGenerator.class);

        // third Name Interpretator
        layer.registerElement(AlterSingleAttributeDragDropElement.class);
        layer.registerElement(AlterSingleAttributeRadioElement.class);
        layer.registerElement(AlterSingleAttributeFreeAnswerElement.class);
        layer.registerElement(AlterMultiAttributeOneActorElement.class);

        // relation elements
        layer.registerElement(RelationGeneratorListElement.class);
        layer.registerElement(RelationGeneratorPairElement.class);
        layer.registerElement(RelationGeneratorAlteriPairElement.class);

        // fourth Meta Elements
        layer.registerElement(TextElement.class);
        layer.registerElement(SwitchToNetworkElement.class);
        layer.registerElement(SwitchToNetworkElementAutoDraw.class);
        layer.registerElement(MinimumAlteriReachedElement.class);
        layer.registerElement(MaximumAlteriReachedElement.class);
        layer.registerElement(AudioRecorderElement.class);
        layer.registerElement(AutoSaveElement.class);
        layer.registerElement(CloseVennMakerElement.class);
        layer.registerElement(DataProtectionElement.class);
        layer.registerElement(CryptActorElement.class);
    }

    /**
     * Wird vor dem Speichern von Bildern vom SetImageSizeDialog benutzt um die
     * gewünschte Auflösung zu übermitteln
     *
     * @param width
     * @param height
     */
    public void setImageExportSize(int width, int height) {
        this.imageExportWidth = width;
        this.imageExportHeight = height;
    }

    public void registerEventListeners() {
        EventProcessor.getInstance().addEventListener(
                new EventListener<LoadTemplateEvent>() {
                    @Override
                    public void eventOccured(VennMakerEvent event) {
                        InterviewController controller = new InterviewController();
                        controller.setCalledFromConfigDialog(false);
                        controller.start();
                    }

                    @Override
                    public Class<LoadTemplateEvent> getEventType() {
                        return LoadTemplateEvent.class;
                    }

                });
    }

    public VennMakerView getViewOfNetwork(Netzwerk n) {
        for (VennMakerView v : views) {
            if (v.getNetzwerk().equals(n))
                return v;
        }
        return null;
    }

    public VennMakerView getActualVennMakerView() {
        return this.view;
    }

    /**
     * Check if changes saved
     *
     * @return true: Changes saved, false: Changes not saved
     */
    public boolean isChangesSaved() {

        return this.changesSaved;
    }

    /**
     * Set flag for Changes saved
     *
     * @param s
     *           true: Changes saved, false: Changes not saved
     */
    public void setChangesSaved(boolean s) {
        this.changesSaved = s;
    }

    /**
     * Returns the project
     *
     * @return project
     */
    public Projekt getProject() {
        return this.projekt;
    }

    /**
     * Set the project
     *
     * @param s
     *           project
     */
    public void setProject(Projekt s) {
        this.projekt = s;
    }

    /**
     * Set VennMaker Panel
     *
     * @param c
     *           JPanel
     */
    public void setVisPanel(JPanel c) {
        this.visPanel = c;
    }

    /**
     * Get VennMaker Panel
     *
     * @return JPanel
     */
    public JPanel getVisPanel() {
        return this.visPanel;
    }

    /**
     * Aufruf wenn ein neues Projekt / Interview angelegt wird, somit wird
     * gew�hrleistet, das VennMaker sich selber wieder als Listener beim
     * EventProcessor anmeldet.
     */
    public void resetEventListenerIsSetFlag() {
        eventListenerIsSet = false;
    }

    /**
     * Adds a new <code>UncaughtExceptionListener</code> to listen to Uncaught
     * Exceptions
     *
     * @param listener
     *           new <code>UncaughtExceptionListener</code> to listen to Uncaught
     *           Exceptions
     * @throws IllegalArgumentException
     *            if <code>listner</code> is <code>null</code>
     */
    public void addUncaughtExceptionListener(UncaughtExceptionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException(
                    "UncaughtExceptionListener should not be null");

        this.uncaughtExceptionListeners.add(listener);
    }

    /**
     * Removes the given <code>UncaughtExceptionListener</code>
     *
     * @param listener
     *           <code>UncaughtExceptionListener</code> to remove from the list
     * @throws IllegalArgumentException
     *            if <code>listner</code> is <code>null</code>
     */
    public void removeUncaughtExceptionListener(
            UncaughtExceptionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException(
                    "UncaughtExceptionListener should not be null");

        this.uncaughtExceptionListeners.remove(listener);
    }

    /**
     * Calls every <code>UncaughtExceptionListener</code> in the list
     */
    public void callUncaughtExceptionListeners() {
        for (UncaughtExceptionListener listener : this.uncaughtExceptionListeners)
            listener.exceptionOccured();
    }

    /**
     * Determinates if a given network name already exists
     *
     * @param name
     *           possible network name to be checked for
     * @return true if bezeichnung is a non-used network name; false otherwise
     */
    protected boolean checkNetworkName(String name) {

        for (Netzwerk net : getProject().getNetzwerke()) {
            if (net.getBezeichnung().equals(name)) {
                JOptionPane.showMessageDialog(VennMaker.getInstance(),
                        Messages.getString("VennMaker.Existing_Network_Name"),
                        Messages.getString("VennMaker.Existing_Network_Name_Title"),
                        JOptionPane.ERROR_MESSAGE); // $NON-NLS-1$

                return false;
            }
        }
        return true;
    }
}
