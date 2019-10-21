package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jeofferson.onclas.PackageActivities.ADMIN_ManageAccountRequests;
import com.jeofferson.onclas.PackageActivities.MainActivity;
import com.jeofferson.onclas.R;

import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;

    private EditText logInEditTextUserName;
    private EditText logInEditTextPassword;

    private ProgressBar logInProgressBar;
    private TextView logInTextViewError;

    private Button logInButton;
    private FloatingActionButton logInTextViewRegister;

    private long lastClicked = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_log_in);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {

            db = FirebaseFirestore.getInstance();
            usersCollection = db.collection("Users");

            setStatusBar();

            logInEditTextUserName = findViewById(R.id.logInEditTextUserName);
            logInEditTextPassword = findViewById(R.id.logInEditTextPassword);

            logInProgressBar = findViewById(R.id.logInProgressBar);
            logInTextViewError = findViewById(R.id.logInTextViewError);

            logInButton = findViewById(R.id.logInButton);
            logInTextViewRegister = findViewById(R.id.logInTextViewRegister);

            logInTextViewRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    long nowClicked = System.currentTimeMillis();

                    if ((nowClicked - lastClicked) > 1000) {

                        lastClicked = nowClicked;
                        goToRegister();

                    }

                }
            });

            logInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    logInTextViewError.setVisibility(View.GONE);

                    String username = logInEditTextUserName.getText().toString().trim();
                    final String password = logInEditTextPassword.getText().toString().trim();

                    if (username.isEmpty()) {

                        logInEditTextUserName.setError(getResources().getString(R.string.required));

                    } else if (password.isEmpty()) {

                        logInEditTextPassword.setError(getResources().getString(R.string.required));

                    } else {

                        logInButton.setClickable(false);
                        logInProgressBar.setVisibility(View.VISIBLE);

                        final String email = username + getResources().getString(R.string.email);
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseInstanceId.getInstance().getInstanceId()
                                            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                                @Override
                                                public void onSuccess(InstanceIdResult instanceIdResult) {

                                                    String deviceToken = instanceIdResult.getToken();

                                                    Map<String, Object> tokenMap = new HashMap<>();
                                                    tokenMap.put("deviceToken", deviceToken);

                                                    usersCollection.document(mAuth.getCurrentUser().getUid()).update(tokenMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    logInEditTextPassword.setText("");

                                                                    if (email.equals("ADMIN101@jeofferson.com") && password.equals("ADMIN101")) {

                                                                        goTo_ADMIN_ManageAccountRequests();

                                                                    } else {

                                                                        goToMain();

                                                                    }

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                }
                                                            });

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                } else {

                                    logInProgressBar.setVisibility(View.GONE);
                                    logInButton.setClickable(true);

                                    try {

                                        throw task.getException();

                                    } catch (FirebaseAuthInvalidUserException e) {

                                        logInTextViewError.setText(getResources().getString(R.string.invalidUsernamePassword));
                                        logInTextViewError.setVisibility(View.VISIBLE);

                                    } catch (FirebaseAuthInvalidCredentialsException e) {

                                        logInTextViewError.setText(getResources().getString(R.string.invalidUsernamePassword));
                                        logInTextViewError.setVisibility(View.VISIBLE);

                                    } catch (Exception e) {

                                        logInTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                                        logInTextViewError.setVisibility(View.VISIBLE);

                                    }

                                }

                            }
                        });

                    }

                }
            });

        } else {

            goToMain();

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        checkUserExistence();

    }


    public void checkUserExistence() {

        if (mAuth.getCurrentUser() != null) {

            goToMain();

        }

    }


    public void setStatusBar() {

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

    }


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.onclas_logo)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.doWantToExit))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finishAffinity();

                    }

                })
                .setNegativeButton(getResources().getString(R.string.no), null)
                .show();

    }


    public void goToMain() {

        Intent intentMain = new Intent(LogIn.this, MainActivity.class);
        startActivity(intentMain);

    }


    public void goTo_ADMIN_ManageAccountRequests() {

        Intent intent_ADMIN_ManageAccountRequests = new Intent(LogIn.this, ADMIN_ManageAccountRequests.class);
        startActivity(intent_ADMIN_ManageAccountRequests);

    }


    public void goToRegister() {

        Intent intentRegister = new Intent(LogIn.this, Register.class);
        startActivity(intentRegister);

    }


}
