/**
 * Draw all actors on the current network map
 */
package data;

import events.AddActorEvent;
import events.ComplexEvent;
import gui.Messages;
import gui.SpringEmbedder;
import gui.VennMaker;

import java.awt.geom.Point2D;
import java.util.Random;

public class DrawAllActors {

    Random randomGenerator = new Random();

    /**
     * Draw all actors on the network map
     */
    public void draw() {
        final Netzwerk network = VennMaker.getInstance().getProject()
                .getCurrentNetzwerk();

        for (final Akteur akteur : VennMaker.getInstance().getProject()
                .getAkteure()) {
            if (!VennMaker.getInstance().getProject().getCurrentNetzwerk()
                    .getAkteure().contains(akteur)
                    && VennMaker.getInstance().getProject().getEgo() != akteur) {

                // Akteursposition setzen
                Point2D point = network.getHintergrund().getRandomPointBySectorAndCircle(akteur, network, randomGenerator);


                ComplexEvent event = new ComplexEvent(
                        Messages.getString("VennMaker.Create_Actor")); //$NON-NLS-1$
                event.addEvent(new AddActorEvent(akteur, network, point));
                EventProcessor.getInstance().fireEvent(event);
            }
        }

    }

    /**
     * Draw all actors on the network map
     *
     * @param network
     */
    public void draw(Netzwerk network) {
        System.out.println("draw(network) called");

        for (final Akteur akteur : VennMaker.getInstance().getProject()
                .getAkteure()) {
            if (!VennMaker.getInstance().getProject().getCurrentNetzwerk()
                    .getAkteure().contains(akteur)
                    && VennMaker.getInstance().getProject().getEgo() != akteur) {
                // Akteursposition setzen
                Point2D point = new Point2D.Double(
                        (-100 + randomGenerator.nextInt(200)),
                        (-100 + randomGenerator.nextInt(200)));

                ComplexEvent event = new ComplexEvent(
                        Messages.getString("VennMaker.Create_Actor")); //$NON-NLS-1$
                event.addEvent(new AddActorEvent(akteur, network, point));
                EventProcessor.getInstance().fireEvent(event);
            }
        }

    }

    /**
     * Draw all actors on the network map and use the spring embedder layout
     * algorithm
     *
     * @param network
     */
    public void drawSpringEmbedder(Netzwerk network) {

        for (final Akteur akteur : VennMaker.getInstance().getProject()
                .getAkteure()) {
            if (!VennMaker.getInstance().getProject().getCurrentNetzwerk()
                    .getAkteure().contains(akteur)
                    && VennMaker.getInstance().getProject().getEgo() != akteur) {
                // Akteursposition setzen
                Point2D point = new Point2D.Double(
                        (-100 + randomGenerator.nextInt(200)),
                        (-100 + randomGenerator.nextInt(200)));

                ComplexEvent event = new ComplexEvent(
                        Messages.getString("VennMaker.Create_Actor")); //$NON-NLS-1$
                event.addEvent(new AddActorEvent(akteur, network, point));
                EventProcessor.getInstance().fireEvent(event);
            }
        }

        SpringEmbedder s = new SpringEmbedder();
        s.useSpringEmbedder(network.getAkteure(), network);

        VennMaker.getInstance().getConfig().setEgoMoveable(true);

    }
}
