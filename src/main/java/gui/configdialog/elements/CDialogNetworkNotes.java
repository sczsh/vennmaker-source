/**
 *
 */
package gui.configdialog.elements;

import gui.configdialog.ConfigDialogElement;
import gui.configdialog.ConfigDialogTempCache;
import gui.configdialog.save.NetworkNotesSaveElement;
import gui.configdialog.save.SaveElement;
import gui.configdialog.settings.ConfigDialogSetting;
import gui.configdialog.settings.SettingNetworkNotes;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog fuer Netzwerk Notizen
 */
public class CDialogNetworkNotes extends ConfigDialogElement {
    private static final long serialVersionUID = 1L;

    private JEditorPane editor;

    @Override
    public void buildPanel() {
        dialogPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        dialogPanel.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        if (editor == null) {
            editor = new JEditorPane();
            editor.setText(net.getMetaInfo());
        }

        JScrollPane editorScroll = new JScrollPane(editor);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        dialogPanel.add(editorScroll, gbc);
    }

    @Override
    public ConfigDialogSetting getFinalSetting() {
        return new SettingNetworkNotes(editor.getText(), net);
    }

    @Override
    public void setAttributesFromSetting(SaveElement setting) {
        if (!(setting instanceof NetworkNotesSaveElement))
            return;

        ConfigDialogTempCache.getInstance().addActiveElement(this);
        NetworkNotesSaveElement element = (NetworkNotesSaveElement) setting;

        this.buildPanel();
        editor.setText(element.getNotes());
    }

    @Override
    public SaveElement getSaveElement() {
        NetworkNotesSaveElement elem = new NetworkNotesSaveElement(
                editor.getText());
        elem.setElementName(this.getClass().getSimpleName());

        return elem;
    }
}
