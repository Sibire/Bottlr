package com.example.bottlr.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// This class does nothing yet, and is still as delivered from the template

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SlideshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is a slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}