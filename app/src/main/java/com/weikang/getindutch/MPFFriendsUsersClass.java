package com.weikang.getindutch;

import android.support.annotation.NonNull;

class MPFFriendsUsersClass implements Comparable<MPFFriendsUsersClass> {
    private String uid;
    private String name;
    private String photoUrl;
    private float bal;
    private String payee = "";

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public MPFFriendsUsersClass(String uid){
        this(uid,"No name","https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public MPFFriendsUsersClass(String uid, String name){
        this(uid,name,"https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png");
    }

    public MPFFriendsUsersClass(String uid, String name, String photoUrl){
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() { return photoUrl; }

    public String getUid(){ return uid; }

    public void setBal(float bal){this.bal = bal;}

    public float getBal(){return bal;}

    public String toString(){return name;}

    @Override
    public int compareTo(@NonNull MPFFriendsUsersClass o) {
        if (this.bal == o.getBal()){
            return this.name.compareTo(o.getName());
        } else {
            return (int) (this.bal - o.getBal());
        }
    }
}
