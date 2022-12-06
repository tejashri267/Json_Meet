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
import com.example.jsonmeet.models.User;
import com.example.jsonmeet.network.ApiClient;
import com.example.jsonmeet.network.ApiService;
import com.example.jsonmeet.utilities.Constants;
import com.example.jsonmeet.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutGoingInvitation extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private String inviterToken = null;
    String meetingRoom = null;
    private String meetType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_going_invitation);

        preferenceManager = new PreferenceManager(getApplicationContext());

        ImageView imgtype = findViewById(R.id.meetingtype);
        meetType = getIntent().getStringExtra("type");

        if (meetType != null) {
            if (meetType.equals("video")) {
                imgtype.setImageResource(R.drawable.ic_video);
            }else {
                imgtype.setImageResource(R.drawable.ic_call);
            }
        }

        TextView firstChar = findViewById(R.id.firstChar);
        TextView userNametxt = findViewById(R.id.textUserName);
        TextView textEmail = findViewById(R.id.TextEmail);

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            firstChar.setText(user.firstName.substring(0, 1));
            userNametxt.setText(String.format("%s %s", user.firstName, user.lastName));
            textEmail.setText(user.email);
        }

        ImageView imgCancel = findViewById(R.id.stopInvitation);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (user != null) {
                   cancelInvitation(user.token);
               }
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    inviterToken = task.getResult().getToken();
                    if (meetType != null && user != null) {
                        initiateMeeting(meetType, user.token);
                    }
                }
            }
        });


    }
    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_INVITATION_);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, preferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, preferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom = preferenceManager.getString(Constants.KEY_ID) + "_" +
                    UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_INVITATION_);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeader(),
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_INVITATION_)) {
                        Toast.makeText(OutGoingInvitation.this, "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();
                    }else if (type.equals(Constants.REMOTE_INVITATION_RESPONSE)) {
                        Toast.makeText(OutGoingInvitation.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(OutGoingInvitation.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(OutGoingInvitation.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken) {
        try {

            JSONArray tokens =  new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_INVITATION_RESPONSE, Constants.REMOTE_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_INVITATION_RESPONSE);


        }catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_INVITATION_RESPONSE);
            if (type != null) {
               if (type.equals(Constants.REMOTE_INVITATION_ACCEPTED)) {
                 //  Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                   try {
                       URL serverURL = new URL("https://meet.jit.si");
                       JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                       builder.setServerURL(serverURL);
                       builder.setWelcomePageEnabled(false);
                       builder.setRoom(meetingRoom);
                       if (meetType.equals("audio")) {
                           builder.setVideoMuted(true);
                       }

                       JitsiMeetActivity.launch(OutGoingInvitation.this, builder.build());
                       finish();
                   }catch (Exception e) {
                       Toast.makeText(OutGoingInvitation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                       finish();
                   }

               }else  if (type.equals(Constants.REMOTE_INVITATION_REJECTED)) {
                   Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
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