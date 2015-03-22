package model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by sauray on 21/03/15.
 */
public class Document implements Parcelable, Comparable<Document>{

    protected int id;
    protected String name, specification, description, url, location, extension;
    protected GregorianCalendar calendar;

    public Document(int id, String name, String specification, String description, String url, GregorianCalendar calendar){
        this.id = id;
        this.name = name;
        this.specification = specification;
        this.description = description;
        this.url = url;
        this.calendar = calendar;
        String[] split = url.split(Pattern.quote("."));
        extension=split[split.length-1];
        location = null;
    }

    public Document(Parcel in){
        readFromParcel(in);
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getLocation(){
        return location;
    }

    public String getExtension(){
        return extension;
    }

    public void setExtension(String extension){
        this.extension = extension;
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

    public String getUrl(){
        return url;
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
        dest.writeString(url);
        dest.writeString(location);
        dest.writeString(extension);
        dest.writeSerializable(calendar);
    }

    protected void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        specification = in.readString();
        description = in.readString();
        url = in.readString();
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
        return "documentID="+id+" - name="+name+" - specification="+specification+" - description="+description+" - url="+url;
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
