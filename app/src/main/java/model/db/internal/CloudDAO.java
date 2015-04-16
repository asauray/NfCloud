package model.db.internal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Document;
import model.Mode;
import model.Nfcard;
import model.Room;

/**
 * Created by Alexis on 17/02/2015.
 */
public class CloudDAO {


    // ---------- ATTRIBUTES

    protected final static int VERSION = 1;
    protected final static String NOM = "nfcloud.db";
    protected SQLiteDatabase db;
    protected Cloud handler;

    // ---------- TABLES

    private DAOCallback callback;


    // ---------- CONSTRUCTOR

    public CloudDAO(Context context) {
        this.handler = new Cloud(context, NOM, null, VERSION);
    }


    // ---------- METHODS

    public SQLiteDatabase open() {
        db = handler.getWritableDatabase();
        return db;
    } // ---------------------------------------------------------- open()

    public void close() {
        db.close();
    } // ---------------------------------------------------------- close()

    public SQLiteDatabase getDb() {
        return db;
    } // ---------------------------------------------------------- getDb()


    // ----- room

    public long insertRoom(Room room) {

        long ret = 0;
        Cursor c = db.query(Cloud.ROOM, new String[]{Cloud.ROOM_ID}, Cloud.ROOM_ID + " LIKE '" + room.getId() + "'", null, null, null, null);
        if (c != null && c.moveToFirst()) {
            //this.removeRoom(room.getId());
        }

        if (room != null) {

            // On rentre le reseau dans la bdd
            ContentValues values = new ContentValues();
            values.put(Cloud.ROOM_ID, room.getId());
            values.put(Cloud.ROOM_NAME, room.getName());
            values.put(Cloud.ROOM_DESC, room.getDescription());
            values.put(Cloud.ROOM_USERGROUP, room.getUserGroup());
            values.put(Cloud.ROOM_CATEGORY, room.getCategory());
            ret = db.insert(Cloud.ROOM, null, values);
            Log.d(room.getName(), "=====> SQLite : Insertion room");
        }
        return ret;
    }

    public Map<Integer, Room> findRooms(int request){
        HashMap<Integer, Room> ret = new HashMap<Integer, Room>();
        Room tmp = null;
        int tmpId, tmpCat;
        String tmpName, tmpDesc, tmpUserGroup;

        Cursor c = null;
        if(request == Mode.ALL){
            c = db.query(Cloud.ROOM, new String[]{Cloud.ROOM_ID, Cloud.ROOM_NAME, Cloud.ROOM_DESC, Cloud.ROOM_USERGROUP, Cloud.ROOM_CATEGORY}, null, null, null, null, null);

        }
        else{
            c = db.query(Cloud.ROOM, new String[]{Cloud.ROOM_ID, Cloud.ROOM_NAME, Cloud.ROOM_DESC, Cloud.ROOM_USERGROUP, Cloud.ROOM_CATEGORY}, Cloud.ROOM_CATEGORY + " = '" + request + "'", null, null, null, null);
        }

        while (c.moveToNext()) {
            tmpId = c.getInt(0);
            tmpName = c.getString(1);
            tmpDesc = c.getString(2);
            tmpUserGroup = c.getString(3);
            tmpCat = c.getInt(4);
            tmp = new Room(tmpId, tmpName, tmpDesc, tmpUserGroup, tmpCat);
            ret.put(tmpId, tmp);
            Log.d(tmp.getName() , "=====> SQLite : SELECT ALL room");
        }
        c.close();
        return ret;
    }

    public int removeRoom(int id) {
        Log.d("" + id, "=====> SQLite : Suppression room");
        this.removeDocuments(id);
        return db.delete(Cloud.ROOM, Cloud.ROOM_ID + " = " + id, null);
    }

    public int removeDocument(int id) {
        Log.d("" + id, "=====> SQLite : Suppression document");
        return db.delete(Cloud.DOCUMENT, Cloud.DOCUMENT_ID + " = " + id, null);
    }


    // ----- document

    public long insertDocument(Document document) {
        long ret = 0;
        removeDocument(document.getId());
        if (document != null) {
            ContentValues values = new ContentValues();
            values.put(Cloud.DOCUMENT_ID, document.getId());
            Log.d(document.getId()+"", "insert:id");
            values.put(Cloud.DOCUMENT_NAME, document.getFileName());
            Log.d(document.getName()+"", "insert:name");
            values.put(Cloud.DOCUMENT_SPEC, document.getSpecification());
            values.put(Cloud.DOCUMENT_DESC, document.getDescription());
            values.put(Cloud.DOCUMENT_LOCATION, document.getLocation());
            values.put(Cloud.DOCUMENT_ROOM, document.getRoom());
            values.put(Cloud.DOCUMENT_DATE, document.getCalendar().toString());
            ret = db.insert(Cloud.DOCUMENT, null, values);
        }
        return ret;
    }

    public List<Document> findDocuments(int room) {
        ArrayList<Document> ret = new ArrayList<Document>();
        Document tmp = null;
        int tmpId;
        String tmpName, tmpSpec, tmpDesc, tmpLocation, tmpDate;

        if (room != 0) {
            Cursor c = db.query(Cloud.DOCUMENT, new String[]{Cloud.DOCUMENT_ID, Cloud.DOCUMENT_NAME, Cloud.DOCUMENT_SPEC, Cloud.DOCUMENT_DESC, Cloud.DOCUMENT_LOCATION, Cloud.DOCUMENT_DATE}, Cloud.DOCUMENT_ROOM + " LIKE '" + room + "'", null, null, null, null);

            while (c.moveToNext()) {
                tmpId = c.getInt(0);
                tmpName = c.getString(1);
                tmpSpec = c.getString(2);
                tmpDesc = c.getString(3);
                tmpLocation = c.getString(4);
                tmpDate = c.getString(5);
                //GregorianCalendar calendar = GregorianCalendar.

                Log.d(tmpId + "", "tmpid");
                Log.d(tmpName + "", "tmpName");

                tmp = new Document(tmpId, tmpName, tmpSpec, tmpDesc, tmpLocation, room);
                ret.add(tmp);
            }
            c.close();
        }
        return ret;
    }

        public Document findDocument(int id){
            Document tmp = null;
            int tmpId, tmpRoom;
            String tmpName, tmpSpec, tmpDesc, tmpLocation;

            Cursor c = db.query(Cloud.DOCUMENT, new String[]{Cloud.DOCUMENT_ID, Cloud.DOCUMENT_NAME, Cloud.DOCUMENT_SPEC, Cloud.DOCUMENT_DESC, Cloud.DOCUMENT_LOCATION, Cloud.DOCUMENT_ROOM}, Cloud.DOCUMENT_ID + " = '" + id + "'", null, null, null, null);
            if(c.getCount()!=0) {
                c.moveToFirst();
                tmpId = c.getInt(0);
                tmpName = c.getString(1);
                tmpSpec = c.getString(2);
                tmpDesc = c.getString(3);
                tmpLocation = c.getString(4);
                tmpRoom = c.getInt(5);
                tmp = new Document(tmpId, tmpName, tmpSpec, tmpDesc, tmpLocation, tmpRoom);
            }
            return tmp;
    }

    public int removeDocuments(int idRoom) {
        Log.d("" + idRoom, "=====> SQLite : Suppression documents d'une room");

        return db.delete(Cloud.DOCUMENT, Cloud.DOCUMENT_ROOM + " = " + idRoom, null);
    }


    // ----- card

    public long insertNfCard(Nfcard nfcard) {
        long ret = 0;

        if (nfcard != null) {

            ContentValues values = new ContentValues();
            values.put(Cloud.CARD_ID, nfcard.getId());
            values.put(Cloud.CARD_ROOM, nfcard.getRoom());
            values.put(Cloud.CARD_HASH, nfcard.getSha256());

            ret = db.insert(Cloud.CARD, null, values);
            Log.d(nfcard.getSha256(), "=====> SQLite : Insertion car");
        }
        return ret;
    }

    public List<Nfcard> findNfCards() {
        ArrayList<Nfcard> ret = new ArrayList<Nfcard>();
        Nfcard tmp = null;
        int tmpId, tmpRoom;
        String hash;

        Cursor c = db.query(Cloud.CARD, new String[]{Cloud.CARD_ID, Cloud.CARD_ROOM, Cloud.CARD_HASH}, null, null, null, null, null);

        while (c.moveToNext()) {
            tmpId = c.getInt(0);
            tmpRoom = c.getInt(1);
            hash = c.getString(2);
            tmp = new Nfcard(tmpId, tmpRoom, hash);
            ret.add(tmp);
        }
        c.close();


        Log.d("", "=====> SQLite : SELECT ALL NfCard");

        return ret;
    }
}
