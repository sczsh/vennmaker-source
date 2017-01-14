package interview.configuration.filterSelection;

import data.Akteur;
import gui.VennMaker;

import java.util.List;

/**
 * Panel to select the filter performed for Alteri-selection
 * <p>
 * same as FilterPanel but only for alteri
 */
public class AlteriFilterPanel extends FilterPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AlteriFilterPanel() {
        super("InterviewElement.FilterDescription");
    }

    @Override
    public List<Akteur> getFilteredActors() {
        if (dummyCreationEnabled)
            return getDummyFilteredActors();
        System.out.println("AlteriFilterPanel...." + filter);
        InterviewFilterDialog fid = new InterviewFilterDialog(this, filter, false);
        List<Akteur> actors = fid.getActors();
        fid.dispose();

        Akteur ego = VennMaker.getInstance().getProject().getEgo();
        if (actors.contains(ego))
            actors.remove(ego);

        return actors;
    }
}
