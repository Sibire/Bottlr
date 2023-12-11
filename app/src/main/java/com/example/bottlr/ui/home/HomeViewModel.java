package com.example.bottlr.ui.home;

// Not being used for anything at the moment

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}