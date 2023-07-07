package com.example.modabba;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modabba.SessionManagement.SessionManagement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubcriptionHistoryFragment extends Fragment {

    private Context context;
    RecyclerView recyclerView;
    List<subcriptionhistory> list;
    private FirebaseFirestore db;
    private SessionManagement sessionManagement;
    private static final String TAG = SubcriptionHistoryFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragmentsubcriptionhistory,container,false);
        recyclerView=view.findViewById(R.id.subcription_list);

        init();

        list=new ArrayList<>();

        final String[] amt = {""};
        final String[] id = {""};
        final String[] time = {""};

        // Cloud Firebase 数据库中<users> 集合下的 <UserDocumentId> 文档的 <MyOrders> 集合
        final CollectionReference ref = db.collection("users").document(sessionManagement.getUserDocumentId()).collection("MyOrders");
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots) {
                    Map<String,Object> sublist=documentSnapshot.getData();
//                    Log.i(TAG,"MyOrders sublist: "+sublist);

                    id[0]= String.valueOf(sublist.get("subcription_id")); //订购id
                    if (!id[0].equals("--")){
                        time[0]=String.valueOf(sublist.get("date"))+" "+String.valueOf(sublist.get("time_of_arrival")); //预计订单送达时间
                        Log.i(TAG,"MyOrders sublist-time[0]: "+time[0]);
                    }
                }
            }
        });

        // Cloud Firebase 数据库中<users> 集合下的 <UserDocumentId> 文档的 <Wallet> 集合
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Wallet")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots) {
                            Map<String,Object> sublist=documentSnapshot.getData();
//                            Log.i(TAG,"Wallet sublist: "+sublist);

                            id[0]= String.valueOf(sublist.get("subscription id")); //订购id
                            if (!id[0].equals("--")) {
                                amt[0]=String.valueOf(sublist.get("amount_deducted"));//扣除金额
                                Log.i(TAG,"MyOrders sublist-amt[0]: "+amt[0]);

                                list.add(new subcriptionhistory(id[0], "p1", time[0], "-", amt[0]));
                                subcriptionhistoryAdapter subcriptionhistoryAdapter = new subcriptionhistoryAdapter(getContext(), list);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                                recyclerView.setAdapter(subcriptionhistoryAdapter);
                            }
                        }
                    }
                });

        return (view);

    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        sessionManagement = new SessionManagement(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
