package gui.utilities;

import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class SelectAllOnFocus implements FocusListener {

    private final JTextComponent component;

    public SelectAllOnFocus(JTextComponent component) {
        this.component = component;
    }


    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        this.component.selectAll();
    }

}