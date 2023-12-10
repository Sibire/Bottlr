package com.example.bottlr;

import android.net.Uri;

public class Bottle {
    private String name;
    private String distillery;
    private String type;
    private String abv;
    private String age;
    private Uri photoUri;
    private String notes;

    // Updated constructor
    public Bottle(String name, String distillery, String type, String abv, String age, Uri photoUri, String notes) {
        this.name = name;
        this.distillery = distillery;
        this.type = type;
        this.abv = abv;
        this.age = age;
        this.photoUri = photoUri;
        this.notes = notes;
    }

    // Getters and Setters
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
}
