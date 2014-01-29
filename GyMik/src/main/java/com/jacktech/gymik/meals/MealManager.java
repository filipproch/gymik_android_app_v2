package com.jacktech.gymik.meals;

import com.jacktech.gymik.DataWorker;
import com.jacktech.gymik.UpdateClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by toor on 9.1.14.
 */
public class MealManager {

    private static MealManager instance;

    private ArrayList<MealDay> meals;
    private int maxMeals = 0;
    private MealManagerCallback callback;

    private MealManager(){}

    public static MealManager getInstance(){
        if(instance == null)
            instance = new MealManager();
        return instance;
    }

    public void load(){
        try {
            meals = new ArrayList<MealDay>();
            JSONArray object = DataWorker.getInstance().getJidlo().getJSONArray("data");
            for(int a = 0;a <object.length();a++){
                JSONObject mealDay = object.getJSONObject(a);
                MealDay day = MealDay.create(mealDay.getLong("day"));
                JSONArray jidla = mealDay.getJSONArray("data");
                for(int b = 0;b < jidla.length();b++){
                    JSONObject jidlo = jidla.getJSONObject(b);
                    day.addMeal(Meal.create(jidlo));
                    if(jidlo.getBoolean(Meal.MEAL_SELECTED))
                        day.setSelectedMeal(b);
                }
                if(maxMeals < jidla.length())
                    maxMeals = jidla.length();

                meals.add(day);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MealDay> getMeals(){
        return meals;
    }

    public void writeJidlo(){
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            for(MealDay day : meals){
                JSONObject dayJSON = new JSONObject();
                dayJSON.put("day", day.getDay());
                JSONArray dayMeals = new JSONArray();
                for(int a = 0;a < day.getMeals().size();a++)
                    dayMeals.put(day.getMeals().get(a).getJSON(day.getSelectedMeal() == a));
                dayJSON.put("data", dayMeals);
                array.put(dayJSON);
            }
            object.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataWorker.getInstance().writeJidlo(object);
        load();
    }

    public void updateJidlo(JSONObject data) {
        DataWorker.getInstance().writeJidlo(data);
        load();
    }

    public int getMaxMeals() {
        return maxMeals;
    }

    public void setMaxMeals(int maxMeals) {
        this.maxMeals = maxMeals;
    }

    public void orderMeal(int mealId, int i) {
        if(callback != null){
            callback.mealManagerAction(Action.MEAL_ORDERED);
            meals.get(mealId).setOrdered(i);
        }
    }

    public void finishOrdering(){
        if(callback != null){
            callback.mealManagerAction(Action.MEAL_ORDERING);
            UpdateClass.getInstance().orderJidlo();
        }
    }

    public void setCallback(MealManagerCallback callback) {
        this.callback = callback;
    }

    public int getTodayId() {
        Calendar c = Calendar.getInstance();
        for(int i = 0;i<meals.size();i++)
            if(meals.get(i).getDay() >= c.getTimeInMillis()-86400000)
                return i;
        return -1;
    }

    public interface MealManagerCallback{
        public void mealManagerAction(Action action);
    }

    public enum Action{
        MEAL_ORDERED, MEAL_ORDERING;
    }
}
