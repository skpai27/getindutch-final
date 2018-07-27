package com.weikang.getindutch;

public class SCCheckboxSpinner {
    private String memberName;
    private boolean selected;
    private String memberUid;



    public SCCheckboxSpinner(String memberName, String memberUid) {
        this.memberName = memberName;
        this.selected = false;
        this.memberUid = memberUid;
    }

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String toString(){
        return getMemberName();
    }
}
