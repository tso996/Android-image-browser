package uk.ac.tees.aad.b1143506;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import uk.ac.tees.aad.b1143506.Adapters.ToDoAdapter;
import uk.ac.tees.aad.b1143506.Model.ToDoModel;
import uk.ac.tees.aad.b1143506.Utils.DatabaseHandler;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    //    Button button;
    Animation rotateOpen;
    Animation rotateClose;
    Animation fromBottom;
    Animation toBottom;
    Animation landscape_from_bottom;
    Animation landscape_to_bottom;
    //api to find location based on longitude and lattitude
//https://api.myptv.com/geocoding/v1/locations/by-position/54.574226/-1.234956?language=en&apiKey=MjhhNDBjN2JmMmI2NGNmNGIyMWFjOWZlMDQ3OWIwOWI6MmQyMDY5YmYtOWJmMy00ZTg4LWE5NjctNDE1ZmFlMDM2MDdj
    private DatabaseHandler db;
    private static final int REQUEST_LOCATION_PERMISSION =1 ;
    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private FloatingActionButton add_task_button;
    private FloatingActionButton add_image_button;
    private FloatingActionButton search_location_button;
    private boolean clicked = false;
    public static boolean getLocationCheck = false;
    public static boolean getCustomLocation = false;

    public static String currentLocationAddress = "???";
    public static String chosenCustomLocation = " ";
    double latitude;
    double longitude;
    int orientation;

    private List<ToDoModel> taskList;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DatabaseHandler(this);
        db.openDatabase();



        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
//        button = (Button)findViewById(R.id.map_button);
        fab = findViewById(R.id.fab);
        add_image_button = findViewById(R.id.add_image);
        search_location_button = findViewById(R.id.search_location);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db,MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);


        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        tasksAdapter.setTasks(taskList);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /* User's latitude and longitude is fetched here using the location object. */
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("latitude and longitude",Double.toString(latitude)+" "+Double.toString(longitude));
                //=== Getting city name from lattitude and longitude from 3rd party api
                String url = "https://api.myptv.com/geocoding/v1/locations/by-position/"+Double.toString(latitude)+"/"+Double.toString(longitude)+"?language=en&apiKey=MjhhNDBjN2JmMmI2NGNmNGIyMWFjOWZlMDQ3OWIwOWI6MmQyMDY5YmYtOWJmMy00ZTg4LWE5NjctNDE1ZmFlMDM2MDdj";
                Log.d("STATE",url);
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("STATE","received response");
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("locations");
                            Log.d("STATE", String.valueOf(jsonArray.length()));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
//                        Log.d("STATE", String.valueOf(jObject.getString("formattedAddress")));
                                currentLocationAddress = jObject.getJSONObject("address").getString("city");
                                Log.d("location from main: ",currentLocationAddress);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("volley error: ",error.toString());
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);

                //===
            }
        };

        //ask for permission to get the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientation = getResources().getConfiguration().orientation;
                onfabClicked();

            }
        });

        add_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationCheck = true;
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
                locationManager.removeUpdates(locationListener);

            }
        });
        //loads google maps activity
        search_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationCheck = false;
                getCustomLocation = true;
                locationManager.removeUpdates(locationListener);

                new AddNewTaskWithLocation().show(getSupportFragmentManager(),AddNewTaskWithLocation.TAG);
            }
        });


    }

    public void assignAnimations(){
        //this is put here so that the animations don't bug out after the first play
        rotateOpen = AnimationUtils.loadAnimation(
                this,R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(
                this,R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(
                this,R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(
                this,R.anim.to_bottom_anim);
        landscape_from_bottom = AnimationUtils.loadAnimation(this,R.anim.landscape_from_bottom_anim);
        landscape_to_bottom = AnimationUtils.loadAnimation(this,R.anim.landscape_to_bottom_anim);
    }
    public void onfabClicked(){
        assignAnimations();
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }
    public void setVisibility(boolean clicked){
        if(clicked){
            add_image_button.setVisibility(View.INVISIBLE);
            search_location_button.setVisibility(View.INVISIBLE);

        }else{
            add_image_button.setVisibility(View.VISIBLE);
            search_location_button.setVisibility(View.VISIBLE);

        }

    }
    public void setAnimation(boolean clicked){
        if(clicked){
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                add_image_button.setAnimation(landscape_to_bottom);
                search_location_button.setAnimation(toBottom);
            }else {
                add_image_button.setAnimation(landscape_to_bottom);
                search_location_button.setAnimation(toBottom);
            }
            fab.setAnimation(rotateClose);

        }else{
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                add_image_button.setAnimation(landscape_from_bottom);
                search_location_button.setAnimation(fromBottom);
            }else{
                add_image_button.setAnimation(landscape_from_bottom);
                search_location_button.setAnimation(fromBottom);
            }
            fab.setAnimation(rotateOpen);

        }
    }

    @Override
    public void handleDialogClose(DialogInterface dialog){
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}