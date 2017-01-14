package gui;

import javax.swing.*;
import java.awt.*;

/**
 *
 *
 *
 */
public class VennMakerCellEditor extends DefaultCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public VennMakerCellEditor(JComboBox cb) {
        super(cb);
        cb.setEditable(false);
    }

    public VennMakerCellEditor(JTextField tf) {
        super(tf);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (editorComponent instanceof JTextField) {
            ((JTextField) editorComponent).setText("");
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
