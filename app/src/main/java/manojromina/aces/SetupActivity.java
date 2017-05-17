package manojromina.aces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mSetupImageButton;
    private EditText mSetupNameField;
    private EditText mSetupDescriptionField;
    private Button mSetupFinishButton;

    //when user select the image then it will have some value till the nthe value is null.
    private Uri mImageUri =  null ;

    private static final int GALLERY_REQUEST = 1;

    //firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    //firebase Auth
    private FirebaseAuth mFirebaseAuth;

    //firebase storage
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;


    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mProgress = new ProgressDialog(this);

        mSetupImageButton = (ImageButton)findViewById(R.id.profilePic);
        mSetupNameField = (EditText)findViewById(R.id.setupNameField);
        mSetupDescriptionField = (EditText)findViewById(R.id.setupDescriptionField);
        mSetupFinishButton = (Button)findViewById(R.id.setupSubmitButton);

        //firebase databae
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Users");

        //firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        //firebase storage
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();


        //opening the gallery when the image button is clciked
        mSetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        //when finished button is pressed
        mSetupFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button Clicked");
                startSetupAccount();
            }
        });
    }

    //when the image from the gallery is selected and the image is cropped
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST  && resultCode == RESULT_OK)
        {

            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        //when image is crop then changing the default pic to the cropped image pic
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mSetupImageButton.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void startSetupAccount() {
        final String name = mSetupNameField.getText().toString().trim();
        final String description = mSetupDescriptionField.getText().toString().trim();
        final String user_id = mFirebaseAuth.getCurrentUser().getUid();


        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(description)&& mImageUri != null)
        {


            mProgress.setMessage("Finishing Setup");
            mProgress.show();

            //adding the profile pic in the folder
            StorageReference filepath = mStorageReference.child("Profile_Pics").child(mImageUri.getLastPathSegment());

            //this will put file in the firebase storage
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //if the image is uploaded successfully then the download link is generated
                    Uri downloadUri = taskSnapshot.getDownloadUrl();

                    mDatabaseReference.child(user_id).child("name").setValue(name);
                    mDatabaseReference.child(user_id).child("description").setValue(description);
                    mDatabaseReference.child(user_id).child("image").setValue(downloadUri.toString()  );

                    mProgress.dismiss();

                    Intent mainIntent = new Intent(SetupActivity.this ,MainActivity.class);
                    //if the user is not signed in then he can not go to back or any other activity without sign in
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(mainIntent);

                }
            });



        }
    }


}

