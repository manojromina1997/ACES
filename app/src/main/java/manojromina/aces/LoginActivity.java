package manojromina.aces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //UI variables
    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private Button mLoginButton;
    private Button mCreateAccountButton;

    //google signIN button
    private SignInButton mGoogleButton;

    //firebase Auth
    private FirebaseAuth mFirebaseAuth;

    //firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReferenceUser;

    //progress bar
    private ProgressDialog mProgress;


    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        //firebase data connecting with the users
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReferenceUser = mFirebaseDatabase.getReference().child("Users");
        mDatabaseReferenceUser.keepSynced(true);


        mProgress = new ProgressDialog(this);

        //connecting with the ui
        mGoogleButton = (SignInButton) findViewById(R.id.googleButton);
        mLoginEmailField = (EditText) findViewById(R.id.loginEmailField);
        mLoginPasswordField = (EditText) findViewById(R.id.loginPasswordField);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mCreateAccountButton = (Button) findViewById(R.id.createAccountButton);

        //when the create account button is clicked
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                //if the user is not signed in then he can not go to back or any other activity without sign in
                registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(registerIntent);
            }
        });

          //when login button is clicked
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checklogin();
            }
        });


        //Building google signIn
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //when the button is pressed signIn method is called
        mGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //connecting with googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    //this will called the signedIn
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //validating the user input
    private void checklogin() {

        String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgress.setMessage("Checking Login");
            mProgress.show();
            mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        mProgress.dismiss();
                        chekUserExists();

                    } else {
                        Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_LONG);
                    }
                }
            });
        }

    }

    //So this method is use to check whether the user data is present in our database or not
    //if user has signed up using normal email without using the facebook or gmail then their data is present on the database
    //if the user has signed in by using the gmail or facebook then there data is not present on the database
    //so for such user we should ask about more additional details like full name , profile photo and other things
    private void chekUserExists() {

        if(mFirebaseAuth.getCurrentUser()!=null) {
            final String user_id = mFirebaseAuth.getCurrentUser().getUid();

            //this will check whether user is in the databse or not
            mDatabaseReferenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //if user is there then go to main activity
                    if (dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        //if the user is not signed in then he can not go to back or any other activity without sign in
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(mainIntent);
                    }
                    //if user is not there then
                    else {
                        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
                        //if the user is not signed in then he can not go to back or any other activity without sign in
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    //when the signin is done then
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            mProgress.setMessage("Starting SignIn .......");
            mProgress.show();

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                mProgress.dismiss();
            }
        }
    }

    //connecting firebase with google.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mProgress.dismiss();
                            chekUserExists();
                        }
                        // ...
                    }
                });
    }


}

