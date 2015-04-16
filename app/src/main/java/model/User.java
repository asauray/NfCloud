package model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by sauray on 21/03/15.
 */
public class User {

    private int id;
    private int privilege;
    private String name;
    private HashMap<Integer, Nfcard> nfcards;

    public User(int id, String name,HashMap<Integer, Nfcard> nfcards){
        this.id = id;
        this.nfcards = nfcards;
        this.name = name;
    }

    public Nfcard getCard(Integer key){
        return nfcards.get(key);
    }

}
