package gui;

import org.jvnet.flamingo.common.icon.ResizableIcon;

import javax.swing.*;
import java.awt.*;

public class ResizableImageIcon implements ResizableIcon {
    private ImageIcon icon;

    public ResizableImageIcon(ImageIcon icon) {
        this.icon = icon;
    }

    @Override
    public void revertToOriginalDimension() {
    }

    @Override
    public void setDimension(Dimension arg0) {
    }

    @Override
    public void setHeight(int arg0) {
    }

    @Override
    public void setWidth(int arg0) {
    }

    @Override
    public int getIconHeight() {
        return 32;
    }

    @Override
    public int getIconWidth() {
        return 32;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }
}