package model.db.internal;

/**
 * Created by sauray on 13/03/15.
 */
public interface DAOCallback {

    public void insertionArretPerformed(int progress, int size);

    public void insertionLignePerformed(int progress, int size);

    public void insertionStationPerformed(int progress, int size);

    public void insertionBornePerformed(int progress, int size);

    public void associationPerformed(int progress, int size);

}
