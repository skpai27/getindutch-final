package com.weikang.getindutch;

public class UserAddExpense {

    private String uid;
    private String name;
    private String photoUrl;
    private boolean isSelected;

    public UserAddExpense(String uid){
        this(uid,"No name","https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public UserAddExpense(String uid, String name){
        this(uid,name,"https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public UserAddExpense(String uid, String name, String photoUrl){
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        //set by default true
        this.isSelected = true;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() { return photoUrl; }

    public String getUid(){ return uid; }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }
}
