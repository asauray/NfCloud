package model;

/**
 * Created by sauray on 21/03/15.
 */
public class Nfcard {

    private String name;
    private int idUser, idCard;

    public Nfcard(int idUser, int idCard, String name, int privilege){
        this.idUser = idUser;
        this.idCard = idCard;
        this.name = name;
    }

}
