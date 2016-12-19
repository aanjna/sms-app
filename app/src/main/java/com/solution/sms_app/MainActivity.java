package com.solution.sms_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private SMSAdapter smsAdapter;
    private ArrayList<SMS> smsDataList;
    private Cursor c;
    private UploadToDrive uploadSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.messages_list);
        smsDataList = new ArrayList<SMS>();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SendSMSActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onResume(){
        super.onResume();
        final ArrayList<SMS> sms_list, smslist_group;
        sms_list = new ArrayList<>();
        smslist_group = new ArrayList<>();

        int REQUEST_CODE_ASK_PERMISSIONS = 123;
        try{
            //Permission for API >=25
            if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {}
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);}

            c = getContentResolver().query(Uri.parse("content://sms/inbox"),new String[]{"_id", "address", "date", "body"},null,null,null);
            StringBuilder sb = new StringBuilder();

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    String address = c.getString(c.getColumnIndexOrThrow("address"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));
                    String body = c.getString(c.getColumnIndexOrThrow("body"));
                    sb.append(address).append("\n");
                    sb.append(body).append("\n");
                    sb.append(date).append("\n");
                    sb.append("\n");
                    sms_list.add(new SMS(address, date, body));
                }
                c.close();
            }

            smsDataList= sms_list;
            //MAP to store the details of all the messages associated with one sender
            Map<String, SMS> map = new LinkedHashMap<>();

            for (SMS message : sms_list) {

                SMS existingValue = map.get(message.address);
                if(existingValue == null){
                    map.put(message.address, message);
                }
            }

            smslist_group.clear();
            smslist_group.addAll(map.values());

            smsAdapter= new SMSAdapter(smslist_group);
            recyclerView.setAdapter(smsAdapter);

            recyclerView.addOnItemTouchListener(
                    new ItemOnClickListener(getApplicationContext(), new ItemOnClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            ArrayList<SMS> smslist_inside = new ArrayList<SMS>();
                            String n = smslist_group.get(position).address;

                            for (int i = 0; i < sms_list.size(); i++) {
                                if(sms_list.get(i).address.equals(n))
                                    smslist_inside.add(sms_list.get(i));
                            }

                            Intent i = new Intent(MainActivity.this, ReadSMSActivity.class);
                            i.putParcelableArrayListExtra("messages", smslist_inside);
                            startActivity(i);
                        }
                    })
            );


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    class UploadToDrive extends AsyncTask<Void, Integer, Uri> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Exporting to file ...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgress(0);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Uri doInBackground(Void... params) {
            FileOutputStream fos = null;
            try {
                String file_name ="mysms.txt";
                File file = new File(getFilesDir(),file_name);
                if (!file.exists())
                    file.createNewFile();
                //Storing the file inside the internal storage of the device since all device may no have external storage
                fos = openFileOutput(file_name, Context.MODE_PRIVATE);
                c = getContentResolver().query(Uri.parse("content://sms/inbox"),null,null,null,null);
                int count = c.getCount(), i = 0;

                StringBuilder sb = new StringBuilder();
                if (c.moveToFirst()) {
                    do {
                        sb.append(c.getString(c.getColumnIndex("address")))
                                .append("\n");
                        sb.append(c.getString(c.getColumnIndex("body")))
                                .append("\n");
                        sb.append(c.getString(c.getColumnIndex("date")))
                                .append("\n");
                        sb.append("\n");
                        publishProgress(++i*100/count);
                    } while (!isCancelled() && c.moveToNext());
                }
                fos.write(sb.toString().getBytes());
                return Uri.fromFile(file);
            }
            catch (Exception e) {
            }
            finally {
                if (fos != null) {
                    try {
                        fos.close();
                    }
                    catch (IOException e) {}
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Uri result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result == null) {
                Toast.makeText(MainActivity.this, "Export to file failed! File may not be accessibe.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(MainActivity.this, UploadOnDrive.class);
            startActivity(i);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent i = new Intent(MainActivity.this,SearchSMSActivity.class);
            i.putParcelableArrayListExtra("search", smsDataList);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_upload_to_drive) {
            uploadSMS = new UploadToDrive();
            uploadSMS.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (uploadSMS != null) {
            uploadSMS.cancel(false);
            uploadSMS.pDialog.dismiss();
        }
        super.onPause();
    }

}
