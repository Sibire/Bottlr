package com.example.bottlr;

//region Imports
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
//endregion

public class Cocktail implements Parcelable {

    //region Fields
    // Fields initialization
    private String name;
    private String base;
    private String mixer;
    private String juice;
    private String liqueur;
    private String garnish;
    private String extra;
    private Uri photoUri;
    private String notes;
    private String keywords;
    private final String rating;

    private String bottleID;

    //endregion

    //region Constructor
    // Cocktail constructor
    public Cocktail(String name, String base, String mixer, String juice, String liqueur, String garnish, String extra,
                    Uri photoUri, String notes, String keywords, String rating) {
        this.name = name;
        this.base = base;
        this.mixer = mixer;
        this.juice = juice;
        this.liqueur = liqueur;
        this.garnish = garnish;
        this.extra = extra;
        this.photoUri = photoUri;
        this.notes = notes;
        this.keywords = keywords;
        this.rating = rating;
    }
    //endregion

    //region Getters / Setters

    // Bottle getters and setters for future use

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
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    // Rating
    public String getRating() { return rating; }
    public void setRating(String rating) { this.region = rating; }

    public String getBottleID() { return bottleID; }
    public void setBottleID(String ID) { this.bottleID = ID; }

    //endregion

    //region Create Bottle from Parcel Data

    // Creating a bottle from parcel data

    protected Cocktail(Parcel in) {
        name = in.readString();
        distillery = in.readString();
        type = in.readString();
        abv = in.readString();
        age = in.readString();
        photoUri = in.readParcelable(Uri.class.getClassLoader());
        notes = in.readString();
        region = in.readString();
        rating = in.readString();
        keywords = in.readString();
    }
    //endregion

    //region DescribeContents
    // Required for Parcelable
    // TODO: Verify this
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
        dest.writeString(keywords);
    }
    //endregion

    //region Create Bottle  / Store in Array
    // Creating a bottle from parcel data and storing it to the bottle array
    public static final Creator<Cocktail> CREATOR = new Creator<Cocktail>() {
        @Override
        public Cocktail createFromParcel(Parcel in) {
            return new Cocktail(in);
        }

        @Override
        public Cocktail[] newArray(int size) {
            return new Cocktail[size];
        }
    };
    //endregion

}
