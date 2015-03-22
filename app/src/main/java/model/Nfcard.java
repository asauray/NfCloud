package model;

import java.security.NoSuchAlgorithmException;

import model.security.Hash;

/**
 * Created by sauray on 21/03/15.
 */
public class Nfcard {

    private String name, privilege;
    private int userID, cardID;
    private String hash;

    public Nfcard(int idUser, int idCard, String name, String privilege) throws NoSuchAlgorithmException {
        this.userID = idUser;
        this.cardID = idCard;
        this.name = name;
        this.privilege = privilege;
        hash = Hash.sha256(""+userID + name + privilege + cardID);
    }

    public Nfcard(String hash){
        this.hash = hash;
    }

    public String getName(){
        return name;
    }

    public int getUserID(){
        return userID;
    }

    public int getCardID(){
        return cardID;
    }

    public String getPrivilege(){
        return privilege;
    }

    public String getSha256(){
        return hash;
    }

    public String toString(){
        return "userID="+userID+" - cardID="+cardID+" - privilege="+privilege+" - name="+name;
    }

}
