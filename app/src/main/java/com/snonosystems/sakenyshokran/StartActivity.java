package com.snonosystems.sakenyshokran;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;


public class StartActivity extends AppCompatActivity {

    CircleImageView image_prof;
    Button btn_finish_reg;
    FirebaseAuth mAuth;
    public static final int IMG_CODE = 0;
    Uri image_uri;

    boolean PICKED_IMAGE = false;
    EditText txt_name, txt_phone;
    TextView txt_skip;
    ViewDialog dialog;
    FirebaseUser mUser;
    FirebaseDatabase database ;
    DatabaseReference myRef ;
    StorageReference UserProfileImageRef;
    ProgressBar progressBar;
    TextView prog_txt;

    int status = 0;

    ProgressDialog progressdialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        dialog = new ViewDialog(StartActivity.this);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("users_images");

        btn_finish_reg = findViewById(R.id.btn_finish_login);
        mAuth = FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        image_prof = findViewById(R.id.image_profile_start);

        txt_name = findViewById(R.id.txt_name);
        txt_phone = findViewById(R.id.txt_phone);
        txt_skip =findViewById(R.id.txt_skip);

        prog_txt = findViewById(R.id.progress_txt);

        progressBar = findViewById(R.id.progress_Bar);




        txt_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                finish();


            }
        });














        image_prof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMG_CODE);


            }
        });


        btn_finish_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( TextUtils.isEmpty(txt_name.getText())){


                    showMessage("Please Enter Your Name");
                    return;


                }else if (TextUtils.isEmpty(txt_phone.getText())){

                    showMessage("Please Enter Your Name");
                    return;
                }


                String name = txt_name.getText().toString();
                String phone = txt_phone.getText().toString();

                finishRegistration(name,phone);



            }
        });




    }

    private void finishRegistration(final String name, final String phone) {


        if (PICKED_IMAGE){

         //   CreateProgressDialog();
            Uri resultUri = image_uri;

            final StorageReference filePath = UserProfileImageRef.child(LoginActivity.profileModel.getUid() + ".jpg");

            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                {




                    if(task.isSuccessful())
                    {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String url = uri.toString();


                                LoginActivity.profileModel.setImage(url);

                                continueAddingData(name,phone);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                               showMessage(e.getMessage());
                            }
                        });




                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showMessage(e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {


                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress , true);
                    progressBar.setVisibility(View.VISIBLE);
                    prog_txt.setText((int)progress + " %");
                    Log.w("Progress","onProgress: " + progress + "% uploaded");






              /*      if(progress>=100){
                        progressdialog.dismiss();
                    }
*/



                }
            });
        }else {
            continueAddingData(name,phone);

        }









    }

    
    private void myProg(final int progress){

        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressdialog.setProgress( progress);
            }
        },500);



    }

    private void continueAddingData(String name , String phone) {


        LoginActivity.profileModel.setName(name);
        LoginActivity.profileModel.setPhone(phone);

        database.getReference("users").child(LoginActivity.profileModel.getUid()).setValue(LoginActivity.profileModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(e.getMessage());
            }
        });



    }


    private void showMessage(String message) {

        dialog.showDialog(message);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == IMG_CODE && resultCode == RESULT_OK && data.getData() != null) {
            image_uri = data.getData();

            CropImage.activity(image_uri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                image_prof.setImageURI(resultUri);
                image_uri=resultUri;
                PICKED_IMAGE = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }


    }


    public void CreateProgressDialog()
    {

        progressdialog = new ProgressDialog(StartActivity.this);

        progressdialog.setIndeterminate(false);

        progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressdialog.setCancelable(true);

        progressdialog.setMax(100);

        progressdialog.show();

    }


}
