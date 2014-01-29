package com.jacktech.gymik.bakalari;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Znamka implements Comparable<Znamka> {

    public static String ZNAMKA_HODNOTA = "mark_value";
    public static String ZNAMKA_VAHA = "mark_weight";
    public static String ZNAMKA_VAHA_INFO = "mark_weight_info";
    public static String ZNAMKA_DATE = "mark_date";
    public static String ZNAMKA_POPIS = "mark_description";

	private double hodnota = -1;
	private int vaha = 1;
	private String vahaInfo = null;
	private Long date = 0L;
	private String popis = null;
	
	public Znamka(double hodnota, int vaha, String vahaInfo, Long date, String popis){
		this.hodnota = hodnota;
		this.vaha = vaha;
		this.vahaInfo = vahaInfo;
		this.date = date;
		this.popis = popis;
	}

    public Znamka(double hodnota, int vaha, String vahaInfo, String date, String popis){
        this.hodnota = hodnota;
        this.vaha = vaha;
        this.vahaInfo = vahaInfo;
        this.date = getTimestamp(date);
        this.popis = popis;
    }

    public Long getDate(){
        return date;
    }

    public String getDateDisplay() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        return c.get(Calendar.DAY_OF_MONTH)+"."+(c.get(Calendar.MONTH)+1)+".";
    }

    public Long getTimestamp(String date){
        Calendar c = Calendar.getInstance();
        String[] datum = date.split("\\.");
        int day = Integer.parseInt(datum[0]);
        int month = Integer.parseInt(datum[1]);
        int year = c.get(Calendar.YEAR);
        if( month > 8 && c.get(Calendar.MONTH) < 8)
            year--;
        Calendar zn = new GregorianCalendar(year, month, day);
        return zn.getTimeInMillis();
    }

    @Override
    public int compareTo(Znamka z){
        return date.compareTo(z.getDate());
    }

    public String getPopis() {
        return popis;
    }

    public void setPopis(String popis) {
        this.popis = popis;
    }

    public String getVahaInfo() {
        return vahaInfo;
    }

    public void setVahaInfo(String vahaInfo) {
        this.vahaInfo = vahaInfo;
    }

    public Integer getVaha() {
        return vaha;
    }

    public void setVaha(int vaha) {
        this.vaha = vaha;
    }

    /**
     *
     * @return Mark if set, -1 if not set, 0 if N
     */
    public Double getHodnota() {
        return hodnota;
    }

    public void setHodnota(int hodnota) {
        this.hodnota = hodnota;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof Znamka){
            Znamka znamka = (Znamka) object;
            if(znamka.getHodnota() == getHodnota() && znamka.getVaha() == getVaha() && popis.equals(popis)){
                if(znamka.compareTo(znamka) == 0){
                    return true;
                }
            }
        }
        return false;
    }

    public static Znamka getZnamkaFromBundle(Bundle bundle){
        return new Znamka(bundle.getInt(ZNAMKA_HODNOTA), bundle.getInt(ZNAMKA_VAHA), bundle.getString(ZNAMKA_VAHA_INFO), bundle.getLong(ZNAMKA_DATE), bundle.getString(ZNAMKA_POPIS));
    }

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putDouble(ZNAMKA_HODNOTA, getHodnota());
        bundle.putInt(ZNAMKA_VAHA, getVaha());
        bundle.putString(ZNAMKA_VAHA_INFO, getVahaInfo());
        bundle.putLong(ZNAMKA_DATE, date);
        bundle.putString(ZNAMKA_POPIS, getPopis());
        return bundle;
    }

    public static Znamka getZnamkaFromJSONObject(JSONObject object) throws JSONException {
        return new Znamka(object.getInt(ZNAMKA_HODNOTA), object.getInt(ZNAMKA_VAHA), object.getString(ZNAMKA_VAHA_INFO), object.getLong(ZNAMKA_DATE), object.getString(ZNAMKA_POPIS));
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(ZNAMKA_HODNOTA, getHodnota());
        object.put(ZNAMKA_VAHA, getVaha());
        object.put(ZNAMKA_VAHA_INFO, getVahaInfo());
        object.put(ZNAMKA_DATE, getDate());
        object.put(ZNAMKA_POPIS, getPopis());
        return object;
    }
}
