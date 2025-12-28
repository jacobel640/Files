package com.example.files.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import com.example.files.MainActivity;
import com.example.files.R;
import com.example.files.actions.DialogCopy;
import com.example.files.models.JFile;

import java.io.File;

import static com.example.files.Statics.selectedJFiles;

public class SendAction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_send_action);

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction != null) {
            Intent send = new Intent(this, MainActivity.class);
            Object receivedUri = receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (receivedUri == null) {
                finish();
                return;
            }
            String uri = receivedUri.toString();

            if (receivedAction.equals(Intent.ACTION_SEND)) {
                send.putExtra("send", uri);
                startActivity(send);
            }
            if (receivedAction.equals(Intent.ACTION_SEND_MULTIPLE)) {
                send.putExtra("send_multiple", uri);
                startActivity(send);
            }
        }

        finish();

        //if (receivedIntent.getComponent().getPackageName().equals("com.example.files")) onBackPressed();
    }
}