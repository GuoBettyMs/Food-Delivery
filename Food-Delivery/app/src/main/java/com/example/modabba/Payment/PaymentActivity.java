package com.example.modabba.Payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.modabba.R;
import com.example.modabba.SessionManagement.SessionManagement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Objects;

public class PaymentActivity extends AppCompatActivity {

    private TextView number,balance;
    private SessionManagement sessionManagement;
    private FirebaseFirestore db;
    private FloatingActionButton add_money;
    private DecimalFormat decimalFormat;
    private String TAG = PaymentActivity.class.getName();
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        init();

        listenToDocument();//监听钱包余额

        add_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(PaymentActivity.this,AddMoney.class);
                startActivityForResult(i,10);//设置请求code，接收回调
            }
        });

    }

    /**
    * add_money 跳转到 PaymentActivity
     * 接收 setResult 设置回调
    **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==10 &&  resultCode==RESULT_OK){
            listenToDocument();//监听钱包余额
        }

    }

    /**
    * 监听钱包余额
    **/
    private void listenToDocument() {

        db.collection("users")
                .document(sessionManagement.getUserDocumentId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Double credits  = Double.parseDouble(Objects.requireNonNull(documentSnapshot.get("wallet")).toString());
                        if (credits > 0){
                            balance.setText("₹ " +decimalFormat.format(credits * 100 / 100.0));
                        }else{
                            balance.setText("₹ 0");
                        }
//                        Log.i(TAG,"credits "+(long)documentSnapshot.get("wallet")+"++"+credits);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Please try after some time",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void init(){

        db = FirebaseFirestore.getInstance();
        backArrow = findViewById(R.id.b2);
        number  = findViewById(R.id.payment_number);
        balance = findViewById(R.id.credit);
        add_money=findViewById(R.id.add_money);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        decimalFormat = new DecimalFormat("#.00"); //保留两位小数

        sessionManagement  = new SessionManagement(this);
        number.setText(sessionManagement.getUserNumber());

    }

}
