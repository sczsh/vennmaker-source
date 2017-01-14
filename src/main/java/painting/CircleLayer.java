package painting;

import java.awt.*;

public class CircleLayer extends PaintLayer implements PaintLayerInterface {
    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Utilities.drawCircles(g);
    }
}
