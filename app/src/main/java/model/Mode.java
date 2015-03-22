package model;

/**
 * Created by asauray on 2/18/15.
 */
public class Mode {

    private String title;
    private int darkIcon, lightIcon, version;
    private boolean isChecked;

    public Mode(String title, int darkIcon, int lightIcon){
        this.title = title;
        this.darkIcon = darkIcon;
        this.lightIcon = lightIcon;
        isChecked = false;
    }

    public String getTitle(){
        return title;
    }

    public int getDarkIcon(){
        return darkIcon;
    }

    public int getLightIcon(){
        return lightIcon;
    }

    public boolean isChecked(){
        return isChecked;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    public int getVersion(){
        return version;
    }
}
