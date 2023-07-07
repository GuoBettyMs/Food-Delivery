package com.example.modabba.Dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.modabba.ActivityConstants;
import com.example.modabba.BottomSheetCallback;
import com.example.modabba.R;
import com.example.modabba.SessionManagement.SessionManagement;
import com.example.modabba.VerificationActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    //private BottomSheetListener bottomSheetListener;
    private String number,email,username;
    private EditText editTextNumber,editTextEmail,editTextUsername,editTextAlternateNumber;
    private Button update;
    private Context context;
    private String documentId;
    private SessionManagement sessionManagement;
   // private AwesomeValidation awesomeValidation;
   private BottomSheetCallback bottomSheetCallback; //实例化自定义接口

    public EditProfileBottomSheet(Context context, String number, String email, String username, String documentId) {
        this.context = context;
        this.number = number.substring(3);
        this.email = email;
        this.username = username;
        this.documentId = documentId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.edit_profile_layout,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setText();

        sessionManagement = new SessionManagement(context);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String n  = editTextNumber.getText().toString().trim();
                String e = editTextEmail.getText().toString().trim();
                String u = editTextUsername.getText().toString().trim();

                //TODO: Functionality Add 1. Send OTP 2. Verify Otp 3. Logout User 4. Clear Session

                    Log.i(EditProfileBottomSheet.class.getSimpleName(),n);

                    if(!username.equals(u) && !u.isEmpty()){
                        updateUsername(u);
                    }
//                    if(!email.equals(e) && !e.isEmpty() && e.contains("@")){
                    if(!email.equals(e) && !e.isEmpty() ){
                            sendEmailLink(e);
                    }
                    if(!number.equals(n) && !n.isEmpty() && n.length()==11){
                        updatePhoneNumber(n);
                    }

                dismiss();
            }
        });

    }

    /**
     * 更新用户联系号码
     **/
    private void updatePhoneNumber(String new_number) {
        //TODO: Functionality Add 1. Send OTP 2. Verify Otp 3. Logout User 4. Clear Session

        startActivity(new Intent(context, VerificationActivity.class)
        .putExtra("callingActivity", ActivityConstants.MainActivity)
        .putExtra("newnumber",new_number)
                .putExtra("documentId",sessionManagement.getUserDocumentId()));
        Toast.makeText(context,"new_number verificating", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新用户邮箱
     **/
    private void sendEmailLink(final String e) {

        //TODO:send email to update email
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(documentId)
                .update("email",e)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sessionManagement.changeEmail(e);
                        Toast.makeText(context,"email Successfully Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(EditProfileBottomSheet.class.getSimpleName(),e.getMessage());
                        Toast.makeText(context,"Please try after some time", Toast.LENGTH_SHORT).show();

                    }
                });

        sessionManagement.changeEmail(e);
        Toast.makeText(context,"email Successfully Updated", Toast.LENGTH_SHORT).show();
    }

    /**
    * 更新用户名
    **/
    private void updateUsername(final String u) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(documentId)
                .update("username",u)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sessionManagement.changeUserName(u);
                        Toast.makeText(context,"Username Successfully Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(EditProfileBottomSheet.class.getSimpleName(),e.getMessage());
                        Toast.makeText(context,"Please try after some time", Toast.LENGTH_SHORT).show();

                    }
                });

        sessionManagement.changeUserName(u);
        Toast.makeText(context,"Username Successfully Updated", Toast.LENGTH_SHORT).show();

    }

    /**
     * 设置文本UI
     **/
    private void setText() {

        //Setting the text in the editText
        editTextUsername.setText(username);
//        editTextUsername.setSelection(username.length());
        editTextNumber.setText(number);
//        editTextNumber.setSelection(number.length());
        editTextEmail.setText(email);
        editTextEmail.setSelection(email.length());

    }

    /**
    * 初始化
    **/
    private void init(View view){

        editTextNumber = view.findViewById(R.id.edit_profile_number);
        editTextEmail  = view.findViewById(R.id.edit_profile_email);
        editTextUsername = view.findViewById(R.id.edit_profile_username);
        update = view.findViewById(R.id.edit_profile_update_button);
    }

    private void addValidation() {
    }

    /**
    * 关闭，触发自定义接口
    **/
    @Override
    public void dismiss() {
        super.dismiss();

        bottomSheetCallback.onBottomCallback(); //调用接口方法

    }

    /**
    * 连接实例化的接口
    **/
    public void setBottomSheetCallback(BottomSheetCallback bottomSheetCallback) {
        this.bottomSheetCallback = bottomSheetCallback;
    }

}
