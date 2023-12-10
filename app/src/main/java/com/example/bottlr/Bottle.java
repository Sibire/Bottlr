package com.example.bottlr;

import java.net.URI;

public class Bottle {
    private String name;
    private String distillery;
    private String type;
    private String abv;
    private String age;
    private String notes; // Optional, if you want to include it
    private URI photoUri;

    // Constructor
    public Bottle(String name, String distiller, String type, String abv, String age, URI photoUri) {
        this.name = name;
        this.distillery = distiller;
        this.type = type;
        this.abv = abv;
        this.age = age;
        this.photoUri = photoUri;
    }

    // Getters
    public String getName() { return name; }
    public String getDistillery() { return distillery; }
    public String getType() { return type; }
    public String getAbv() { return abv; }
    public String getAge() { return age; }
    public URI getPhotoUri() { return photoUri; }
}
