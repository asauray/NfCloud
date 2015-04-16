package model;

import java.security.NoSuchAlgorithmException;

import model.security.Hash;

/**
 * Created by sauray on 21/03/15.
 */
public class Nfcard {

    private int id, room;
    private String hash;

    public Nfcard(int id, int room) throws NoSuchAlgorithmException {
        this.id = id;
        this.room = room;
        hash = Hash.sha256(""+id + room);
    }

    public Nfcard(int id, String hash){
        this.id = id;
        this.room = -1;
        this.hash = hash;
    }

    public Nfcard(int id, int room, String hash){
        this.id = id;
        this.room = room;
        this.hash = hash;
    }

    public int getId(){
        return id;
    }

    public int getRoom(){
        return room;
    }

    public String getSha256(){
        return hash;
    }

    public void setSha256(String hash){
        this.hash = hash;
    }

    public String toString(){
        return "id="+id+" - privilege="+room;
    }

}
