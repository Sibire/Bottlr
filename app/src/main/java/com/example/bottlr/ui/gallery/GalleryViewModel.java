package com.example.bottlr.ui.gallery;

// Not being used for anything at the moment

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}