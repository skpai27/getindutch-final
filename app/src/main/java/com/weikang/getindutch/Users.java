package com.weikang.getindutch;

class Users {
    private String uid;
    private String name;
    private String photoUrl;

    public Users(String uid){
        this(uid,"No name","https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public Users(String uid, String name){
        this(uid,name,"https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public Users(String uid, String name, String photoUrl){
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() { return photoUrl; }

    public String getUid(){ return uid; }
}
