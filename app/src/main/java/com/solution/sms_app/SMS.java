package com.solution.sms_app;

import android.os.Parcel;
import android.os.Parcelable;

public class SMS implements Parcelable {
    public String address;
    public String date;
    public String body;

    public SMS(String address, String date, String body) {
        this.address = address;
        this.date = date;
        this.body = body;
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(date);
        dest.writeString(body);
    }
    public static final Creator CREATOR
            = new Creator() {
        public SMS createFromParcel(Parcel in) {
            return new SMS(in);
        }

        public SMS[] newArray(int size) {
            return new SMS[size];
        }

    };
    public SMS(Parcel source) {
        address = source.readString();
        date = source.readString();
        body = source.readString();
    }
}
