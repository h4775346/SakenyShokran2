package com.snonosystems.sakenyshokran;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    TextView txtSignup;
    ViewDialog dialog;
    Button btn_login;
    ProgressBar progress;
    EditText txt_mail, txt_pass;
    CheckBox check_save;
    CallbackManager mCallbackManager;
    public static String TAG = "Actions Login";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    SignInButton google_login;
    private ProgressDialog mdialog;
    public static ProfileModel profileModel;
    FirebaseDatabase database;
    DatabaseReference myRef;

    private static final int RC_SIGN_IN = 9001;

    @Override
    public void onStart() {

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        profileModel = new ProfileModel();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            updateUI(currentUser);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mdialog = new ProgressDialog(this);
        mdialog.setTitle("wait Please");
        mdialog.setMessage("Connecting to google");
        mdialog.setCancelable(true);


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        txt_mail = findViewById(R.id.txt_mail_login);
        txt_pass = findViewById(R.id.txt_pass_login);
        progress = findViewById(R.id.pro_bar);
        google_login = findViewById(R.id.gmail_login);
        google_login.setSize(SignInButton.SIZE_STANDARD);
        check_save = findViewById(R.id.check_save);

        SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        String user = preferences.getString("email", "null");
        String pass = preferences.getString("Password", "null");

        if (!user.equals("null") && !pass.equals("null")) {

            txt_mail.setText(user);
            txt_pass.setText(pass);

            check_save.setChecked(true);

        }

        check_save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    txt_mail.setText("");
                    txt_mail.setText("");
                    SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                }
            }
        });

        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdialog.show();
                signIn();
            }
        });


        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        txtSignup = findViewById(R.id.txt_signup);
        btn_login = findViewById(R.id.btn_login);
        dialog = new ViewDialog(LoginActivity.this);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_login.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);


                if (TextUtils.isEmpty(txt_pass.getText()) || TextUtils.isEmpty(txt_mail.getText())) {

                    showMessage("Please Confirm All Empty Data");

                } else {
                    String email = txt_mail.getText().toString();
                    String password = txt_pass.getText().toString();

                    startLogin(email, password);
                }


            }
        });

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), SignSellerActivity.class));

            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    private void showMessage(String message) {

        btn_login.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
        dialog.showDialog(message);

    }

    private void startLogin(final String email, final String password) {


        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (check_save.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email", email);
                    editor.putString("Password", password);
                    editor.apply();


                }
                updateUI(user);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e.toString().contains("There is no user")) {
                    showMessage("Failed to Login . \n Please Check Your username or password");
                } else {
                    showMessage(e.getMessage());

                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                mdialog.hide();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
        // Pass the activity result back to the Facebook SDK


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        mdialog.hide();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {

        if (user != null) {


            profileModel.clear();
            profileModel.setEmail(user.getEmail());


            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("users");

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        profileModel = snapshot.getValue(ProfileModel.class);
                        Toast.makeText(LoginActivity.this, profileModel.getName(), Toast.LENGTH_LONG).show();


                        if (profileModel.getUid().equals(user.getUid())) {
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            intent.putExtra("mail",profileModel.getClass());

                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                            break;
                        }


                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });


        }


    }


}
