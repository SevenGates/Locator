package mah.sys.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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

public class MapActivity extends AppCompatActivity implements  View.OnClickListener, Observer, BuildingFragment.BuildingFragmentCommunicator, RoomFragment.RoomFragmentCommunicator, LevelFragment.LevelFragmentCommunicator {

    // Variabler från sökning.
    private Bitmap
            overheadMap,
            floorMap;
    private int
            goalFloor,
            maxFloor,
            roomX,
            roomY,
            doorX,
            doorY,
            corridorX,
            corridorY;

    private int[][] path;

    private String
            buildingName,
            roomName;

    private ServerCommunicator server;

    private AppCompatButton
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

        // Ladda förra instance om den finns.
        if (savedInstanceState != null)
            return;

        // Index börjar på 0.
        currentFragmentIndex = 0;

        // Skapa serverkommunikation.
        server = new ServerCommunicator();

        // Hämta views.
        btnGoBack = (AppCompatButton)findViewById(R.id.btnGuideBack);
        btnGoForward = (AppCompatButton)findViewById(R.id.btnGuideForward);
        txtTopGuide = (TextView) findViewById(R.id.txtGuideStep);
        txtBottomGuide = (TextView) findViewById(R.id.txtGuideDesc);

        // Sätt Listeners.
        btnGoBack.setOnClickListener(this);
        btnGoForward.setOnClickListener(this);

        // Färga knappar TODO: Detta är inte snyggt, fixa detta?
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.buttonColor)});
        btnGoBack.setSupportBackgroundTintList(csl);
        btnGoForward.setSupportBackgroundTintList(csl);

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
    public int getRoomX() {
        return roomX;
    }

    @Override
    public int getRoomY() {
        return roomY;
    }

    @Override
    public int getDoorX() {
        return doorX;
    }

    @Override
    public int getDoorY() {
        return doorY;
    }

    @Override
    public int getCorridorX() {
        return corridorX;
    }

    @Override
    public int getCorridorY() {
        return corridorY;
    }

    @Override
    public int getMaxFloors() {
        return maxFloor;
    }

    @Override
    public int getGoalFloor() {
        return goalFloor;
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

    private void markRoomOnBitMap() {
        Bitmap image = floorMap.copy(floorMap.getConfig(),true);
        Canvas canvas = new Canvas(image);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#BF360C"));

        canvas.drawCircle(roomX - 5, roomY - 5, 10, paint);
        floorMap = image;

        /*Bitmap image = floorMap.copy(floorMap.getConfig(),true);
        int[] pixels = new int[image.getHeight() * image.getWidth()];

        int width = image.getWidth(), height = image.getHeight();
        image.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i=(roomY-10)*width; i<width*roomY+10*width; i+=width)
            for(int j = roomX-10; j<roomX+10; j++)
                pixels[i+j] = Color.parseColor("#BF360C");
        image.setPixels(pixels, 0, width, 0, 0, width, height);
        floorMap = image;*/
    }

    private void drawLineToRoom() {
        Canvas canvas = new Canvas(floorMap);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#BF360C"));

        canvas.drawLine(0,0,400,400, paint);
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

    @Override
    public void update(Observable observable, Object data) {
        Log.w("Test", "Activity Noted");
        HashMap<String,String> objects = ((ObservableRunnable<HashMap<String,String>>) observable).getData();
        // Hämta alla variabler från Hashmapen.
        goalFloor = Integer.valueOf(objects.get("GoalFloor").replaceAll("\\D+", ""));
        maxFloor = Integer.valueOf(objects.get("MaxFloors"));
        buildingName = objects.get("Name");
        roomName = objects.get("roomId");

        // Koordinater
        String
            roomCoords = objects.get("RoomCoor"),
            doorCoords = objects.get("DoorCoor"),
            corridorCoors = objects.get("CorridorCoor");
        roomX = Integer.parseInt(roomCoords.split("\\.")[0]);
        roomY = Integer.parseInt(roomCoords.split("\\.")[1]);
        doorX = Integer.parseInt(doorCoords.split("\\.")[0]);
        doorY = Integer.parseInt(doorCoords.split("\\.")[1]);
        corridorX = Integer.parseInt(corridorCoors.split("\\.")[0]);
        corridorY = Integer.parseInt(corridorCoors.split("\\.")[1]);

        int nbrOfNodes = Integer.parseInt(objects.get("nbrOfNodes"));
        Log.w("Test", Integer.toString(nbrOfNodes));
        path = new int[nbrOfNodes][2];
        String node;
        for(int i = 1; i < nbrOfNodes+1; i++){
            node = objects.get("node"+i);
            Log.w("Test", node.split("\\.")[0]);
            Log.w("Test", node.split("\\.")[1]);
            Log.w("Test", Integer.toString(path[i-1][0]));
            Log.w("Test", Integer.toString(path[i-1][1]));
        }

        for(int i = 0; i < path.length; i++)
            System.out.println(path[i][0] + ", " + path[i][1]);

        // Hämta båda bilder och decodea dem till Bitmaps
        byte[] overheadBytes = Base64.decode(objects.get("Overhead"),Base64.DEFAULT);
        overheadMap = getBitmap(overheadBytes);

        byte[] floorMaps = Base64.decode(objects.get("FloorMap"), Base64.DEFAULT);
        floorMap = getBitmap(floorMaps);

        markRoomOnBitMap();
        drawLineToRoom();

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
