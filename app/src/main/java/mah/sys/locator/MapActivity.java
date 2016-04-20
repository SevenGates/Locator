package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LevelFragment;
import mah.sys.locator.fragments.LoadingFragment;
import mah.sys.locator.fragments.RoomFragment;

public class MapActivity extends FragmentActivity implements  View.OnClickListener, Observer, BuildingFragment.BuildingFragmentCommunicator, RoomFragment.RoomFragmentCommunicator, LevelFragment.LevelFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
            overheadMap,
            floorMap;
    private int
            goalFloor,
            maxFloor;
    private double
            roomCoords,
            doorCoords,
            corridorCoords;

    private String
            buildingName,
            roomName;

    private ServerCommunicator server;

    private Button
            btnGoBack,
            btnGoForward;
    private TextView
            txtTopGuide,
            txtBottomGuide;

    private final int MIN_SWIPE_DISTANCE = 200;
    private float touchX1, touchX2;

    private boolean loading = true;

    private final Fragment[] FRAGMENTS = { new BuildingFragment(), new LevelFragment(), new RoomFragment()};
    private int currentFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Log.w("Test", "Activity Started");
        if (savedInstanceState != null)
            return;

        // Index börjar på 0.
        currentFragmentIndex = 0;

        // Skapa serverkommunikation.
        server = new ServerCommunicator();

        // Hämta views.
        btnGoBack = (Button) findViewById(R.id.btnGuideBack);
        btnGoForward = (Button) findViewById(R.id.btnGuideForward);
        txtTopGuide = (TextView) findViewById(R.id.txtGuideStep);
        txtBottomGuide = (TextView) findViewById(R.id.txtGuideDesc);

        // Sätt Listeners.
        btnGoBack.setOnClickListener(this);
        btnGoForward.setOnClickListener(this);

        // Få variabler från Intent.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref.
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        // Gör sökning.
        ObservableRunnable<HashMap<String,String>> runnable = new ObservableRunnable<HashMap<String,String>>() {
            @Override
            public void run() {
                if (isRoomSearch) {
                    try {
                        data = server.searchRoom(searchTerm, chosenComplex);
                        setChanged();
                        notifyObservers();
                        Log.w("Test", "Observers Notified MapActiviy");
                    } catch (IOException e) {
                        Log.w("Test", "Connection Error!");
                        // TODO: Error msg, måste fixas i activity, inte från tråden.
                    }
                }
            }
        };

        runnable.addObserver(this);
        new Thread(runnable).start();

        // Stäng av frammåtknappen under laddningen.
        btnGoBack.setEnabled(true);
        btnGoForward.setEnabled(false);

        // Starta laddningsfragment.
        LoadingFragment startFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_map, startFragment).commit();
        Log.w("Test", "Fragment Started");
    }

    private Bitmap getBitmap(byte[] bytes) {
        // Avkoda bytearrayen till en bild.
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.w("BitmapTest", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    @Override
    public Bitmap getOverheadMap() {
        return overheadMap;
    }

    @Override
    public String getBuildingName() {
        return buildingName;
    }

    @Override
    public void setIntructions(String top, String bottom) {
        txtTopGuide.setText(top);
        txtBottomGuide.setText(bottom);
    }

    public Bitmap getFloorMap() {
        return floorMap;
    }

    @Override
    public double getRoomCoords() {
        return roomCoords;
    }

    @Override
    public double getDoorCoords() {
        return doorCoords;
    }

    @Override
    public double getCorridorCoords() {
        return corridorCoords;
    }

    @Override
    public int getMaxFloors() {
        return maxFloor;
    }

    @Override
    public int getGoalFloor() {
        return goalFloor;
    }

    @Override
    public void onClick(View v) {
        if (v == btnGoBack) {
            if(currentFragmentIndex <= 0) {
                startActivity(new Intent(this, SearchActivity.class));
            }
            else {
                switchFragment(--currentFragmentIndex);
                updateButtonText();
            }
        }
        else if (v == btnGoForward) {
            if(currentFragmentIndex >= FRAGMENTS.length - 1) {
                startActivity(new Intent(this, SearchActivity.class));
            }
            else {
                switchFragment(++currentFragmentIndex);
                updateButtonText();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX1 = event.getX();
                Log.w("Test", "Down");
                break;
            case MotionEvent.ACTION_UP:
                touchX2 = event.getX();
                Log.w("Test", "Up");
                float delta = touchX2 - touchX1;
                if(Math.abs(delta) > MIN_SWIPE_DISTANCE) {
                    if(loading)
                        break;
                    if(touchX2 > touchX1 && currentFragmentIndex > 0) {
                        switchFragment(--currentFragmentIndex);
                        updateButtonText();
                    }
                    else if(touchX1 > touchX2 && currentFragmentIndex < FRAGMENTS.length - 1) {
                        switchFragment(++currentFragmentIndex);
                        updateButtonText();
                    }
                }
            break;
        }
        return super.onTouchEvent(event);
    }

    private void updateButtonText() {
        String
            back = getResources().getString(R.string.arrowLeft),
            forward = getResources().getString(R.string.arrowRight),
            cancel = getResources().getString(R.string.btnText_cancel),
            finish = getResources().getString(R.string.btnText_finish);

        // Backbutton update
        if(currentFragmentIndex == 0)
            btnGoBack.setText(cancel);
        else
            btnGoBack.setText(back);

        // Forwardbutton update
        if(currentFragmentIndex == FRAGMENTS.length - 1)
            btnGoForward.setText(finish);
        else
            btnGoForward.setText(forward);
    }

    private void switchFragment(int newFragmentIndex) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_map, FRAGMENTS[newFragmentIndex]).commit();
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.w("Test", "Activity Noted");
        HashMap<String,String> objects = ((ObservableRunnable<HashMap<String,String>>) observable).getData();
        // Hämta alla variabler från Hashmapen.
        goalFloor = Integer.valueOf(objects.get("GoalFloor").replaceAll("\\D+", ""));
        maxFloor = Integer.valueOf(objects.get("MaxFloors"));
        roomCoords = Double.valueOf(objects.get("RoomCoor"));
        doorCoords = Double.valueOf(objects.get("DoorCoor"));
        corridorCoords = Double.valueOf(objects.get("CorridorCoor"));
        buildingName = objects.get("Name");
        roomName = objects.get("roomId");

        // Hämta båda bilder och decodea dem till Bitmaps
        byte[] overheadBytes = Base64.decode(objects.get("Overhead"),Base64.DEFAULT);
        overheadMap = getBitmap(overheadBytes);

        byte[] floorMaps = Base64.decode(objects.get("FloorMap"), Base64.DEFAULT);
        floorMap = getBitmap(floorMaps);
        // Starta den första fragmenten.
        switchFragment(currentFragmentIndex);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGoForward.setEnabled(true);
            }
        });
        loading = false;
        Log.w("Test", "Building Fragment Started");
    }
}
