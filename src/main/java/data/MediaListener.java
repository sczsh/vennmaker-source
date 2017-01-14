/**
 *
 */
package data;


import events.MediaEvent;

import java.util.EventListener;

/**
 * Interface fuer die Listener fuer Audio- und Bildwiedergabe
 */
public interface MediaListener extends EventListener {

    void action(MediaEvent e);

}
