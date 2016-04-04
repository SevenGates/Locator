package mah.sys.locator.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import mah.sys.locator.MapActivity;
import mah.sys.locator.R;

/**
 * Created by Alex on 04-Apr-16.
 */
public class BuildingFragment extends Fragment {

    // Skapa variabler
    private ImageView mapView;
    private Activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("Test", "View Infalated");
        return inflater.inflate(R.layout.building_layout,container,false);
    }

    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.w("Test", "ViewStateRestored");
        mapView = (ImageView)getView().findViewById(R.id.imageViewMap);
        activity = getActivity();
        loadImage();
    }

    private void loadImage() {
        // Skapa ny tråd då nätverks operationer inte kan användas på UI-tråden.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Skapa socket.
                    Socket socket = new Socket("192.168.1.164", 8080);

                    // Sätt strömmarna.
                    DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    InputStream input = socket.getInputStream();

                    // Skicka servern ett meddelande. TODO: Här ska ett kommando.
                    output.writeUTF("Test from Android");
                    output.flush();

                    Log.w("Test", "Message Sent");

                    // Skapa en buffert att läsa in bytes i. Med hjälpvariabler.
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int read;
                    byte[] data = new byte[16384];

                    // Läs från inputströmmen och lägg i bufferten.
                    while ((read = input.read(data, 0, data.length)) != -1)
                        buffer.write(data, 0, read);
                    buffer.flush();

                    // Avkoda bytearrayen till en bild.
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(buffer.toByteArray(), 0, buffer.toByteArray().length);
                    Log.w("Test", String.valueOf(bitmap.getWidth()));
                    // Kör på UI-tråden för att uppdatera imageView.
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeImage(bitmap);
                        }
                    });

                    // Stäng socket.
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Ändra bild på imageView
     * @param map Ny bild.
     */
    private void changeImage(Bitmap map) {
        mapView.setImageBitmap(map);
    }
}
