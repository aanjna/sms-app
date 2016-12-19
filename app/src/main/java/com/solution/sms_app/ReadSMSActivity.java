package com.solution.sms_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;


import java.util.ArrayList;

public class ReadSMSActivity extends AppCompatActivity {
    RecyclerView rv;
    SMSAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_all_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = this.getIntent();
        ArrayList<SMS> messageList = i.getParcelableArrayListExtra("messages");

        getSupportActionBar().setTitle(messageList.get(0).address);

        rv = (RecyclerView) findViewById(R.id.single_address_view);

        LinearLayoutManager llm = new LinearLayoutManager(ReadSMSActivity.this);
        rv.setLayoutManager(llm);

        adapter = new SMSAdapter(messageList);

        rv.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
