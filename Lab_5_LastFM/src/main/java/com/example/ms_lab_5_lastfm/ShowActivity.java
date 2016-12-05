package com.example.ms_lab_5_lastfm;

import android.content.Intent;
import android.os.Bundle;
import android.app.ListActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ShowActivity extends ListActivity {

    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ArrayList<String> values = intent.getStringArrayListExtra("values");
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        setListAdapter(mAdapter);
    }
}
