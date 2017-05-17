package manojromina.aces;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SingleBlogActivity extends AppCompatActivity {

    private String mPost_key = null;
    private ImageView mBlogSingleImage;
    private TextView mBlogSingleUserName;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDescription;
    private Button mBlogRemovePost;
    private Button mBlogEditPost;

    //firebase auth
    private FirebaseAuth mFirebaseAuth;


    private DatabaseReference mDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_blog);



        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blogs");
        mFirebaseAuth = FirebaseAuth.getInstance();

        mPost_key = getIntent().getExtras().getString("blog_Id");
        System.out.println("Post Key is "+mPost_key);


        mBlogSingleImage = (ImageView)findViewById(R.id.blog_single_image);
        mBlogSingleUserName = (TextView) findViewById(R.id.blog_username);
        mBlogSingleTitle = (TextView) findViewById(R.id.blog_single_title);
        mBlogSingleDescription = (TextView) findViewById(R.id.blog_single_description);
        mBlogRemovePost = (Button)findViewById(R.id.removePost);
        mBlogEditPost = (Button)findViewById(R.id.editPost);




        // this method is use to get the blog elements
        mDatabaseReference.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_description = (String) dataSnapshot.child("description").getValue();
                final String post_username = (String) dataSnapshot.child("username").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();


                mBlogSingleTitle.setText(post_title);
                mBlogSingleUserName.setText(post_username);
                mBlogSingleDescription.setText(post_description);


                Glide.with(SingleBlogActivity.this)
                        .load(post_image)
                        .into(mBlogSingleImage);


                mBlogSingleUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent userProfileIntent = new Intent(SingleBlogActivity.this,OtherProfileActivity.class);
                        //blog id is passed so by using it whole detail of the blog can be taken
                        userProfileIntent.putExtra("user_name",post_username);
                        startActivity(userProfileIntent);

                    }
                });

                //checking if the blog posted user is same as current user then only he can delete a post

                if(mFirebaseAuth.getCurrentUser().getUid().equals(post_uid) )
                {

                    mBlogEditPost.setVisibility(View.VISIBLE);
                    mBlogRemovePost.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBlogEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(MainActivity.this,post_key,Toast.LENGTH_LONG).show();
                Intent editPostIntent = new Intent(SingleBlogActivity.this,EditPostActivity.class);
                //blog id is passed so by using it whole detail of the blog can be taken
                editPostIntent.putExtra("edit_blog_id",mPost_key);
                startActivity(editPostIntent);
            }
        });


        mBlogRemovePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabaseReference.child(mPost_key).removeValue();

                Intent mainIntent = new Intent(SingleBlogActivity.this,MainActivity.class);
                startActivity(mainIntent);


            }
        });
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
            startActivity(new Intent(SingleBlogActivity.this,MainActivity.class));
        }
        if(item.getItemId() == R.id.action_profile)
        {
            startActivity(new Intent(SingleBlogActivity.this,ProfileActivity.class));
        }
        if(item.getItemId() == R.id.action_forum)
        {
            startActivity(new Intent(SingleBlogActivity.this,MessageActivity.class));
        }



        return super.onOptionsItemSelected(item);
    }
}
