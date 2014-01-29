package com.jacktech.gymik.meals;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by toor on 6.12.13.
 */

public class Meal {
    public static String MEAL_NAME = "meal_name";
    public static String MEAL_TYPE = "meal_type";
    public static String MEAL_DIET_ID = "meal_diet_id";
    public static String MEAL_MEAL_ID = "meal_meal_id";
    public static String MEAL_SELECTED = "meal_selected";

    private String name;
    private Meal.Type type;
    private int dietId, mealId;
    private SelectionCallback selectionCallback;

    public Meal(String name, Type type, int dietId, int mealId){
        this.name = name;
        this.type = type;
        this.dietId = dietId;
        this.mealId = mealId;
    }

    private Meal() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getDietId() {
        return dietId;
    }

    public void setDietId(int dietId) {
        this.dietId = dietId;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public void setSelectionCallback(SelectionCallback selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    public JSONObject getJSON(boolean selected) {
        JSONObject object = new JSONObject();
        try {
            object.put(MEAL_NAME, name);
            object.put(MEAL_TYPE, type.typeInt);
            object.put(MEAL_DIET_ID, dietId);
            object.put(MEAL_MEAL_ID, mealId);
            object.put(MEAL_SELECTED, selected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static Meal create(JSONObject object){
        Meal meal = new Meal();
        try {
            meal.setName(object.getString(Meal.MEAL_NAME));
            meal.setType(Meal.Type.getFromInt(object.getInt(Meal.MEAL_TYPE)));
            meal.setDietId(object.getInt(Meal.MEAL_DIET_ID));
            meal.setMealId(object.getInt(Meal.MEAL_MEAL_ID));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return meal;
    }

    public enum Type{
        SOUP(0, "Polévka"), MAIN_DISH(1, "Jídlo"), DESERT(2,"Dezert"), UNKNOWN(-1,"Neznámý");

        private int typeInt;
        private String typeString;

        Type(int typeInt, String typeString) {
            this.typeInt = typeInt;
            this.typeString = typeString;
        }

        @Override
        public String toString(){
            return typeString;
        }

        public static Type getFromString(String type){
            if(type.contains("Polévka:")){
                return SOUP;
            }else if(type.contains("Menu")){
                return MAIN_DISH;
            }else if(type.contains("Zákusek")){
                return DESERT;
            }
            return UNKNOWN;
        }

        public static Type getFromInt(int type) {
            Type[] valueEnums = Type.values();
            for(Type t : valueEnums){
                if(t.typeInt == type)
                    return t;
            }
            return UNKNOWN;
        }

        public int value() {
            return typeInt;
        }
    }

    public void select(){
        if(selectionCallback != null)
            selectionCallback.selected(this);
    }

    public interface SelectionCallback{
        public void selected(Meal meal);
    }

}
