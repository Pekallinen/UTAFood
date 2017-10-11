package pekallinen.utafood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FoodAdapter extends ArrayAdapter<Food> {

    public FoodAdapter(Context context, ArrayList<Food> foods) {
        super(context, 0, foods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Food food = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_food, parent, false);
        }

        TextView foodName = (TextView) convertView.findViewById(R.id.food_name);
        foodName.setText(food.getName());

        TextView foodIngredients = (TextView) convertView.findViewById(R.id.food_ingredients);
        foodIngredients.setText(food.getIngredients());

        return convertView;
    }
}
