package com.example.bottlr;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Bottle implements Parcelable {

    // Fields initialization
    private String name;
    private String distillery;
    private String type;
    private String abv;
    private String age;
    private Uri photoUri;
    private String notes;

    // Bottle constructor
    public Bottle(String name, String distillery, String type, String abv, String age, Uri photoUri, String notes) {
        this.name = name;
        this.distillery = distillery;
        this.type = type;
        this.abv = abv;
        this.age = age;
        this.photoUri = photoUri;
        this.notes = notes;
    }

    // Bottle getters and setters
    // Not all of these are used *yet* but as there's going to be an edit function
    // I wanted to cover all of my bases
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDistillery() { return distillery; }
    public void setDistillery(String distillery) { this.distillery = distillery; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAbv() { return abv; }
    public void setAbv(String abv) { this.abv = abv; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Uri getPhotoUri() { return photoUri; }
    public void setPhotoUri(Uri photoUri) { this.photoUri = photoUri; }

    // Creating a bottle from parcel data
    protected Bottle(Parcel in) {
        name = in.readString();
        distillery = in.readString();
        type = in.readString();
        abv = in.readString();
        age = in.readString();
        photoUri = in.readParcelable(Uri.class.getClassLoader());
        notes = in.readString();
    }

    // Writing to parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(distillery);
        dest.writeString(type);
        dest.writeString(abv);
        dest.writeString(age);
        dest.writeParcelable(photoUri, flags);
        dest.writeString(notes);
    }

    // Needed to get parcelable to work
    @Override
    public int describeContents() {
        return 0;
    }

    // Creating a bottle from parcel data and storing it to the bottle array
    public static final Parcelable.Creator<Bottle> CREATOR = new Parcelable.Creator<Bottle>() {
        @Override
        public Bottle createFromParcel(Parcel in) {
            return new Bottle(in);
        }

        @Override
        public Bottle[] newArray(int size) {
            return new Bottle[size];
        }
    };
}
