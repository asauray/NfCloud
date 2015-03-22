package model;

/**
 * Created by sauray on 21/03/15.
 */
public class User {

    private int id;
    private int privilege;
    private String name;
    private Nfcard card;

    public User(int id, int privilege, String name){
        this.id = id;
        this.privilege = privilege;
        this.name = name;
    }

    public Nfcard getCard(){
        return card;
    }

}
