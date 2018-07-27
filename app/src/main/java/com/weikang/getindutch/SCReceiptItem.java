package com.weikang.getindutch;

import android.os.Parcel;
import android.os.Parcelable;

public class SCReceiptItem implements Parcelable {

    private String itemDescriptions;
    private Float itemPrice;
    private String targetGroup;

    public SCReceiptItem(String itemDescriptions, Float itemPrice) {
        this.itemDescriptions = itemDescriptions;
        this.itemPrice = itemPrice;
        this.targetGroup = "";
    }

    public SCReceiptItem(Parcel pc){
        itemDescriptions = pc.readString();
        itemPrice = pc.readFloat();
        targetGroup = pc.readString();
    }

    public String getItemDescriptions() {
        return itemDescriptions;
    }

    public Float getItemPrice() {
        return itemPrice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemDescriptions);
        dest.writeFloat(itemPrice);
        dest.writeString(targetGroup);
    }

    public static final Parcelable.Creator<SCReceiptItem> CREATOR = new Parcelable.Creator<SCReceiptItem>(){
        @Override
        public SCReceiptItem createFromParcel(Parcel source) {
            return new SCReceiptItem(source);
        }

        @Override
        public SCReceiptItem[] newArray(int size) {
            return new SCReceiptItem[size];
        }
    };

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getTargetGroup() {
        return targetGroup;
    }
}
