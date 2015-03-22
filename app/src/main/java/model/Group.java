package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sauray on 21/03/15.
 */
public class Group implements Parcelable, Comparable<Group>{

    private int id;
    private String name, admin,description, url;

    public Group(int id, String name, String admin, String description, String url){
        this.id = id;
        this.name = name;
        this.admin = admin;
        this.description = description;
        this.url = url;
    }

    public Group(Parcel in){
        readFromParcel(in);
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getAdmin(){
        return admin;
    }

    public String getDescription(){
        return description;
    }

    public String getUrl(){
        return url;
    }

    @Override
    public int describeContents() {
        return 3;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Group createFromParcel(Parcel in) {
                    return new Group(in);
                }

                public Group[] newArray(int size) {
                    return new Group[size];
                }
            };

    @Override
    public int compareTo(Group another) {
        return name.compareTo(name);
    }

    @Override
    public String toString(){
        return "groupID="+id+" - name="+name+" - url="+url;
    }
}
