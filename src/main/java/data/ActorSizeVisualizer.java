/**
 *
 */
package data;

import java.util.Map;

public class ActorSizeVisualizer extends Visualizer {
    private Map<Object, Integer> sizes;

    public int getSize(AttributeSubject subject, Netzwerk network) {
        assert this.sizes != null;
        if (this.sizes.get(subject.getAttributeValue(this.getAttributeType(),
                network)) != null)
            return this.sizes.get(subject.getAttributeValue(this.getAttributeType(),
                    network));

        else
            /*
			 * Standard-Groesse, wenn keine Groesse fuer diesen AttributeType-Wert gesetzt.
			 */
            return 11;
    }

    public void setSizes(Map<Object, Integer> sizes) {
        this.sizes = sizes;
    }

    public Map<Object, Integer> getSizes() {
        return sizes;
    }
}
