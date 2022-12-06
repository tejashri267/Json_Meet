package com.example.jsonmeet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jsonmeet.R;
import com.example.jsonmeet.listners.userListener;
import com.example.jsonmeet.models.User;

import java.util.List;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userviewholer> {

    private List<User> users;
    private userListener Listener;

    public userAdapter(List<User> users, userListener listener) {
        this.users = users;

        this.Listener = listener;
    }

    @NonNull
    @Override
    public userviewholer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user, parent, false);
        return new userviewholer(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userviewholer holder, int position) {
          holder.userSetData(users.get(position));
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

//    public void setUsers(List<User> users) {
//        this.users = users;
//    }

    class userviewholer extends RecyclerView.ViewHolder {
        TextView textFirstchar, textUsername, textemail;
        ImageView imgvideo, imgaudio;

        userviewholer(@NonNull View itemView) {
            super(itemView);
            textFirstchar=itemView.findViewById(R.id.textchar1);
            textUsername=itemView.findViewById(R.id.username);
            textemail=itemView.findViewById(R.id.textemail);
            imgvideo=itemView.findViewById(R.id.imgvideo);
            imgaudio=itemView.findViewById(R.id.imgcall);
        }
        void userSetData(User user) {
            textFirstchar.setText(user.firstName.substring(0, 1));
            textUsername.setText(String.format("%s %s",user.firstName,user.lastName));
            textemail.setText(user.email);
            imgaudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Listener.initialAudioMeet(user);
                }
            });
            imgvideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Listener.initialVideoMeet(user);
                }
            });
        }

    }
}
