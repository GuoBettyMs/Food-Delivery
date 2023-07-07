package com.example.modabba.Payment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.modabba.NotificationService;
import com.example.modabba.R;
import com.example.modabba.SessionManagement.SessionManagement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMoney extends AppCompatActivity implements PaymentResultWithDataListener {

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private EditText et_amount;
    private FloatingActionButton floatingActionButton;
    private static final String TAG = AddMoney.class.getSimpleName();
    private ImageView backArrow;
    private TextView min;
    int amount;
    private RequestQueue requestQueue;
    private  String url1 = "http://192.168.43.21:5000/order";
    private  String url2 = "http://192.168.43.21:5000/verifysign";
    private SessionManagement sessionManagement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);

        initViews();
        requestQueue = Volley.newRequestQueue(this);

        final DocumentReference docref=db.collection("users").document(sessionManagement.getUserDocumentId());

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = Integer.parseInt(et_amount.getText().toString().trim());
                if(amount<50){
                    min.setVisibility(View.VISIBLE);
                }else {
                    //amount = amount * 100;
                    startPayment(amount);
                }
            }
        });

    }

    /**
    * 充值
    **/
    private void startPayment(final int amt) {
        String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String,Object> transaction=new HashMap<>();
        transaction.put("date_Of_transaction",currentDate);
        transaction.put("time_Of_transaction",currentTime);
        transaction.put("amount",amt);

        // <users> 集合下文档新增 <Transaction> 集合
        final DocumentReference docref=db.collection("users").document(sessionManagement.getUserDocumentId());
        docref.collection("Transaction").add(transaction).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference){
                Toast.makeText(AddMoney.this, "Add Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddMoney.this, "Try Again", Toast.LENGTH_SHORT).show();
            }
        });

        //查询 Cloud Firestore <users> 集合->文档-> <Transaction> 集合的每一个文档所有数据
        docref.collection("Transaction")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Transaction 集合文档数量 => " + task.getResult().size());
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "Map :"+document.getId() + " => " + document.getData());
//                            }
                            //找到<Subscriptions> 集合的最新文档ID，添加订单信息和钱包流水信息
                            for(int i=0;i<task.getResult().size();i++){
                                if (i == (task.getResult().size() - 1)){
                                    String id = task.getResult().getDocuments().get(i).getId();
                                    Log.d(TAG, "文档("+task.getResult().size()+") :"+id + " => " + task.getResult().getDocuments().get(i).getData());
                                    //<Wallet> 集合 添加钱包交易信息
                                    walletTransaction(amt,id);

                                }
                            }

                        }
                    }

                });


        //<users> 集合下文档，更新钱包余额显示
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long credits  = (long)documentSnapshot.get("wallet");
                docref.update("wallet",credits+amount).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        NotificationService notificationService = new NotificationService();
                        notificationService.showNotification(getApplicationContext(),"Wallet","Money Has Been Added");
                        Toast.makeText(AddMoney.this, "Added to Modabba Cash", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddMoney.this, "something came upg ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);// 设置 ResultCode，PaymentActivity 才能接收到数据
        finish();
//        Checkout checkout=new Checkout();
//        checkout.setImage(R.drawable.app_logo);
//        final Activity activity=this;
//        try {
//            JSONObject option=new JSONObject();
//            option.put("description","Add To Modabba Cash");
//            option.put("currency","INR");
//            option.put("payment_amt",amt);
//            checkout.open(activity,option);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }

    /**
    * <Wallet> 集合 添加钱包交易信息
    **/
    private void walletTransaction(long added,String id)
    {
        String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        Map<String,Object> wal=new HashMap<>();
        wal.put("date_Of_transaction",currentDate);
        wal.put("wal_transaction_razor",id);
        wal.put("subscription id","--");
        wal.put("time_Of_transaction",currentTime);
        wal.put("amount_deducted",0);
        wal.put("amount_added",added);

        //添加钱包余额
        CollectionReference coref=db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Wallet");
        coref.add(wal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "wallet updated");
            }
        });
    }

    private void initViews() {

        sessionManagement = new SessionManagement(this);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.done);
        backArrow = findViewById(R.id.b1);
        et_amount = findViewById(R.id.amount);
        min = findViewById(R.id.minimum_indicator);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        et_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @SuppressLint("RestrictedApi")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
                else{
                    floatingActionButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
//        String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
//        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        Map<String,Object> transaction=new HashMap<>();
//        transaction.put("date_Of_transaction",currentDate);
//        transaction.put("time_Of_transaction",currentTime);
//        transaction.put("razor_payment_id",paymentData.getPaymentId());
////        transaction.put("amount",100);//transaction.put("amount",paymentData.getData().getInt("payment_amt"));
//
//        try {
//            transaction.put("amount",paymentData.getData().getInt("payment_amt"));
//            walletTransaction(paymentData.getData().getInt("payment_amt"),paymentData.getPaymentId());//添加钱包流水信息
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        //新增 <Transaction> 集合
//        final DocumentReference docref=db.collection("users").document(sessionManagement.getUserDocumentId());
//        docref.collection("Transaction").add(transaction).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//            @Override
//            public void onSuccess(DocumentReference documentReference){
//                Toast.makeText(AddMoney.this, "Add Successful", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(AddMoney.this, "Try Again", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        //更新钱包余额显示
//        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                long credits  = (long)documentSnapshot.get("wallet");
//                docref.update("wallet",credits+amount).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        NotificationService notificationService = new NotificationService();
//                        notificationService.showNotification(getApplicationContext(),"Wallet","Money Has Been Added");
//                        Toast.makeText(AddMoney.this, "Added to Modabba Cash", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(AddMoney.this, "something came upg ", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//        });

        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
    }

}
