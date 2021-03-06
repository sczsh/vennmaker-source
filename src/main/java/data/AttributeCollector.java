/**
 *
 */
package data;

import data.AttributeType.Scope;
import gui.VennMaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AttributeCollector implements AttributeSubject {
    /**
     * Hier werden fuer jedes Netzwerk die Attribute gespeichert.
     */
    private Map<Netzwerk, Map<AttributeType, Object>> attributes = new HashMap<Netzwerk, Map<AttributeType, Object>>();

    /*
     * (non-Javadoc)
     *
     * @see data.AttributeSubject#getAttributeValue(data.AttributeType,
     * data.Netzwerk)
     */
    @Override
    public Object getAttributeValue(AttributeType type, Netzwerk network) {
        if (type != null) {
            if (attributes.get(network) == null)
                attributes.put(network, new HashMap<AttributeType, Object>());
            if (attributes.get(network).get(type) == null
                    && type.getDefaultValue() != null)
                attributes.get(network).put(type, type.getDefaultValue());
            return attributes.get(network).get(type);
        } else
            return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see data.AttributeSubject#getAttributes(data.Netzwerk)
     */
    @Override
    public Map<AttributeType, Object> getAttributes(Netzwerk network) {
        if (attributes.get(network) == null)
            attributes.put(network, new HashMap<AttributeType, Object>());
        return attributes.get(network);
    }

    /*
     * (non-Javadoc)
     *
     * @see data.AttributeSubject#setAttributeValue(data.AttributeType,
     * data.Netzwerk, java.lang.Object)
     */
    @Override
    public void setAttributeValue(AttributeType type, Netzwerk network,
                                  Object value) {
        setAttributeValue(type, network, value, VennMaker.getInstance()
                .getProject().getNetzwerke());
    }

    @Override
    public void setAttributeValue(AttributeType type, Netzwerk network,
                                  Object value, Vector<Netzwerk> networks) {
        if (type.getScope() == Scope.NETWORK) {
            putAttributeValue(type, network, value);
        } else
            for (Netzwerk n : networks) {
                putAttributeValue(type, n, value);
            }
    }

    /**
     * inserts the value for the given AttributeType in the given Network
     *
     * @param type    the type, which is about to receive a new value
     * @param network the network, in which this change will take place
     * @param value
     */
    private void putAttributeValue(AttributeType type, Netzwerk network,
                                   Object value) {
        if (attributes.get(network) == null)
            attributes.put(network, new HashMap<AttributeType, Object>());
        attributes.get(network).put(type, value);
    }
}
