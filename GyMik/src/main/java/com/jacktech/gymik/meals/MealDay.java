package com.jacktech.gymik.meals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by toor on 11.1.14.
 */
public class MealDay implements Meal.SelectionCallback {
    private Long day;
    private ArrayList<Meal> meals;
    private int selectedMeal = -1;
    private int ordered = -1;

    private MealDay(Long day){
        this.day = day;
        this.meals = new ArrayList<Meal>();
    }

    public void addMeal(Meal meal){
        this.meals.add(meal);
        if(meal.getType() == Meal.Type.MAIN_DISH && selectedMeal == -1)
            selectedMeal = meals.size()-1;
        if(meal.getType() == Meal.Type.MAIN_DISH)
            meal.setSelectionCallback(this);
    }

    public Meal getMeal(int mealIndex){
        return meals.get(mealIndex);
    }

    public ArrayList<Meal> getMeals(){
        return meals;
    }

    public int mealsCount(){
        return meals.size();
    }

    public int getSelectedMeal(){
        return selectedMeal;
    }

    public void setSelectedMeal(int selectedMeal){
        this.selectedMeal = selectedMeal;
    }

    public static MealDay create(Long day) {
        return new MealDay(day);
    }

    public static Long getTimestampFromDate(String date) throws ParseException{
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.parse(date).getTime();
    }

    @Override
    public void selected(Meal meal) {
        if(meals.contains(meal))
            selectedMeal = meals.indexOf(meal);
    }

    public Long getDay() {
        return day;
    }

    public static String getDateFromTimestamp(Long day) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day);
        String dayString = getDayName(c.get(Calendar.DAY_OF_WEEK))+" - "+c.get(Calendar.DAY_OF_MONTH)+"."+(c.get(Calendar.MONTH)+1)+"."+c.get(Calendar.YEAR);
        return dayString;
    }

    private static String getDayName(int dayOfWeek) {
        switch (dayOfWeek){
            case Calendar.MONDAY:
                return "Pondělí";
            case Calendar.TUESDAY:
                return "Úterý";
            case Calendar.WEDNESDAY:
                return "Středa";
            case Calendar.THURSDAY:
                return "Čtvrtek";
            case Calendar.FRIDAY:
                return "Pátek";
        }
        return "???";
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }

    public int getOrdered() {
        return ordered;
    }

    public String getDayString() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(day);
        return c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
    }
}
