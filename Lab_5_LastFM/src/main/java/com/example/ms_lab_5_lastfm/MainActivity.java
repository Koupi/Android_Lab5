package com.example.ms_lab_5_lastfm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    EditText artistName;
    Context context;
    DBHelper dbHelper;

    private String dbName = "dbName";
    private String tableName = "artist";
    private String artistNameColumn = "artist_name";
    private String songName = "track_name";
    private String listeners = "listeners";
    private String playcount = "playcount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        dbHelper = new DBHelper(this);

        artistName = (EditText)findViewById(R.id.editText_artist);
    }

    public static boolean isConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void onClickLoad(View v) {
        String findName = artistName.getText().toString();
        if(findName.equals("")) {
            return;
        }

        if(!isConnected(context)){
            Toast.makeText(context, "No connection", Toast.LENGTH_SHORT).show();
        }

        LoadTask mt = new LoadTask();
        String queryString = "http://ws.audioscrobbler.com/2.0/?method="+getString(R.string.method_top)+"&artist="+findName+"&limit=10"+"&api_key="+getString(R.string.api_key);
        mt.execute(queryString);

        try {
            List<Song> list = mt.get();

            if(list.size()==0){
                Toast.makeText(context, "Not found", Toast.LENGTH_SHORT).show();
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(tableName, artistNameColumn+" = ?", new String[]{findName});
                dbHelper.close();

                db = dbHelper.getWritableDatabase();

                for(Song song : list){
                    song.song = song.song.replaceAll("[\\(\\)\'\"]", "");
                    db.execSQL("INSERT INTO "+tableName+" ("+artistNameColumn+", "+playcount+", "+listeners+", "+songName+") VALUES ('"+findName+"', '"+song.playCount+"', '"+song.listeners+"', '"+song.song+"')");
                }
                dbHelper.close();
                Toast.makeText(context, "Data loaded", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    private void showWithQuery(Cursor c) {
        ArrayList<String> values = new ArrayList<>();
        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex(artistNameColumn);
            int trackColIndex = c.getColumnIndex(songName);
            int listenersColIndex = c.getColumnIndex(listeners);
            int playColIndex = c.getColumnIndex(playcount);
            do {
                values.add("Artist: " + c.getString(nameColIndex) +
                        "\nSong: " + c.getString(trackColIndex) +
                        "\nUsers listened = " + c.getString(listenersColIndex) +
                        "\nListen count = " + c.getString(playColIndex)
                );
            } while (c.moveToNext());
        }
        c.close();
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("values", values);
        startActivity(intent);
    }

    public void onClickShowAll(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(tableName, null, null, null, null, null, null);
        showWithQuery(c);
        dbHelper.close();
    }

    public void onClickShow(View v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(tableName, null, artistNameColumn+" = ?", new String[]{artistName.getText().toString()}, null, null, null);
        showWithQuery(c);
        dbHelper.close();
    }

    public class LoadTask extends AsyncTask<String, String, List<Song>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Load started", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Song> result) {
            super.onPostExecute(result);
            Toast.makeText(context, "Finished", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Song> doInBackground(String... strings) {
            List<Song> list = new ArrayList<Song>();
            try {
                URL url = new URL(strings[0]);
                URLConnection conn = url.openConnection();
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(conn.getInputStream());
                doc.getDocumentElement().normalize();
                NodeList error = doc.getElementsByTagName("error");
                if (error.getLength() != 0) {
                    return list;
                }

                NodeList songs = doc.getElementsByTagName("song");
                for (int j = 0; j < songs.getLength(); ++j) {
                    Element condition = (Element) songs.item(j);
                    String nameText = condition.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    String playcount = condition.getElementsByTagName("playcount").item(0).getFirstChild().getNodeValue();
                    String listeners = condition.getElementsByTagName("listeners").item(0).getFirstChild().getNodeValue();
                    Song song = new Song();
                    song.song = nameText;
                    song.playCount = playcount;
                    song.listeners = listeners;
                    list.add(song);
                }
            } catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            return list;
        }
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, dbName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+tableName +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, artist_name TEXT, track_name TEXT, listeners INTEGER, playcount INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

}
