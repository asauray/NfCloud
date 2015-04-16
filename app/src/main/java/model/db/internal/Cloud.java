package model.db.internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Jambo is an internal database implementation of the gorilla database.
 *
 * Created by Alexis on 17/02/2015.
 */
public class Cloud extends SQLiteOpenHelper{

    // ---------- DATABASE

    // ---------- TABLES
    // ----- room
    public static final String ROOM = "room";
    public static final String ROOM_ID = "id";
    public static final String ROOM_NAME = "nom";
    public static final String ROOM_DESC = "description";
    public static final String ROOM_CATEGORY = "category";
    public static final String ROOM_USERGROUP = "usergroup";

    // ----- document
    public static final String DOCUMENT = "document";
    public static final String DOCUMENT_ID = "id";
    public static final String DOCUMENT_NAME = "name";
    public static final String DOCUMENT_SPEC = "specification";
    public static final String DOCUMENT_DESC = "description";
    public static final String DOCUMENT_LOCATION = "location";
    public static final String DOCUMENT_DATE = "date";
    public static final String DOCUMENT_SIZE = "size";
    public static final String DOCUMENT_ROOM = "room";

    // ----- appartient
    public static final String CARD = "card";
    public static final String CARD_ID = "id";
    public static final String CARD_ROOM = "room";
    public static final String CARD_HASH = "hash";

    // ---------- CREATE TABLE
    private static final String ROOM_CREATE = "CREATE TABLE " + ROOM + "("
            + ROOM_ID + " INTEGER PRIMARY KEY, "
            + ROOM_NAME + " TEXT NOT NULL DEFAULT 'unknown',"
            + ROOM_DESC + " TEXT DEFAULT NULL,"
            + ROOM_CATEGORY + " INTEGER NOT NULL,"
            + ROOM_USERGROUP + " TEXT NOT NULL DEFAULT 'unknown'"
            + ");";

    private static final String DOCUMENT_CREATE = "CREATE TABLE " + DOCUMENT + "("
            + DOCUMENT_ID + " INTEGER PRIMARY KEY, "
            + DOCUMENT_NAME + " TEXT NOT NULL DEFAULT 'unknown',"
            + DOCUMENT_SPEC + " TEXT DEFAULT '', "
            + DOCUMENT_DESC + " TEXT DEFAULT '', "
            + DOCUMENT_LOCATION + " TEXT DEFAULT '',"
            + DOCUMENT_DATE + " TEXT DEFAULT '',"
            + DOCUMENT_SIZE + " INTEGER DEFAULT 0,"
            + DOCUMENT_ROOM + " INTEGER NOT NULL CONSTRAINT fkdocument_room REFERENCES " + ROOM + " (id) "
            + ");";


    private static final String CARD_CREATE = "CREATE TABLE " + CARD + "("
            + CARD_ID + " INTEGER PRIMARY KEY, "
            + CARD_ROOM + " INTEGER NOT NULL CONSTRAINT fkcard_room REFERENCES " + ROOM + " (id), "
            + CARD_HASH + " TEXT NOT NULL DEFAULT 'unknown'"
            + ");";



    // ---------- CONSTRUCTOR

    public Cloud(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    // ---------- METHODS

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(ROOM_CREATE);
        database.execSQL(DOCUMENT_CREATE);
        database.execSQL(CARD_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(CloudDAO.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + ROOM);
        db.execSQL("DROP TABLE IF EXISTS " + DOCUMENT);
        db.execSQL("DROP TABLE IF EXISTS " + CARD);
        onCreate(db);
    }
}

