package manojromina.aces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;

    private Button mRegisterButton;

    //firebase authentication
    private FirebaseAuth mFirebaseAuth;
    private ProgressDialog mProgress;

    //firebase database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgress = new ProgressDialog(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        //firebase Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Users");

        nameField = (EditText)findViewById(R.id.nameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        mRegisterButton = (Button)findViewById(R.id.registerButton);

        mRegisterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startRegister();
            }
        });

    }


    //when the user pressed register button
    private void startRegister() {

        //all the detail are stored into string
        final String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        //checking whether the use have enter all the field
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            //the complete listener will return the things if the user is successfully register

            mProgress.setMessage("Signing Up ...");
            mProgress.show();

            //creating user with emai land password
            mFirebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful()){

                        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                        //if the user is not signed in then he can not go to back or any other activity without sign in
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(setupIntent);

                    }
                }
            });
        }

    }
}
