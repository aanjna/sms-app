package com.solution.sms_app;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class SendSMSActivity extends AppCompatActivity {

    ImageButton imageButton;
    Button buttonSend;
    AutoCompleteTextView textPhoneNo;
    EditText textSMS;
    private static final int PICK_CONTACT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSend = (Button) findViewById(R.id.button);
        textPhoneNo = (AutoCompleteTextView) findViewById(R.id.phone);
        textSMS = (EditText) findViewById(R.id.message);
        imageButton = (ImageButton) findViewById(R.id.ib_contact);

        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String phoneNo = textPhoneNo.getText().toString();
                String sms = textSMS.getText().toString();

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                    Toast.makeText(getApplicationContext(), "Sent!",
                            Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);

                startActivityForResult(intent, PICK_CONTACT);
            }
        });
    }

}
