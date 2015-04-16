package model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by sauray on 21/03/15.
 */
public class Document implements Parcelable, Comparable<Document>{

    protected int id;
    protected int room;
    protected String name, specification, description, location, extension;
    protected GregorianCalendar calendar;

    public Document(String location, int room){
        this.location = location;
        String[] splitSlash = location.split(Pattern.quote("/"));
        name=splitSlash[splitSlash.length-1];
        String[] splitName = name.split(Pattern.quote("."));
        if(splitName.length == 2) {
            name = splitName[0];
            extension = splitName[1];
        }
        this.room = room;
    }

    public Document(int id, String name, String specification, String description, String location,  int room){
        this.id = id;
        String[] split = name.split(Pattern.quote("."));
        Log.d(this.name, "new document name");
        if(split.length>1) {
            this.name = split[0];
            extension = split[1];
        }
        Log.d(this.name, "new document name");
        Log.d(this.extension, "new document extension");
        this.specification = specification;
        this.description = description;
        this.room = room;
        this.calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        this.location = location;
    }

    public Document(int id, String name, String specification, String description, GregorianCalendar calendar){
        this.id = id;
        String[] split = name.split(Pattern.quote("."));
        if(split.length>1) {
            this.name = split[0];
            extension = split[1];
        }
        this.specification = specification;
        this.description = description;
        this.calendar = calendar;
        location = null;
    }

    public Document(Parcel in){
        readFromParcel(in);
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getFileName(){
        return name+"."+extension;
    }

    public String getLocation(){
        return location;
    }

    public String getExtension(){
        return extension;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getSpecification(){
        return specification;
    }

    public String getDescription(){
        return description;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public GregorianCalendar getCalendar(){
        return calendar;
    }

    @Override
    public int describeContents() {
        return 8;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(specification);
        dest.writeString(description);
        dest.writeInt(room);
        dest.writeString(location);
        dest.writeString(extension);
        dest.writeSerializable(calendar);
    }

    protected void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        specification = in.readString();
        description = in.readString();
        room = in.readInt();
        location = in.readString();
        extension = in.readString();
        calendar = (GregorianCalendar) in.readSerializable();
    }

    @Override
    public int compareTo(Document another) {
        return calendar.compareTo(another.calendar);
    }

    @Override
    public String toString(){
        return "documentID="+id+" - name="+name+" - specification="+specification+" - description="+description;
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Document createFromParcel(Parcel in) {
                    return new Document(in);
                }

                public Document[] newArray(int size) {
                    return new Document[size];
                }
            };

}
