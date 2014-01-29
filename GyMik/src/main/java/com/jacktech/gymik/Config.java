package com.jacktech.gymik;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Config {

    private static final String TAG = "Config";
    private static Config instance;

    public static final String KEY_CONFIG_VERSION = "config_version";
    public static final String KEY_CLASS = "class";
    public static final String KEY_SCHOOL_YEAR = "school_year";
    public static final String KEY_BAKALARI_USER = "bak_user";
    public static final String KEY_BAKALARI_PASSWORD = "bak_psw";
    public static final String KEY_BAKALARI_DOWNLOADED = "bak_downloaded";
    public static final String KEY_LAST_SUPLOVANI_DOWNLOAD = "last_suplov";
    public static final String KEY_SUPLOVANI_DOWNLOAD_TIME = "suplov_download_time";
    public static final String KEY_SUPLOVANI_AUTO_DOWNLOAD = "suplov_auto_download";
    public static final String KEY_LAST_WEEK_UPDATE = "last_week";
    public static final String KEY_SHOW_UPDATES = "show_updates";
    public static final String KEY_JIDELNA_USERNAME = "jidlo_user";
    public static final String KEY_JIDELNA_PASSWORD = "jidlo_psw";

	private SharedPreferences preferences;
    private Context context;

    private static int configVersion = 2;

    private static final Object[][] defaultConfigValues = new Object[][]{
            {KEY_CONFIG_VERSION,configVersion},
            {KEY_CLASS,"-"},
            {KEY_SCHOOL_YEAR,"-"},
            {KEY_BAKALARI_USER,"-"},
            {KEY_BAKALARI_PASSWORD,"-"},
            {KEY_BAKALARI_DOWNLOADED, false},
            {KEY_LAST_SUPLOVANI_DOWNLOAD,-1L},
            {KEY_SUPLOVANI_DOWNLOAD_TIME,"school"},
            {KEY_SUPLOVANI_AUTO_DOWNLOAD,true},
            {KEY_LAST_WEEK_UPDATE,0L},
            {KEY_SHOW_UPDATES,0},
            {KEY_JIDELNA_USERNAME,"-"},
            {KEY_JIDELNA_PASSWORD,"-"}};

    private Config(){}

    public static Config getInstance(){
        if(instance == null)
            instance = new Config();
        return instance;
    }

    public boolean init(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("main",0);
		if(configVersion > getConfigInt(KEY_CONFIG_VERSION, -1)){
            Log.v(TAG, "Outdated config found or first launch, configVersion="+getConfigInt(KEY_CONFIG_VERSION));
            SharedPreferences.Editor editor = preferences.edit();
            for(Object[] config: defaultConfigValues){
                if(!preferences.contains((String)config[0])){
                    if(config[1] instanceof Integer)
                        editor.putInt((String)config[0],(Integer)config[1]);
                    else if(config[1] instanceof Boolean)
                        editor.putBoolean((String)config[0], (Boolean)config[1]);
                    else if(config[1] instanceof String)
                        editor.putString((String) config[0], (String)config[1]);
                    else if(config[1] instanceof Long)
                        editor.putLong((String) config[0], (Long) config[1]);
                }
            }
            editor.putInt(KEY_CONFIG_VERSION, configVersion);
            editor.commit();
            return true;
		}
        return false;
	}
	
	public void setConfigInt(String config, int value){
		SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(config, value);
        editor.commit();
	}

    public void setConfigLong(String config, long value) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putLong(config, value);
        editor.commit();
    }

    public void setConfigBool(String config, boolean value){
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(config, value);
        editor.commit();
    }

    public void setConfigString(String config, String value){
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(config, value);
        editor.commit();
    }
	
	public int getConfigInt(String config, int defaultValue){
		return preferences.getInt(config, defaultValue);
	}

    public long getConfigLong(String config, long defaultValue){
        return preferences.getLong(config, defaultValue);
    }

    public boolean getConfigBoolean(String config, boolean defaultValue){
        return preferences.getBoolean(config, defaultValue);
    }

    public String getConfigString(String config, String defaultValue){
        return preferences.getString(config, defaultValue);
    }

    /**
     *
     * @param config key to a configuration
     * @return configuration value for specified key, if not found returns 0
     */
    public int getConfigInt(String config){
        return preferences.getInt(config, 0);
    }

    /**
     *
     * @param config key to a configuration
     * @return configuration value for specified key, if not found returns 0
     */
    public long getConfigLong(String config){
        return preferences.getLong(config, 0L);
    }

    /**
     *
     * @param config key to a configuration
     * @return configuration value for specified key, if not found returns false
     */
    public boolean getConfigBoolean(String config){
        return preferences.getBoolean(config, false);
    }

    /**
     *
     * @param config key to a configuration
     * @return configuration value for specified key, if not found returns null
     */
    public String getConfigString(String config){
        return preferences.getString(config, null);
    }

}
