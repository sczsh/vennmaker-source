package gui.configdialog.save;

import gui.configdialog.elements.*;

import java.util.HashMap;

/**
 * Erlaubt Laden von SaveElements von alten ConfigDialogen in �berarbeitete
 * Dialoge
 * <p>
 * Einfache HashMap zur Umwandlung von alten ConfigDialog-Namen zu neuen.
 * <p>
 * TODO: Eventuelle �berarbeitung durch SaveElement Typ mit passendem Dialog,
 * reduziert jedoch Flexibilit�t
 */
public class SaveElementConverter {
    private HashMap<String, String> conversionMap;

    public SaveElementConverter() {
        conversionMap = new HashMap<>();

        conversionMap.put(CDialogRelationColor.class.getSimpleName(),
                CDialogRelationColorTable.class.getSimpleName());
        conversionMap.put(CDialogRelationDashTable.class.getSimpleName(),
                CDialogRelationDashTable.class.getSimpleName());
        conversionMap.put(CDialogRelationSize.class.getSimpleName(),
                CDialogRelationSizeTable.class.getSimpleName());
    }

    public String convert(String name) {
        if (conversionMap.get(name) != null)
            return conversionMap.get(name);
        return name;
    }
}
