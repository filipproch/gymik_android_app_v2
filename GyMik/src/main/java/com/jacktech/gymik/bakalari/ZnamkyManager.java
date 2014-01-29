package com.jacktech.gymik.bakalari;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jacktech.gymik.Adapters;
import com.jacktech.gymik.Config;
import com.jacktech.gymik.DataWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by toor on 9.1.14.
 */
public class ZnamkyManager {

    private static final String TAG = "ZnamkyManager";
    private static ZnamkyManager instance;
    private Context context;

    private HashMap<String, ArrayList<Znamka>> marks;
    private HashMap<String, ArrayList<Znamka>> reloadMarks;
    private OnZnamkyActionListener onZnamkyActionListener;
    private boolean reloadingMarks = false;
    private boolean noMarks = false;

    public void setOnZnamkyActionListener(OnZnamkyActionListener onZnamkyActionListener) {
        this.onZnamkyActionListener = onZnamkyActionListener;
    }

    public interface OnZnamkyActionListener{
        public void onAction(ZnamkyManager.Action action, Bundle actionInfo);
    }

    public enum Action{
        NEW_MARK, REMOVED_MARK, BAD_AVERAGE;
    }

    private ZnamkyManager(){
        marks = new HashMap<String, ArrayList<Znamka>>();
    }

    public static ZnamkyManager getInstance(){
        if(instance == null)
            instance = new ZnamkyManager();
        return instance;
    }



    public void init(Context context) throws JSONException {
        this.context = context;
        loadMarksDb();
    }

    public void loadMarksDb() throws JSONException {
        marks = new HashMap<String, ArrayList<Znamka>>();
        JSONObject object = DataWorker.getInstance().getZnamky();

        if(object == null){
            noMarks = true;
            Config.getInstance().setConfigBool(Config.KEY_BAKALARI_DOWNLOADED, false);
            return;
        }

        if(object.has("noMarks") && object.getBoolean("noMarks")){
            noMarks = true;
            return;
        }

        Iterator<String> iterator = object.keys();
        while(iterator.hasNext()){
            String predmet = iterator.next();
            ArrayList<Znamka> znamky = new ArrayList<Znamka>();
            JSONArray array = object.getJSONArray(predmet);
            for(int a = 0;a < array.length();a++)
                znamky.add(Znamka.getZnamkaFromJSONObject(array.getJSONObject(a)));
            marks.put(predmet, znamky);
        }

        if(marks.size() == 0){
            noMarks = true;
        }
    }

    public void loadMark(String predmet, Znamka znamka){
        ArrayList<Znamka> znamky = marks.get(predmet);
        boolean newMark = true;

        if(znamky == null){
            znamky = new ArrayList<Znamka>();
            marks.put(predmet, znamky);
        }

        for(Znamka z : znamky){
            if(z.equals(znamka)){
                newMark = false;
                break;
            }
        }
        if(newMark){
            Log.v(TAG, "New mark");
            if(onZnamkyActionListener != null){
                Bundle bundle = znamka.getBundle();
                bundle.putString(Predmet.PREDMET, predmet);
                onZnamkyActionListener.onAction(Action.NEW_MARK, bundle);
            }
            if(reloadingMarks){
                if(!reloadMarks.containsKey(predmet))
                    reloadMarks.put(predmet, new ArrayList<Znamka>());
                reloadMarks.get(predmet).add(znamka);
            }
        }else{
            if(reloadingMarks){
                if(!reloadMarks.containsKey(predmet))
                    reloadMarks.put(predmet, new ArrayList<Znamka>());
                reloadMarks.get(predmet).add(znamka);
            }
        }
    }

    public void startMarksReload() {
        if(!reloadingMarks){
            reloadMarks = new HashMap<String, ArrayList<Znamka>>();
            reloadingMarks = true;
        }
    }

    public void finishMarksReload() {
        if(reloadingMarks){
            Config.getInstance().setConfigBool(Config.KEY_BAKALARI_DOWNLOADED, true);
            marks = reloadMarks;
            reloadMarks = null;
            reloadingMarks = false;
            writeMarks();
        }
    }

    private void writeMarks() {
        try{
            JSONObject object = new JSONObject();
            if(!noMarks){
                Iterator<Map.Entry<String,ArrayList<Znamka>>> iterator = marks.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<String, ArrayList<Znamka>> entry = iterator.next();
                    Log.v(TAG, "writing predmet = "+entry.getKey()+", marks count="+entry.getValue().size());
                    JSONArray array = new JSONArray();
                    for(Znamka z : entry.getValue())
                        array.put(z.getJSONObject());
                    object.put(entry.getKey(), array);
                }
            }else{
                object.put("noMarks", true);
            }
            DataWorker.getInstance().writeZnamky(object);
        }catch (JSONException e){
            e.printStackTrace();;
        }
    }

    public boolean isReloadingMarks() {
        return reloadingMarks;
    }

    public List<Znamka> getZnamky(String name) {
        return marks.get(name);
    }

    public ArrayList<Adapters.PredmetyAdapter.PredmetyListItem> getPredmety() {
        ArrayList<Adapters.PredmetyAdapter.PredmetyListItem> predmety = new ArrayList<Adapters.PredmetyAdapter.PredmetyListItem>();
        Iterator<String> iterator = marks.keySet().iterator();
        while(iterator.hasNext()){
            String predmet = iterator.next();
            Predmet p = Predmet.create(predmet, getZnamky(predmet));
            predmety.add(new Adapters.PredmetyAdapter.PredmetyListItem(p, 0));
        }
        return predmety;
    }

    public double getPrumer(){
        double soucetHodnot = 0;
        int pocetHodnot = 0;
        Iterator<Map.Entry<String,ArrayList<Znamka>>> iterator = marks.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ArrayList<Znamka>> entry = iterator.next();
            double prumer = getPrumer(entry.getValue());
            soucetHodnot += prumer;
            pocetHodnot++;
        }
        if(pocetHodnot == 0)
            return 0;
        return new BigDecimal((double)soucetHodnot/pocetHodnot).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    public int getProspech() {
        int prospech = 2;
        double soucetHodnot = 0;
        int pocetHodnot = 0;
        Iterator<Map.Entry<String,ArrayList<Znamka>>> iterator = marks.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ArrayList<Znamka>> entry = iterator.next();
            double prumer = getPrumer(entry.getValue());
            soucetHodnot += prumer;
            pocetHodnot++;
        }
        if(prospech == 2 && pocetHodnot > 0){
            if((double)soucetHodnot/pocetHodnot >= 1.5)
                prospech = 0;
            else
                prospech = 1;
        }
        return prospech;
    }

    public double getPrumer(ArrayList<Znamka> znamky){
        int soucetHodnot = 0;
        int soucetVah = 0;
        for(Znamka z : znamky){
            soucetHodnot += z.getVaha()*z.getHodnota();
            soucetVah += z.getVaha();
        }
        double prumer = -1;
        if(soucetVah != 0){
            prumer = (double)soucetHodnot/soucetVah;
            BigDecimal a = new BigDecimal(prumer);
            prumer = a.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        }
        return prumer;
    }

    public double getPrumer(String predmet){
        return getPrumer(marks.get(predmet));
    }

    public boolean areThereMarks(){
        return !noMarks;
    }

    public void setNoMarks() {
        this.noMarks = true;
        writeMarks();
    }

}
