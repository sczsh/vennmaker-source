package interview.configuration.filterSelection;

import data.Akteur;
import gui.VennMaker;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel to select the filter performed for EGO-selection
 * <p>
 * same as FilterPanel but only for the EGO
 */
public class EgoFilterPanel extends FilterPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EgoFilterPanel() {
        super("InterviewElement.EgoFilterDescription");
    }

    @Override
    public List<Akteur> getDummyFilteredActors() {
        List<Akteur> actors = new ArrayList<Akteur>();
        actors.add(VennMaker.getInstance().getProject().getEgo());
        return actors;
    }

    @Override
    public List<Akteur> getFilteredActors() {
        if (dummyCreationEnabled)
            return getDummyFilteredActors();

        InterviewFilterDialog fid = new InterviewFilterDialog(this, filter, false);
        List<Akteur> actors = fid.getActors();
        Akteur ego = VennMaker.getInstance().getProject().getEgo();
        //Filter matches EGO
        if (actors.contains(ego)) {
            actors = new ArrayList<Akteur>();
            actors.add(ego);
        } else
            actors = null;
        fid.dispose();

        return actors;
    }
}
