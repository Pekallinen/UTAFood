package pekallinen.utafood;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONParser {

    // Pääkampus
    public static ArrayList<Food> PaakampusJSON(String JSONString) throws JSONException {
        // Format the input file a bit
        JSONString = JSONString.substring(6, JSONString.length()-2);
        JSONString = JSONString.replace("\\\"", "\"");

        JSONObject rootJSON = new JSONObject(JSONString);
        JSONArray MealOptions = rootJSON.getJSONArray(("MealOptions"));

        ArrayList<Food> foodList = new ArrayList<>();
        for(int i = 0; i < MealOptions.length(); i++) {
            JSONObject currentFood = MealOptions.getJSONObject(i);
            // Add the name of the current meal
            String foodName = currentFood.getString("Name").toUpperCase();

            // Add the components of the current meal
            String foodIngredients = "";
            JSONArray menuItems = currentFood.getJSONArray("MenuItems");
            for(int j = 0; j < menuItems.length(); j++) {
                JSONObject currentMenuItem = menuItems.getJSONObject(j);
                if(j != 0 && currentMenuItem.getString("Name").length() > 0) {
                    foodIngredients += "\n";
                }
                foodIngredients += currentMenuItem.getString("Name");
                if(currentMenuItem.getString("Name").length() > 0 && currentMenuItem.getString("Diets").length() > 0) {
                    foodIngredients += " (" + currentMenuItem.getString("Diets") + ")";
                }
            }

            foodList.add(new Food(foodName, foodIngredients));
        }
        return foodList;
    }

    // Minerva
    public static ArrayList<Food> MinervaJSON(String JSONString) throws JSONException {
        JSONObject rootJSON = new JSONObject(JSONString);
        JSONArray dayMenus = rootJSON.getJSONArray("MenusForDays");
        JSONObject today = dayMenus.getJSONObject(0);

        // Get a list of the foods available today
        JSONArray foods = today.getJSONArray("SetMenus");
        ArrayList<Food> foodList = new ArrayList<>();
        for(int i = 0; i < foods.length(); i++) {
            JSONObject currentFood = foods.getJSONObject(i);
            // Get the name of the current meal
            String foodName = currentFood.getString("Name");

            // Remove prices from the end of the string
            if(foodName.contains(" ")) {
                foodName = foodName.substring(0, foodName.indexOf(' '));
            }
            foodName = foodName.toUpperCase();

            // Get the components of the current meal
            String foodIngredients = "";
            JSONArray foodComponents = currentFood.getJSONArray("Components");
            for(int j = 0; j < foodComponents.length(); j++) {
                if(j != 0) {
                    foodIngredients += "\n";
                }
                foodIngredients += foodComponents.getString(j);
            }

            foodList.add(new Food(foodName, foodIngredients));
        }
        return foodList;
    }

    // Linna
    public static ArrayList<Food> LinnaJSON(String JSONString) throws JSONException {
        JSONObject rootJSON = new JSONObject(JSONString);
        JSONArray courses = rootJSON.getJSONArray(("courses"));

        ArrayList<Food> foodList = new ArrayList<>();
        for(int i = 0; i < courses.length(); i++) {
            JSONObject currentFood = courses.getJSONObject(i);

            String foodName = currentFood.getString("category").toUpperCase();

            String foodIngredients = currentFood.getString("title_fi");
            if(currentFood.has("properties")) {
                foodIngredients += " (" + currentFood.getString("properties") + ")";
            }
            if(currentFood.getString("desc_fi").length() > 0) {
                foodIngredients += "\n" + currentFood.getString("desc_fi");
                // The food description has a newline at the end so we remove it
                foodIngredients = foodIngredients.substring(0, foodIngredients.length()-1);
            }

            foodList.add(new Food(foodName, foodIngredients));
        }
        return foodList;
    }
}
