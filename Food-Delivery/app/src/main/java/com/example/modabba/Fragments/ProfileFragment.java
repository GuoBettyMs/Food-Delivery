package com.example.modabba.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.modabba.ActivityConstants;
import com.example.modabba.BottomSheetCallback;
import com.example.modabba.Dialogs.CustomDialogFragment;
import com.example.modabba.Dialogs.EditProfileBottomSheet;
import com.example.modabba.FeedbackActivity;
import com.example.modabba.MapActivity;
import com.example.modabba.Payment.AddMoney;
import com.example.modabba.Payment.PaymentActivity;
import com.example.modabba.R;
import com.example.modabba.SessionManagement.SessionManagement;
import com.example.modabba.TrialOrder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private Context context;
    private SessionManagement sessionManagement;
    private FirebaseAuth firebaseAuth;
    private Button logout;
    private TextView currentUser, currentEmail, phone, edit;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Cloud Firestore 的初始化实例;
    private LinearLayout manage_address, payment, order_trial, shareapp, leavefeedback,contactus;
    private static final String TAG = ProfileFragment.class.getSimpleName();

    public ProfileFragment(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        init();

        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        setCurrentUserDetails();

        //更新地址
        manage_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, MapActivity.class).putExtra("callingActivity",002)
                        .putExtra("Sessionid",sessionManagement.getUserDocumentId()));
            }
        });

        //体验订单
        order_trial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, TrialOrder.class));
            }
        });

        //退出登录
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "Are you sure want to logout";
                String p = "Logout";
                String n = "Cancel";
                CustomDialogFragment dialog  = new CustomDialogFragment(context,title,p,n, ActivityConstants.ProfileFragment);
//                dialog.show(getFragmentManager(),"dialog");
                dialog.show(getParentFragmentManager(),"dialog");
            }
        });

        //评价
        leavefeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, FeedbackActivity.class));
            }
        });

        //充值
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, PaymentActivity.class));
            }
        });

        //分享
        shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Add a share link
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Share");
                i.putExtra(android.content.Intent.EXTRA_TEXT, "Share link");
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });

        //联系，跳转到打电话页面
        contactus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:Add Restaurant Phone number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0123456789"));
                startActivity(intent);
            }
        });


        //编辑个人信息
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileBottomSheet editProfileBottomSheet = new EditProfileBottomSheet(
                        context,phone.getText().toString(),currentEmail.getText().toString(),
                        currentUser.getText().toString(),sessionManagement.getUserDocumentId());
                editProfileBottomSheet.show(getChildFragmentManager(),"bottomSheet");

                //执行自定义接口方法
                editProfileBottomSheet.setBottomSheetCallback(new BottomSheetCallback() {
                    @Override
                    public void onBottomCallback() {
                        setCurrentUserDetails();
                    }
                });

            }
        });

    }

    /**
    * 显示当前用户基本信息
    **/
    private void setCurrentUserDetails() {

        HashMap<String,String> map= sessionManagement.getUserDetails();
        currentUser.setText(map.get("NAME"));
        currentEmail.setText(map.get("EMAIL"));
        phone.setText(map.get("NUMBER"));

    }

    private void init() {

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManagement = new SessionManagement(context);


        checkFirestoreCollectionData();
    }

    private void initView(View view) {

        logout = view.findViewById(R.id.logout);
        currentUser = view.findViewById(R.id.name);
        currentEmail = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.number);
        edit = view.findViewById(R.id.editProfile);
        order_trial=view.findViewById(R.id.order_trial);
        manage_address = view.findViewById(R.id.manage_address);
        contactus=view.findViewById(R.id.contact_us);
        payment = view.findViewById(R.id.payment);
        shareapp = view.findViewById(R.id.share_app);
        leavefeedback = view.findViewById(R.id.leave_feedback);
    }

    /**
     * 查询 Cloud Firestore 不同集合下的每一个文档所有数据
     **/
    public void checkFirestoreCollectionData(){
        //查询 Cloud Firestore <users>集合下的每一个文档所有数据
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "users 集合文档数量 => " + task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG,  "users 集合 "+document.getId() + " => " + document.getData());
//                                Log.d(TAG,  "users 集合 "+document.getId() + " => wallet属性值： " + document.get("wallet"));
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <Subscriptions> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Subscriptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Subscriptions 集合 "+task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Subscriptions 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <MyOrders> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("MyOrders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "MyOrders 集合 "+task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "MyOrders 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <Wallet> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Wallet 集合 "+task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "Wallet 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        //查询 Cloud Firestore <users> 集合->文档-> <Transaction> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Transaction")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Transaction 集合 "+task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "Transaction 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

//        查询 Cloud Firestore <users> 集合->文档-> <Feedback> 集合的每一个文档所有数据
        db.collection("users").document(sessionManagement.getUserDocumentId()).collection("Feedback")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Feedback 集合 "+task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, "Feedback 集合 "+document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }

}
