package com.solution.sms_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;

public class SearchSMSActivity extends AppCompatActivity {

    EditText et;
    RecyclerView rv;
    SMSAdapter adapter;

    String selectedText = "";
    ArrayList<SMS> smsSearchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        smsSearchList = new ArrayList<SMS>();

        et = (EditText) findViewById(R.id.search_edt);

        rv = (RecyclerView) findViewById(R.id.search_recycler);

        Intent i = this.getIntent();
        smsSearchList = i.getParcelableArrayListExtra("search");

        LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
        rv.setLayoutManager(llm);


        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                selectedText = et.getText().toString();
                updateListOfSms();
            }
        });
    }

    public void updateListOfSms() {

        if (selectedText != null && !selectedText.equals("")) {

            ArrayList<SMS> searchList = new ArrayList<>();

            for (int i = 0; i < smsSearchList.size(); i++) {


                if(smsSearchList.get(i).address.toLowerCase().contains(selectedText.toLowerCase())) {
                    if (!searchList.contains(smsSearchList.get(i)))
                        searchList.add(smsSearchList.get(i));
                }
                if(smsSearchList.get(i).date.toLowerCase().contains(selectedText.toLowerCase())) {
                    if (!searchList.contains(smsSearchList.get(i)))
                        searchList.add(smsSearchList.get(i));
                }
                if(smsSearchList.get(i).body.toLowerCase().contains(selectedText.toLowerCase())) {
                    if (!searchList.contains(smsSearchList.get(i)))
                        searchList.add(smsSearchList.get(i));
                }


                adapter = new SMSAdapter(searchList);
                rv.setAdapter(adapter);

            }
        }
    }
}
