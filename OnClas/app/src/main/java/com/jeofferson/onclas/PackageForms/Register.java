package com.jeofferson.onclas.PackageForms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeofferson.onclas.PackageActivities.MainActivity;
import com.jeofferson.onclas.PackageObjectModel.GeneralSearchItem;
import com.jeofferson.onclas.PackageOthers.DatePickerFragment;
import com.jeofferson.onclas.R;
import com.jeofferson.onclas.PackageObjectModel.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class Register extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {


    static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    static final int REQUEST_CODE_PHONE_GALLERY = 2;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private StorageReference mStorageRef;
    private StorageReference userPicturePath;

    private FirebaseFirestore db;
    private CollectionReference usersCollection;
    private CollectionReference generalSearchListCollection;

    private UserProfileChangeRequest userProfileChangeRequest;

    private GeneralSearchItem generalSearchItem;

    private User user;
    private String username;
    private String userCover;
    private String userPicture;
    private String firstName;
    private String lastName;
    private String userFullName;
    private String userBio;
    private String gender;
    private long birthday;
    private String type;
    private List<String> departments = new ArrayList<>();
    private List<String> year = new ArrayList<>();
    private String fullType;
    private boolean isAuthorized;

    private CircleImageView registerCircleImageViewUserPicture;
    private boolean haveChosenUserPicture = false;
    private Uri chosenImageUri;

    private EditText registerEditTextFirstName;
    private EditText registerEditTextLastName;
    private EditText registerEditTextUsername;

    private RadioGroup registerRadioGroupGender;

    private TextView registerTextViewDate;
    private ImageButton registerImageButtonDate;
    private Calendar calendarBirthday;

    private EditText registerEditTextPassword;
    private EditText registerEditTextConfirmPassword;

    private ProgressBar registerProgressBar;
    private TextView registerTextViewError;

    private Button registerButton;
    private ImageView registerTextViewLogIn;

    private String userCoverDefaultDownloadUrl;

    private String userPictureMaleDefaultDownloadUrl;
    private String userPictureFemaleDefaultDownloadUrl;

    private long lastClicked = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_register);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {

            mStorageRef = FirebaseStorage.getInstance().getReference();

            db = FirebaseFirestore.getInstance();
            usersCollection = db.collection("Users");
            generalSearchListCollection = db.collection("GeneralSearchList");

            setStatusBar();

            registerCircleImageViewUserPicture = findViewById(R.id.registerCircleImageViewUserPicture);

            registerEditTextFirstName = findViewById(R.id.registerEditTextFirstName);
            registerEditTextLastName = findViewById(R.id.registerEditTextLastName);
            registerEditTextUsername = findViewById(R.id.registerEditTextUsername);

            registerRadioGroupGender = findViewById(R.id.registerRadioGroupGender);

            registerTextViewDate = findViewById(R.id.registerTextViewDate);
            registerImageButtonDate = findViewById(R.id.registerImageButtonDate);
            calendarBirthday = Calendar.getInstance();
            calendarBirthday.set(Calendar.YEAR, 2000);
            calendarBirthday.set(Calendar.MONTH, 0);
            calendarBirthday.set(Calendar.DAY_OF_MONTH, 1);

            registerEditTextPassword = findViewById(R.id.registerEditTextPassword);
            registerEditTextConfirmPassword = findViewById(R.id.registerEditTextConfirmPassword);

            registerProgressBar = findViewById(R.id.registerProgressBar);
            registerTextViewError = findViewById(R.id.registerTextViewError);

            registerButton = findViewById(R.id.registerButton);
            registerTextViewLogIn = findViewById(R.id.registerTextViewLogIn);

            userCoverDefaultDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/userCover%2FuserCoverDefault.jpg?alt=media&token=3aaf26e4-d874-4281-aab3-7e8cc6250788";

            userPictureMaleDefaultDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/userPicture%2FuserPictureMaleDefault.png?alt=media&token=7f2028c8-e539-4e7a-b346-30625b64ca5a";
            userPictureFemaleDefaultDownloadUrl = "https://firebasestorage.googleapis.com/v0/b/onclas-9415f.appspot.com/o/userPicture%2FuserPictureFemaleDefault.png?alt=media&token=34644f35-c186-4a24-b339-75b76246bc68";

            registerCircleImageViewUserPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    setUpReadExternalStoragePermission();

                }
            });

            registerRadioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {

                    if (!haveChosenUserPicture) {

                        switch (i) {

                            case R.id.registerRadioButtonMale:

                                registerCircleImageViewUserPicture.setImageResource(R.drawable.user_picture_male_default);

                                break;

                            case R.id.registerRadioButtonFemale:

                                registerCircleImageViewUserPicture.setImageResource(R.drawable.user_picture_female_default);

                                break;

                        }

                    }

                }
            });

            registerImageButtonDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DialogFragment dialogFragment = new DatePickerFragment();
                    dialogFragment.show(getSupportFragmentManager(), "date picker");

                }
            });

            registerTextViewLogIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    long nowClicked = System.currentTimeMillis();

                    if ((nowClicked - lastClicked) > 1000) {

                        lastClicked = nowClicked;
                        goToLogIn();

                    }

                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    registerTextViewError.setVisibility(View.GONE);

                    firstName = registerEditTextFirstName.getText().toString().trim();
                    lastName = registerEditTextLastName.getText().toString().trim();
                    username = registerEditTextUsername.getText().toString().trim();

                    int selectedRadioButtonGenderId = registerRadioGroupGender.getCheckedRadioButtonId();
                    RadioButton selectedRadioButtonGender = findViewById(selectedRadioButtonGenderId);
                    gender = selectedRadioButtonGender.getText().toString().trim();

                    birthday = calendarBirthday.getTimeInMillis();

                    String password = registerEditTextPassword.getText().toString().trim();
                    String confirmPassword = registerEditTextConfirmPassword.getText().toString().trim();

                    if (firstName.isEmpty()) {

                        registerEditTextFirstName.setError(getResources().getString(R.string.required));

                    } else if (lastName.isEmpty()) {

                        registerEditTextLastName.setError(getResources().getString(R.string.required));

                    } else if (username.isEmpty()) {

                        registerEditTextUsername.setError(getResources().getString(R.string.required));

                    } else if (password.isEmpty()) {

                        registerEditTextPassword.setError(getResources().getString(R.string.required));

                    } else if (confirmPassword.isEmpty()) {

                        registerEditTextConfirmPassword.setError(getResources().getString(R.string.required));

                    } else {

                        if (password.equals(confirmPassword)) {

                            userFullName = firstName + " " + lastName;

                            registerButton.setVisibility(View.GONE);
                            registerProgressBar.setVisibility(View.VISIBLE);

                            String email = username + getResources().getString(R.string.email);
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        currentUserId = mAuth.getCurrentUser().getUid();

                                        userCover = userCoverDefaultDownloadUrl;

                                        userBio = getResources().getString(R.string.userBioDefault);

                                        if (haveChosenUserPicture) {

                                            userPicturePath = mStorageRef.child("userPicture").child(mAuth.getCurrentUser().getUid() + ".jpg");
                                            userPicturePath.putFile(chosenImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        userPicturePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task) {

                                                                if (task.isSuccessful()) {

                                                                    userPicture = task.getResult().toString();

                                                                    addUserToDatabase();

                                                                } else {

                                                                    registerProgressBar.setVisibility(View.GONE);
                                                                    registerButton.setVisibility(View.VISIBLE);

                                                                    registerTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                                                                    registerTextViewError.setVisibility(View.VISIBLE);

                                                                }

                                                            }
                                                        });

                                                    } else {

                                                        registerProgressBar.setVisibility(View.GONE);
                                                        registerButton.setVisibility(View.VISIBLE);

                                                        registerTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                                                        registerTextViewError.setVisibility(View.VISIBLE);

                                                    }

                                                }
                                            });

                                        } else {

                                            if (gender.equals(getResources().getString(R.string.male))) {

                                                userPicture = userPictureMaleDefaultDownloadUrl;

                                            } else {

                                                userPicture = userPictureFemaleDefaultDownloadUrl;

                                            }

                                            addUserToDatabase();

                                        }

                                    } else {

                                        registerProgressBar.setVisibility(View.GONE);
                                        registerButton.setVisibility(View.VISIBLE);

                                        try {

                                            throw task.getException();

                                        } catch (FirebaseAuthWeakPasswordException e) {

                                            registerTextViewError.setText(getResources().getString(R.string.passwordTooShort));
                                            registerTextViewError.setVisibility(View.VISIBLE);

                                        } catch (FirebaseAuthUserCollisionException e) {

                                            registerTextViewError.setText(getResources().getString(R.string.usernameAlreadyExists));
                                            registerTextViewError.setVisibility(View.VISIBLE);

                                        } catch (Exception e) {

                                            registerTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                                            registerTextViewError.setVisibility(View.VISIBLE);

                                        }

                                    }

                                }
                            });

                        } else {

                            registerTextViewError.setText(getResources().getString(R.string.passwordDoesntMatch));
                            registerTextViewError.setVisibility(View.VISIBLE);

                        }

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


    public void setUpReadExternalStoragePermission() {

        if (Build.VERSION.SDK_INT >= 22) {

            if (ContextCompat.checkSelfPermission(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    Toast.makeText(Register.this, "Permission Required", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(Register.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                } else {

                    ActivityCompat.requestPermissions(Register.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                }

            } else {

                openGallery();

            }

        } else {

            openGallery();

        }

    }


    public void openGallery() {

        Intent intentPhoneGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentPhoneGallery.setType("image/*");
        startActivityForResult(intentPhoneGallery, REQUEST_CODE_PHONE_GALLERY);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHONE_GALLERY && resultCode == RESULT_OK && data != null) {

            haveChosenUserPicture = true;

            chosenImageUri = data.getData();
            registerCircleImageViewUserPicture.setImageURI(chosenImageUri);

        }

    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        calendarBirthday.set(Calendar.YEAR, i);
        calendarBirthday.set(Calendar.MONTH, i1);
        calendarBirthday.set(Calendar.DAY_OF_MONTH, i2);

        Date date = calendarBirthday.getTime();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        registerTextViewDate.setText(simpleDateFormat.format(date));

    }


    public void addUserToDatabase() {

        userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(userFullName)
                .setPhotoUri(Uri.parse(userPicture))
                .build();

        mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    type = "NA";
                    fullType = "NA";
                    isAuthorized = false;

                    user = new User(username, userCover, userPicture, firstName, lastName, userFullName, userBio, gender, birthday, type, departments, year, fullType, isAuthorized);
                    generalSearchItem = new GeneralSearchItem(currentUserId, userPicture, userFullName, type, departments, year, fullType, "User", isAuthorized);

                    WriteBatch writeBatch = db.batch();
                    writeBatch.set(usersCollection.document(currentUserId), user);
                    writeBatch.set(generalSearchListCollection.document(), generalSearchItem);
                    writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

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

                                                                registerEditTextPassword.setText("");
                                                                registerEditTextConfirmPassword.setText("");

                                                                    goToMain();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            } else {

                                registerProgressBar.setVisibility(View.GONE);
                                registerButton.setVisibility(View.VISIBLE);

                                registerTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                                registerTextViewError.setVisibility(View.VISIBLE);

                            }

                        }
                    });

                } else {

                    registerProgressBar.setVisibility(View.GONE);
                    registerButton.setVisibility(View.VISIBLE);

                    registerTextViewError.setText(getResources().getString(R.string.checkInternetConnection));
                    registerTextViewError.setVisibility(View.VISIBLE);

                }

            }
        });

    }


    public void goToMain() {

        Intent intentMain = new Intent(Register.this, MainActivity.class);
        startActivity(intentMain);

    }


    public void goToLogIn() {

        Intent intentLogIn = new Intent(Register.this, LogIn.class);
        startActivity(intentLogIn);

    }


}
