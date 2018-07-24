package com.weikang.getindutch;

public class SCCheckboxSpinner {
    private String memberName;
    private boolean selected;

    public SCCheckboxSpinner(String memberName) {
        this.memberName = memberName;
        this.selected = false;
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
}
