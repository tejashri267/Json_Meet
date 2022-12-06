package com.example.jsonmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsonmeet.R;
import com.example.jsonmeet.network.ApiClient;
import com.example.jsonmeet.network.ApiService;
import com.example.jsonmeet.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomigInvitation extends AppCompatActivity {

    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomig_invitation);
        ImageView imageMeetType = findViewById(R.id.meetingtype);
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if(meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetType.setImageResource(R.drawable.ic_video);
            }else {
                imageMeetType.setImageResource(R.drawable.ic_call);
            }
        }
        TextView textfirst = findViewById(R.id.firstChar);
        TextView username = findViewById(R.id.textUserName);
        TextView textEmail = findViewById(R.id.TextEmail);

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName != null) {
            textfirst.setText(firstName.substring(0, 1));
        }
        username.setText(String.format(
                "%s %s",
                firstName,
                getIntent().getStringExtra(Constants.KEY_LAST_NAME)
        ));
        textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imageView1 = findViewById(R.id.accept);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitation(Constants.REMOTE_INVITATION_ACCEPTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });
        ImageView imageView2 = findViewById(R.id.reject);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitation(Constants.REMOTE_INVITATION_REJECTED, getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });

    }

    private void sendInvitation(String type, String receiverToken) {
        try {

            JSONArray tokens =  new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);


        }catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeader(),
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                  if (type.equals(Constants.REMOTE_INVITATION_ACCEPTED)) {
                    //  Toast.makeText(IncomigInvitation.this, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                      try {
                          URL serverURL = new URL("https://meet.jit.si");
                          JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                          builder.setServerURL(serverURL);
                          builder.setWelcomePageEnabled(false);
                          builder.setRoom(getIntent().getStringExtra(Constants.MEETING_ROOM));
                          if (meetingType.equals("audio")) {
                              builder.setVideoMuted(true);
                          }
                          JitsiMeetActivity.launch(IncomigInvitation.this, builder.build());
                          finish();
                      }catch (Exception e) {
                          Toast.makeText(IncomigInvitation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                          finish();
                      }
                  }else {
                      Toast.makeText(IncomigInvitation.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                      finish();
                  }
                }else {
                    Toast.makeText(IncomigInvitation.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(IncomigInvitation.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}