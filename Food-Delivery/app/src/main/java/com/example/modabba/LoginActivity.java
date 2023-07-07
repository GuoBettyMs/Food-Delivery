package com.example.modabba;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.modabba.Utils.PassingData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText textInputEditText;
    private FirebaseFirestore db;
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        checkFirestoreCollectionData();

        textInputEditText  = findViewById(R.id.login_number);
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.length()==11){

                    String phone_number = "+86" + charSequence.toString();

                    PassingData model = new PassingData(LoginActivity.this);
                    model.setPhone(phone_number);

                    startActivity(new Intent(LoginActivity.this, VerificationActivity.class).putExtra("object",model)
                            .putExtra("callingActivity",ActivityConstants.LoginActivity));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    /**
     * 查询 Cloud Firestore <users>集合下的每一个文档所有数据
     **/
    public void checkFirestoreCollectionData(){
        //查询 Cloud Firestore <users>集合下的每一个文档所有数据
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG,  "Verification: "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}
