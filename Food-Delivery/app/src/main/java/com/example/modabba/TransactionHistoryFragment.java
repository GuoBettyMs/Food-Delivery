package com.example.modabba;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionHistoryFragment extends Fragment {

    RecyclerView recyclerView;
    List<transactionhistory>  list;
    private FirebaseFirestore db;
    private SessionManagement sessionManagement;
    private static final String TAG = TransactionHistoryFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragmenttransactionhistory,container,false);
        recyclerView=view.findViewById(R.id.transaction_list);

        init();
        list=new ArrayList<>();
        final String[] amt = {""};
        final String[] id = {""};
        final String[] time = {""};

        // Cloud Firebase 数据库中<users> 集合下的 <UserDocumentId> 文档的 <Wallet> 集合
        final CollectionReference ref = db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Wallet");
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots) {
                    Map<String, Object> sublist = documentSnapshot.getData();
                    Log.i(TAG,"TransactionHistoryFragment sublist: "+sublist);

                    long amount  = Long.parseLong(Objects.requireNonNull(documentSnapshot.get("amount_added")).toString());
                    if (amount < 50){
                        amt[0]=String.valueOf(sublist.get("amount_deducted")); //扣除金额
                        id[0]= String.valueOf(sublist.get("subscription id")); //订单ID
                        time[0]= sublist.get("date_Of_transaction") +" "+ sublist.get("time_Of_transaction");

                        //红色"-"
                        list.add(new transactionhistory(id[0], "-",
                                "doyel saha",time[0],"card", amt[0],true));

                    }else{
                        amt[0]=String.valueOf(sublist.get("amount_added")); //增加余额
                        id[0]= String.valueOf(sublist.get("wal_transaction_razor")); //充值ID
                        time[0]=String.valueOf(sublist.get("date_Of_transaction"))+" "+String.valueOf(sublist.get("time_Of_transaction"));

                        //绿色"+"
                        list.add(new transactionhistory(id[0], "+",
                                "doyel saha",time[0],"card", amt[0], false));

                    }

                    transactionhistoryAdapter transactionhistoryAdapter = new transactionhistoryAdapter(getActivity(), list);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(transactionhistoryAdapter);

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
