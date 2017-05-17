package manojromina.aces;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class OtherProfileActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    //database reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReferenceBlogs;
    private DatabaseReference mDatabaseReferenceUser;
    private DatabaseReference mDatabaseCurrentUser;

    private Query mQueryCurrentUser;

    private FirebaseAuth mFirebaseAuth;

    //Creating UI variables
    private ImageButton mOtherProfilePagePic;
    private TextView mOtherProfileUsername;
    private TextView mOtherProfileDescription;
    private Button mOtherEditProfileButton;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mOtherProfilePagePic = (ImageButton)findViewById(R.id.profilePagePic);
        mOtherProfileUsername = (TextView)findViewById(R.id.profileUsername);
        mOtherProfileDescription = (TextView) findViewById(R.id.profileDescription);
        mOtherEditProfileButton = (Button) findViewById(R.id.editProfile);


        mOtherEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(OtherProfileActivity.this, EditProfileActivity.class);

                startActivity(editProfileIntent);
            }
        }

        );
        mFirebaseAuth = FirebaseAuth.getInstance();
        //for offline data saving


        //connecting to the firebase Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        mDatabaseReferenceBlogs = mFirebaseDatabase.getReference().child("Blogs");
        mDatabaseReferenceUser = mFirebaseDatabase.getReference().child("Users");
        mDatabaseCurrentUser = mFirebaseDatabase.getReference().child("Blogs");


        mUsername = getIntent().getExtras().getString("user_name");
        System.out.println("user_name "+mUsername);

        final String currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        System.out.println("CurrentUserId "+currentUserId);

        mQueryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);


        mDatabaseReferenceBlogs.keepSynced(true);
        //is used for the offline purpose
        mDatabaseReferenceUser.keepSynced(true);


        mDatabaseReferenceUser.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String user_name = (String) dataSnapshot.child("name").getValue();
                System.out.println("Username" +user_name);
                String description = (String) dataSnapshot.child("description").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();


                mOtherProfileUsername.setText(user_name);
                mOtherProfileDescription.setText(description);

                Glide.with(OtherProfileActivity.this)
                        .load(post_image)
                        .into(mOtherProfilePagePic);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mBlogList.setLayoutManager(linearLayoutManager);


    }
    @Override
    protected void onStart() {
        super.onStart();




        FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, MainActivity.BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                MainActivity.BlogViewHolder.class,
                mQueryCurrentUser
        ) {


            @Override
            protected void populateViewHolder(MainActivity.BlogViewHolder viewHolder, Blog model, int position) {

                //getting the position url
                final String post_key = getRef(position).getKey();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());

                //setting on clicklistener
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(MainActivity.this,post_key,Toast.LENGTH_LONG).show();
                        Intent blogSingleIntent = new Intent(OtherProfileActivity.this,SingleBlogActivity.class);
                        //blog id is passed so by using it whole detail of the blog can be taken
                        blogSingleIntent.putExtra("blog_Id",post_key);
                        startActivity(blogSingleIntent);
                    }
                });



            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{


        View mView;

        ImageButton mLikeButton ;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mFirebaseAuth;



        public BlogViewHolder(View itemView) {

            super(itemView);
            mView = itemView ;
            mLikeButton = (ImageButton)mView.findViewById(R.id.likeButton);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mFirebaseAuth = FirebaseAuth.getInstance();

            mDatabaseLike.keepSynced(true);

        }


        public void setTitle(String title)
        {
            TextView post_title = (TextView)mView.findViewById(R.id.post_title);

            post_title.setText(title);
        }
        public void setDescription(String description)
        {
            TextView post_description = (TextView) mView.findViewById(R.id.post_description);
            post_description.setText(description);
        }

        public void setImage(Context context, String image)
        {
            ImageView post_image = (ImageView)mView.findViewById(R.id.post_image);
            //Picasso.with(context).load(image).into(post_image);
            Glide.with(context)
                    .load(image)
                    .into(post_image);

        }

        public void setUsername(String username)
        {
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
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
            startActivity(new Intent(OtherProfileActivity.this,MainActivity.class));
        }
        if(item.getItemId() == R.id.action_profile)
        {
            startActivity(new Intent(OtherProfileActivity.this,ProfileActivity.class));
        }
        if(item.getItemId() == R.id.action_forum)
        {
            startActivity(new Intent(OtherProfileActivity.this,MessageActivity.class));
        }



        return super.onOptionsItemSelected(item);
    }
}
