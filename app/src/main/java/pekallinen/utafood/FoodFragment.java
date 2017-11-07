package pekallinen.utafood;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;



public class FoodFragment extends Fragment {
    private final String[] RESTAURANT_URLS = {
            "http://www.juvenes.fi/DesktopModules/Talents.LunchMenu/LunchMenuServices.asmx/" +
                    "GetMenuByWeekday?KitchenId=13&MenuTypeId=60&Week=WEEKNUMBER&WeekDay=WEEKDAY" +
                    "&lang='fi'&format=json",
            "https://www.amica.fi/modules/json/json/Index?costNumber=0815&language=fi",
            "http://www.sodexo.fi/ruokalistat/output/daily_json/92/YEAR/MONTH/DAY/fi"};

    private static final int PAAKAMPUS = 0;
    private static final int MINERVA = 1;
    private static final int LINNA = 2;
    private static final String RESTAURANT_ID = "restaurant_id";

    private ArrayList<Food> mFoods;
    private FoodAdapter mAdapter;


    public static FoodFragment newInstance(int restaurant) {
        FoodFragment foodFragment = new FoodFragment();

        Bundle args = new Bundle();
        args.putInt(RESTAURANT_ID, restaurant);
        foodFragment.setArguments(args);

        return foodFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Save foodlists somewhere instead of redownloading JSON data after rotation
        // Retain the fragment when changing orientation
        // setRetainInstance(true);

        mFoods = new ArrayList<>();
        mAdapter = new FoodAdapter(getContext(), mFoods);

        DownloadJSONTask downloadJSONTask = new DownloadJSONTask();
        downloadJSONTask.execute(getArguments().getInt(RESTAURANT_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_foodlist, container, false);

        ListView listView = (ListView) v.findViewById(R.id.foodlist);
        listView.setAdapter(mAdapter);

        return v;
    }

    private class DownloadJSONTask extends AsyncTask<Integer, Void, ArrayList<Food>> {
        @Override
        protected ArrayList<Food> doInBackground(Integer... integers) {
            if(integers == null) {
                return null;
            }

            String address = RESTAURANT_URLS[integers[0]];
            Calendar calendar = Calendar.getInstance();

            // Format the restaurant url to current date's version
            switch (integers[0]) {
                case PAAKAMPUS:
                    address = address.replaceFirst("WEEKNUMBER", calendar.get(Calendar.WEEK_OF_YEAR) + "");
                    address = address.replaceFirst("WEEKDAY", calendar.get(Calendar.DAY_OF_WEEK) - 1 + "");
                case MINERVA:
                    // Minerva URL doesn't need editing
                case LINNA:
                    address = address.replaceFirst("YEAR", calendar.get(Calendar.YEAR) + "");
                    address = address.replaceFirst("MONTH", calendar.get(Calendar.MONTH) + 1 + "");
                    address = address.replaceFirst("DAY", calendar.get(Calendar.DAY_OF_MONTH) + "");
            }

            // Get the JSON file
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JSONData = null;
            try {
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer = new StringBuffer();
                String line;
                while( (line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0) {
                    return null;
                }

                JSONData = buffer.toString();
            }
            // TODO: Catch exceptions properly
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Parse JSON to ArrayList<Food> format
            try {
                if(address != null && JSONData != null) {
                    switch (integers[0]) {
                        case 0:
                            return JSONParser.PaakampusJSON(JSONData);
                        case 1:
                            return JSONParser.MinervaJSON(JSONData);
                        case 2:
                            return JSONParser.LinnaJSON(JSONData);
                    }
                }
            }
            catch(JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Food> result) {
            if(result != null && mAdapter != null) {
                mAdapter.clear();
                mAdapter.addAll(result);
            }
        }
    }
}
