package com.weikang.getindutch;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceiptItem implements Parcelable {

    private String itemDescriptions;
    private Float itemPrice;
    private String targetGroup;

    public ReceiptItem(String itemDescriptions, Float itemPrice) {
        this.itemDescriptions = itemDescriptions;
        this.itemPrice = itemPrice;
        this.targetGroup = "";
    }

    public ReceiptItem(Parcel pc){
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

    public static final Parcelable.Creator<ReceiptItem> CREATOR = new Parcelable.Creator<ReceiptItem>(){
        @Override
        public ReceiptItem createFromParcel(Parcel source) {
            return new ReceiptItem(source);
        }

        @Override
        public ReceiptItem[] newArray(int size) {
            return new ReceiptItem[size];
        }
    };

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getTargetGroup() {
        return targetGroup;
    }
}
