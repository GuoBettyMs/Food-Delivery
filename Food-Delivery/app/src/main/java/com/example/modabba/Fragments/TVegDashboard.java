package com.example.modabba.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.modabba.NotificationService;
import com.example.modabba.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class TVegDashboard extends Fragment {
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private TextView dashboard_lunch;
    private String TAG = TVegDashboard.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView=(ViewGroup)inflater.inflate(R.layout.tvegdashboard,container,false);
        dashboard_lunch=rootView.findViewById(R.id.dashboard_lunch);

        //查询 Cloud Firebase 中 <menu> 集合中的 <lunch> 文档
        DocumentReference lunchRef = db.collection("menu").document("lunch");
        lunchRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        StringBuilder builder = new StringBuilder();
                        NotificationService notificationService = new NotificationService();
                        Map<String,String> data = (Map<String, String>) documentSnapshot.get("lunch");
                        Log.d(TAG,  "TVegDashboard: "+data);

                        assert data != null;
                        Iterator<String> itr  = data.keySet().iterator();

                        while (itr.hasNext()){

                            String key = itr.next();
                            String value = data.get(key);
                            String cap  = key.substring(0, 1).toUpperCase() + key.substring(1);

                            builder.append(" ").append(value);
                            builder.append(" ").append(cap).append(" ");

                            if((itr.hasNext()))
                                builder.append("/");

                        }
                        Log.d(TAG,"builder message "+builder.toString());
                        if(builder.toString().length() > 0 )
                            notificationService.showNotification(Objects.requireNonNull(getActivity()),"The Menu Has Been Updated","Tap to View today's menu");
                        dashboard_lunch.setText(builder+" ");
                        System.out.println(builder);
                    }
                });
        return rootView;
    }
}
