/**
 *
 */
package events;

import data.MenuObject;

import java.util.EventObject;

/**
 * Events fuer das Menue und die Toolbar
 */
public class MenuEvent extends EventObject {
    private MenuObject info;

    /**
     * @param source the source object
     * @param info   MenuObject
     */
    public MenuEvent(Object source, MenuObject info) {
        super(source);
        this.info = info;
    }

    /**
     * @return MenuObject
     */
    public MenuObject getInfo() {
        return info;
    }


}

