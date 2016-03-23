package mah.sys.locator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MapActivity extends AppCompatActivity {

    // Skapa variabler
    private ImageView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Tildela views efter ID.
        mapView = (ImageView) findViewById(R.id.imageViewMap);

        // Hämta Intent och tildela sök/typ argumenten.
        Intent intent = getIntent();
        boolean isRoomSearch = intent.getBooleanExtra("isRoomSearch", true); // TODO: UNUSED
        String searchTerm = intent.getStringExtra("searchTerm"); // TODO: UNUSED

        Log.w("Test", "Activity Started");

        // Ladda karta
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

                    // Kör på UI-tråden för att uppdatera imageView.
                    runOnUiThread(new Runnable() {
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
