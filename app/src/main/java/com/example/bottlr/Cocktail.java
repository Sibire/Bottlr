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
    private String rating;
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

    // Cocktail getters and setters for future use

    // Name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Base
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    // Mixer
    public String getMixer() { return mixer; }
    public void setMixer(String mixer) { this.mixer = mixer; }

    // Juice
    public String getJuice() { return juice; }
    public void setJuice(String juice) { this.juice = juice; }

    // Liqueur
    public String getLiqueur() { return liqueur; }
    public void setLiqueur(String liqueur) { this.liqueur = liqueur; }

    // Garnish
    public String getGarnish() { return garnish; }
    public void setGarnish(String garnish) { this.garnish = garnish; }

    // Extra
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }

    // User Notes
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Photo URI
    public Uri getPhotoUri() { return photoUri; }
    public void setPhotoUri(Uri photoUri) { this.photoUri = photoUri; }

    // Keywords
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    // Rating
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    //endregion

    //region Create Bottle from Parcel Data

    // Creating a bottle from parcel data

    protected Cocktail(Parcel in) {
        name = in.readString();
        base = in.readString();
        mixer = in.readString();
        juice = in.readString();
        liqueur = in.readString();
        garnish = in.readString();
        extra = in.readString();
        photoUri = in.readParcelable(Uri.class.getClassLoader());
        notes = in.readString();
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
        dest.writeString(base);
        dest.writeString(mixer);
        dest.writeString(juice);
        dest.writeString(liqueur);
        dest.writeString(garnish);
        dest.writeString(extra);
        dest.writeParcelable(photoUri, flags);
        dest.writeString(notes);
        dest.writeString(rating);
        dest.writeString(keywords);
    }
    //endregion

    //region Create Cocktail  / Store in Array
    // Creating a cocktail from parcel data and storing it to the cocktail array
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
