/**
 *
 */
package data;

import java.util.HashMap;
import java.util.Map;

public class ActorSectorVisualizer extends Visualizer {
    private Map<AttributeType, Object> typeAndValue = new HashMap<AttributeType, Object>();
    private Map<AttributeType, Object> sectorColor = new HashMap<AttributeType, Object>();


    /**
     * @param attributeType and the selected attributeValue
     */
    public void addAttributeTypeAndSelection(AttributeType attributeType, Object attributeValue) {
        this.typeAndValue.put(attributeType, attributeValue);

    }

    public void setAttributeTypeAndSelection(Map<AttributeType, Object> t) {

        this.typeAndValue = t;
    }

    /**
     * @return List of Attribute Types and the selected attributeValues
     */
    public Map<AttributeType, Object> getAttributeTypesAndSelection() {


        return this.typeAndValue;
    }


    public void removeAttributeSelectionAndColor(AttributeType a) {
        if (typeAndValue.containsKey(a) && sectorColor.containsKey(a)) {
            typeAndValue.remove(a);
            sectorColor.remove(a);
        }
    }

    /**
     * @param sectorColor the sectorColor to set
     */
    public void setSectorColor(Map<AttributeType, Object> sectorColor) {
        this.sectorColor = sectorColor;
    }

    /**
     * @return the sectorColor
     */
    public Map<AttributeType, Object> getSectorColor() {
        return sectorColor;
    }


}
