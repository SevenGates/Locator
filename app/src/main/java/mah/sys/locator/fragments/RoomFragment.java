package mah.sys.locator.fragments;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;

import mah.sys.locator.FragmentCommunicator;
import mah.sys.locator.R;
import mah.sys.locator.ZoomableImageView;

/**
 * Created by Alex on 14-Apr-16.
 */
public class RoomFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    // Views
    private ZoomableImageView imgViewRoomMap;
    private Spinner sprPathSelector;

    // Callback
    private RoomFragmentCommunicator callback;

    // Variabler för vägvisning.
    private Bitmap floorMap;
    private int
            roomX,
            roomY,
            doorX,
            doorY,
            corridorX,
            corridorY;
    private int[][][] path;

    // Bild
    private Canvas canvas;
    private Bitmap image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room, container, false);

        // Hämta callback.
        try {
            callback = (RoomFragmentCommunicator) getActivity();
        } catch (ClassCastException e) {
            Log.w("Test", getActivity().toString() + " måste ärva RoomFragmentCommunicator");
        }

        // Hämta variabler.
        floorMap = callback.getFloorMap();
        roomX = callback.getRoomX();
        roomY = callback.getRoomY();
        doorX = callback.getDoorX();
        doorY = callback.getDoorY();
        corridorX = callback.getCorridorX();
        corridorY = callback.getCorridorY();
        path = callback.getPath();

        // Hitta view.
        imgViewRoomMap = (ZoomableImageView) v.findViewById(R.id.imgViewRoomMap);
        sprPathSelector = (Spinner)v.findViewById(R.id.sprPathSelector);

        ArrayList<String> items = new ArrayList<>();
        items.add("Väg 1");
        items.add("Väg 2");
        items.add("Väg 3");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sprPathSelector.setAdapter(spinnerArrayAdapter);
        sprPathSelector.setOnItemSelectedListener(this);

        // Rita linje och märk ut sal på kartan.
        drawLineToRoom(0);

        // Sätt instruktioner.
        String
                topText = getResources().getString(R.string.guide_room_top),
                bottomText = getResources().getString(R.string.guide_room_bottom) + " " + callback.getRoomName();
        callback.setInstructions(topText,bottomText);

        return v;
    }

    /**
     * Ritar ett sträck mellan startnoden och salen via alla checkpoints.
     * Märker även ut salern med en cirkel.
     */
    private void drawLineToRoom(int pathNr) {
        image = floorMap.copy(floorMap.getConfig(),true);
        canvas = new Canvas(image);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#BF360C"));
        paint.setStrokeWidth(5);

        for(int i = 0; i < path[pathNr].length-1; i++)
            canvas.drawLine(path[pathNr][i][0],path[pathNr][i][1],path[pathNr][i+1][0],path[pathNr][i+1][1], paint);
        canvas.drawLine(path[pathNr][path[pathNr].length-1][0],path[pathNr][path[pathNr].length-1][1],corridorX,corridorY, paint);
        canvas.drawLine(corridorX, corridorY, doorX, doorY, paint);
        canvas.drawLine(doorX, doorY, roomX, roomY, paint);

        canvas.drawCircle(roomX, roomY, 12, paint);
        imgViewRoomMap.setImageBitmap(image);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.w("Test","Redrawing" + position + " " + id);
        drawLineToRoom(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.w("Test","Nothing");
    }

    /**
     * Kommunikation med activity.
     */
    public interface RoomFragmentCommunicator extends FragmentCommunicator {
        Bitmap getFloorMap();
        String getRoomName();
        int getRoomX();
        int getRoomY();
        int getDoorX();
        int getDoorY();
        int getCorridorX();
        int getCorridorY();
        int[][][] getPath();
    }
}
