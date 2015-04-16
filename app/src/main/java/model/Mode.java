package model;

/**
 * Created by asauray on 2/18/15.
 */
public class Mode {

    private String title;
    private int roomParameter, darkIcon;
    private boolean isChecked;

    public static final int ALL=0, ADMIN_ROOMS=1, USER_ROOMS=2;

    public Mode(int roomParameter, String title, int darkIcon){
        this.roomParameter = roomParameter;
        this.title = title;
        this.darkIcon = darkIcon;
        isChecked = false;
    }

    public int getRoomParameter(){
        return roomParameter;
    }

    public String getTitle(){
        return title;
    }

    public int getDarkIcon(){
        return darkIcon;
    }

    public boolean isChecked(){
        return isChecked;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

}
