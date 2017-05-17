package manojromina.aces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER =  2;
    private static final String MESSAGE_LENGTH_KEY = "message_length";

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    //creating a variable for accesing firebase realtime datbase
    private FirebaseDatabase mFirebaseDatabase;
    //creating a reference variable for database
    private DatabaseReference mMessageDatabaseRefernce;

    //for gettting the data from the database whenever we send any message
    private ChildEventListener mChildEventListener;

    //firebase authentication variable
    private FirebaseAuth mFirebaseAuth;

    //for checking whether the user is signned in or sign out this can be done by using authstatelistener
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    //firebase Storage
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent i = new Intent (MessageActivity.this, MyService.class);
        startService(i);


        mUsername = ANONYMOUS;



        //Initializing firbase component
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        //mMessageDatabaseReference = mFirebaseDatabase.getReference() those give reference to the root node
//mMessageDatabaseReference = mFirebaseDatabase.getReference().child("messages"); it specifies that we are interested in child node

        mMessageDatabaseRefernce = mFirebaseDatabase.getReference().child("messages");
        mMessageDatabaseRefernce.keepSynced(true);;
        mPhotosStorageReference = mFirebaseStorage.getReference().child("aces_photos/");



        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<Messages> Messages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.message_item, Messages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //when the user click on send button then the text and the username will be pass to the Message Object.
                Messages messages = new Messages(mMessageEditText.getText().toString(),mUsername,null);

                //this will add message to the database.
                mMessageDatabaseRefernce.push().setValue(messages);
                mMessageEditText.setText("");
            }
        });

        //auth state listner
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null)
                {
                    //user is signed in
                    onSignedInInitialize(user.getDisplayName());

                }
                else {
                    //user is sign out
                    onSignedOutCleanup();



                }



            }
        };




    }

    private void onSignedInInitialize(String username)
    {
        mUsername = username ;
        attachDatabaseReadListener();


    }

    private void onSignedOutCleanup()
    {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mMessageAdapter.clear();
        detachDatabaseReadListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    public void attachDatabaseReadListener() {
        //if there is no message then attach the message to the screen because the user has logged in
        if (mChildEventListener == null) {

            //reading fro mthe database
            mChildEventListener = new ChildEventListener() {

                //when message is added then this method is called
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    //datasSnapshot is a object of the firebase datbase
                    //so the data received from the database will be of format present in the FriendlyMessage

                    Messages messages = dataSnapshot.getValue(Messages.class);
                    //By this we are adding the received data from database to the app
                    mMessageAdapter.add(messages);

                }

                //when changes are done in  the previous message then this method is called
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                //when the message is deleted this method is called
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                //when the message is moved this method is called.
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                //when there is error in the childeventlistener this method is caled
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
//when changes is made in the datanbase then childeventlistener will be called.
            mMessageDatabaseRefernce.addChildEventListener(mChildEventListener);
        }
    }
    public void detachDatabaseReadListener(){
        //if mChildEventListener is not null then it means that it has value .
        //so we have to clear the value so if tbe user has signed out he will not able to read the message so we set the listener to null
        if(mChildEventListener != null) {
            mMessageDatabaseRefernce.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            //data will contain a uri
            Uri selectedImageUri = data.getData();

            //for example uri - content://local_images/foo/4 then lastPathSegment will take 4
            StorageReference photoRef = mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            //upload file to firebase storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // When the image has successfully uploaded, we get its download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    // Set the download URL to the message box, so that the user can send it to the database
                    Messages messages = new Messages(null, mUsername, downloadUrl.toString());
                    mMessageDatabaseRefernce.push().setValue(messages);

                }
            });


        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_home)
        {
            startActivity(new Intent(MessageActivity.this,MainActivity.class));
        }
        if(item.getItemId() == R.id.action_profile)
        {
            startActivity(new Intent(MessageActivity.this,ProfileActivity.class));
        }
        if(item.getItemId() == R.id.action_forum)
        {
            startActivity(new Intent(MessageActivity.this,MessageActivity.class));
        }



        return super.onOptionsItemSelected(item);
    }
}
