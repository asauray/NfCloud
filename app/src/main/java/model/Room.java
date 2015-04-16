package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sauray on 21/03/15.
 */
public class Room implements Parcelable, Comparable<Room>{

    private int id;
    private String name;
    private String admin;
    private String description;
    private String url;
    private String userGroup;
    private boolean isChecked;
    private int category;

    public Room(String name, String description){
        this.id = -1;
        this.name = name;
        this.admin = null;
        this.description = description;
        this.userGroup = null;
        this.category = -1;
        isChecked=false;
    }


    public Room(int id, String name, String description, String userGroup, int category){
        this.id = id;
        this.name = name;
        this.admin = null;
        this.description = description;
        this.userGroup = userGroup;
        this.category = category;
        isChecked=false;
    }

    public Room(Parcel in){
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

    public int getCategory(){
        return category;
    }

    public boolean isChecked(){
        return isChecked;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
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
        dest.writeInt(category);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        category = in.readInt();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Room createFromParcel(Parcel in) {
                    return new Room(in);
                }

                public Room[] newArray(int size) {
                    return new Room[size];
                }
            };

    @Override
    public int compareTo(Room another) {
        return name.compareTo(name);
    }

    @Override
    public String toString(){
        return "groupID="+id+" - name="+name+" - url="+url;
    }
}
