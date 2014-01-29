package com.jacktech.gymik.fragments;

import android.app.ListFragment;
import android.os.Bundle;

public abstract class ExtraListFragment extends ListFragment {

    protected ExtraFragment.FragmentCallback fragmentCallback;
    private String className;

    public void setFragmentCallback(ExtraFragment.FragmentCallback fragmentCallback){
        this.fragmentCallback = fragmentCallback;
    }

    protected void callback(Bundle bundle){
        if(bundle != null)
            bundle.putString(ExtraFragment.FRAGMENT_NAME, className);
        if(fragmentCallback != null)
            fragmentCallback.callback(bundle);
    }

    protected void onCreate(Bundle savedInstanceStace, String className){
        super.onCreate(savedInstanceStace);
        this.className = className;
    }

}
