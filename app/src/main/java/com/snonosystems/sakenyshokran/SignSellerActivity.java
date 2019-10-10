package com.snonosystems.sakenyshokran;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignSellerActivity extends AppCompatActivity {

    EditText email , pass, confirmPass;
    FirebaseAuth mAuth;
    Button btn_signup;
    ViewDialog dialog;
    ProgressBar progress ;
    FirebaseDatabase database ;
    DatabaseReference myRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_seller);
        database = FirebaseDatabase.getInstance();



       email = findViewById(R.id.txt_mail);
       pass = findViewById(R.id.txt_pass);
       confirmPass = findViewById(R.id.txt_pass_confirm);

       btn_signup = findViewById(R.id.btn_signup);

       mAuth = FirebaseAuth.getInstance();
       dialog =new ViewDialog(this);
        progress=findViewById(R.id.pro_bar);



       btn_signup.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               progress.setVisibility(View.VISIBLE);
               btn_signup.setVisibility(View.INVISIBLE);
               if(!TextUtils.isEmpty(email.getText()) || !TextUtils.isEmpty(pass.getText()) || !TextUtils.isEmpty(confirmPass.getText())){

                String mail = email.getText().toString();
                String password=pass.getText().toString();
                String con_pass = confirmPass.getText().toString();

                if (password.length()<6){
                   showMessage("password Should not be less than 6 ");
                }else {
                    if (!password.equals(con_pass))
                    {
                        showMessage("password do not match");
                    }else{
                        SignUp(mail , password);
                    }
                }



               }else {
                   showMessage("Please Confirm All Empty Data");
               }


           }
       });



    }

    private void SignUp(String mail, String password) {

        mAuth.createUserWithEmailAndPassword(mail, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                myRef = database.getReference("users").child(Objects.requireNonNull(authResult.getUser()).getUid());


                LoginActivity.profileModel.clear();
                LoginActivity.profileModel.setEmail(Objects.requireNonNull(authResult.getUser()).getEmail());
                LoginActivity.profileModel.setUid(authResult.getUser().getUid());
                myRef.setValue(LoginActivity.profileModel);


                startActivity(new Intent(SignSellerActivity.this,StartActivity.class));




                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d("theMessage",e.getMessage());

                        showMessage(e.getMessage());

                    }
                });


    }


    private void showMessage(String message) {

        btn_signup.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
        dialog.showDialog(message);

    }
}
