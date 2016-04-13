package mah.sys.locator;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import java.util.Observable;
import java.util.Observer;
import mah.sys.locator.fragments.BuildingFragment;
import mah.sys.locator.fragments.LoadingFragment;

public class MapActivity extends FragmentActivity implements Observer, BuildingFragment.BuildingFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
        overheadMap,
        floorMap;
    private int
        goalFloor,
        maxFloor;
    private ServerCommunicator server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Log.w("Test", "Activity Started");

        server = new ServerCommunicator();

        // Få variabler från Intent.
        Intent intent = getIntent();
        final boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true);
        final String searchTerm = intent.getStringExtra("searchTerm");

        // Få variabler från sharedPref
        SharedPreferences settings = getSharedPreferences("mypref", 0);
        final String chosenComplex = settings.getString("chosenComplex", null);

        ObservableRunnable<Object[]> runnable = new ObservableRunnable<Object[]>() {
            @Override
            public void run() {
                if(isRoomSearch) {
                    data = server.searchRoom(searchTerm,chosenComplex);
                    setChanged();
                    notifyObservers();
                    Log.w("Test", "Observers Notified MapActiviy");
                }
            }
        };

        if(savedInstanceState != null)
            return;

        runnable.addObserver(this);
        new Thread(runnable).start();
        LoadingFragment startFragment = new LoadingFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_map,startFragment).commit();
        Log.w("Test", "Fragment Started");
    }

    private Bitmap getBitmap(byte[] bytes) {
        // Avkoda bytearrayen till en bild.
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.w("BitmapTest", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    @Override
    public Bitmap fetchOverheadMap() {
        return overheadMap;
    }

    public Bitmap getFloorMap() {
        return floorMap;
    }

    public int getGoalFloor() {
        return goalFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.w("Test", "Activity Noted");
        Object[] objects = ((ObservableRunnable<Object[]>)observable).getData();
/*
        overheadMap = getBitmap((byte[])objects[0]);
        floorMap = getBitmap((byte[])objects[1]);
        goalFloor = (int)objects[2];
        maxFloor = (int)objects[3];
*/
        BuildingFragment newFragment = new BuildingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_map,newFragment).commit();
        Log.w("Test", "Building Fragment Started");
    }
}
