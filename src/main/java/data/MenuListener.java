/**
 *
 */
package data;


import events.MenuEvent;

import java.util.EventListener;

/**
 * Interface fuer die Listener fuer Audio- und Bildwiedergabe
 */
public interface MenuListener extends EventListener {

    void action(MenuEvent e);

}
