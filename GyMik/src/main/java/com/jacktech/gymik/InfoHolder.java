package com.jacktech.gymik;

import android.os.Bundle;

/**
 * Created by toor on 11.1.14.
 */
public class InfoHolder {

    private static InfoHolder instance;
    public static String JIDELNICEK_SHOWING = "jidelnicek_showing";

    private Bundle dataStore;

    private InfoHolder(){
        dataStore = new Bundle();
    }

    public static InfoHolder getInstance(){
        if(instance == null)
            instance = new InfoHolder();
        return instance;
    }

    public String getStringInfo(String key){
        return dataStore.getString(key);
    }

    public boolean getBooleanInfo(String key){
        return dataStore.getBoolean(key, false);
    }

    public int getIntInfo(String key){
        return dataStore.getInt(key, -1);
    }

    public void setStringInfo(String key, String value){
        dataStore.putString(key, value);
    }

    public void setBooleanInfo(String key, boolean value){
        dataStore.putBoolean(key, value);
    }

    public void setIntInfo(String key, int value){
        dataStore.putInt(key, value);
    }

}
