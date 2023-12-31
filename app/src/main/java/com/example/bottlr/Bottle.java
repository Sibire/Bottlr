package com.example.bottlr;

//region Imports
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
//endregion

public class Bottle implements Parcelable {

    //region Fields
    // Fields initialization
    private String name;
    private String distillery;
    private String type;
    private String abv;
    private String age;
    private Uri photoUri;
    private String notes;
    private String region;
    private Set<String> keywords;
    private String rating;

    //endregion

    //region Constructor
    // Bottle constructor
    public Bottle(String name, String distillery, String type, String abv, String age, Uri photoUri, String notes, String region, Set<String> keywords, String rating) {
        this.name = name;
        this.distillery = distillery;
        this.type = type;
        this.abv = abv;
        this.age = age;
        this.photoUri = photoUri;
        this.notes = notes;
        this.region = region;
        this.keywords = keywords;
        this.rating = rating;
    }
    //endregion

    //region Getters / Setters

    // Bottle getters and setters
    // Not all of these are used or implemented *yet* but as there's going to be an edit function
    // I need to cover all of my bases

    // Name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Distillery
    public String getDistillery() { return distillery; }
    public void setDistillery(String distillery) { this.distillery = distillery; }

    // Type
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // ABV
    public String getAbv() { return abv; }
    public void setAbv(String abv) { this.abv = abv; }

    // Age
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    // User Notes
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Photo URI
    public Uri getPhotoUri() { return photoUri; }
    public void setPhotoUri(Uri photoUri) { this.photoUri = photoUri; }

    // Region
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    // Keywords
    public Set<String> getKeywords() { return keywords; }
    public void setKeywords(Set<String> keywords) { this.keywords = keywords; }

    // Rating
    public String getRating() { return rating; }
    public void setRating(String rating) { this.region = rating; }
    //endregion

    //region Create Bottle from Parcel Data

    // Creating a bottle from parcel data

    protected Bottle(Parcel in) {
        name = in.readString();
        distillery = in.readString();
        type = in.readString();
        abv = in.readString();
        age = in.readString();
        photoUri = in.readParcelable(Uri.class.getClassLoader());
        notes = in.readString();
        region = in.readString();
        rating = in.readString();
        keywords = new HashSet<>(Arrays.asList(in.readString().split(",")));
    }
    //endregion

    //region DescribeContents
    // Required for Parcelable
    @Override
    public int describeContents() {
        return 0;
    }
    //endregion

    //region Write to Parcel

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
        dest.writeString(region);
        dest.writeString(rating);
        dest.writeString(String.join(",", keywords));
    }
    //endregion

    //region Create Bottle  / Store in Array
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
    //endregion

}
