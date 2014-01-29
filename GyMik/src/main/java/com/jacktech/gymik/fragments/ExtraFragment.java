package com.jacktech.gymik.fragments;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by toor on 11.1.14.
 */
public abstract class ExtraFragment extends Fragment{

    public static String FRAGMENT_NAME = "fragment_name";
    protected FragmentCallback fragmentCallback;
    private String className;

    public interface FragmentCallback{
        public void callback(Bundle bundle);
    }

    public enum ActionType{
        ACTION_BAR_TAB_SELECTED, ACTION_BAR_TAB_UNSELECTED;
    }

    public void setFragmentCallback(FragmentCallback fragmentCallback){
        this.fragmentCallback = fragmentCallback;
    }

    public void action(ActionType actionType, Bundle bundle){

    }

    protected void callback(Bundle bundle){
        if(bundle != null)
            bundle.putString(FRAGMENT_NAME, className);
        if(fragmentCallback != null)
            fragmentCallback.callback(bundle);
    }

    protected void onCreate(Bundle savedInstanceStace, String className){
        super.onCreate(savedInstanceStace);
        this.className = className;
    }

}
