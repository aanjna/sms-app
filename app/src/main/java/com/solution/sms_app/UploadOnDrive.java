package com.solution.sms_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UploadOnDrive extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "upload_file";
    private static final int REQUEST_CODE = 101;
    private File textFile;
    private GoogleApiClient googleApiClient;
    public static String drive_id;
    public static DriveId driveID;

    FrameLayout rl;
    TextView success;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);


        rl = (FrameLayout) findViewById(R.id.frame);
        success = (TextView) findViewById(R.id.success);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        View root = rl.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.background));
        String fileName ="SmsList.txt";
        textFile = new File(getFilesDir(),fileName);

        buildGoogleApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "In onStart() - connecting...");
        googleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            Log.i(TAG, "In onStop() - disConnecting...");
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "In onActivityResult() - connecting...");
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "in onConnected() - we're connected, let's do the work in the background");
        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        switch (cause) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();
                    new Thread() {
                        @Override
                        public void run() {
                            OutputStream outputStream = driveContents.getOutputStream();
                            addTextfileToOutputStream(outputStream);
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("SmsBackup")
                                    .setMimeType("text/plain")
                                    .setDescription("The file contains the backup of all the sms until now")
                                    .setStarred(true).build();
                            Drive.DriveApi.getRootFolder(googleApiClient)
                                    .createFile(googleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };

    private void addTextfileToOutputStream(OutputStream outputStream) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(textFile));
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();

                }
            });
            e.printStackTrace();
        }
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(UploadOnDrive.this,
                                "Error adding file to Drive", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showProgress(false);

                    rl.setBackgroundColor(getResources().getColor(R.color.background));
                    View root = rl.getRootView();
                    root.setBackgroundColor(getResources().getColor(R.color.background_text));

                    success.setVisibility(View.VISIBLE);

                    final PendingResult<DriveResource.MetadataResult> metadata
                            = result.getDriveFile().getMetadata(googleApiClient);
                    metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                                           @Override
                                                           public void onResult(DriveResource.MetadataResult metadataResult) {
                                                               Metadata data = metadataResult.getMetadata();
                                                               drive_id = data.getDriveId().encodeToString();
                                                               driveID = data.getDriveId();

                                                           }
                                                       });
                }
            };

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
        }
    }

    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}



