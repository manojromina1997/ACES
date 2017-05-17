package manojromina.aces;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    //database reference
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReferenceBlogs;
    private DatabaseReference mDatabaseReferenceUser;
    private DatabaseReference mDatabaseReferenceLike;

    //for firebase authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //for now the post are dislike
    private boolean mProcessLike = false;

    //connecting with add a post
    private Button mAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddPost =(Button) findViewById(R.id.addPost);

        mAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,PostActivity.class));
            }
        });


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReferenceBlogs = mFirebaseDatabase.getReference().child("Blogs");
        mDatabaseReferenceUser = mFirebaseDatabase.getReference().child("Users");
        mDatabaseReferenceLike = mFirebaseDatabase.getReference().child("Likes");

        mDatabaseReferenceBlogs.keepSynced(true);
        //is used for the offline purpose
        mDatabaseReferenceUser.keepSynced(true);

        //firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();



        //auth state listener is use to check whether the user has signed in or not

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //if the current user is null then
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Intent loginIntent = new Intent(MainActivity.this ,LoginActivity.class);
                    //if the user is not signed in then he can not go to back or any other activity without sign in
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(loginIntent);

                }

            }
        };

        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mBlogList.setLayoutManager(linearLayoutManager);

        chekUserExists();

    }

    @Override
    protected void onStart() {
        super.onStart();


        mFirebaseAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabaseReferenceBlogs
        ) {


            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                //getting the position url
                final String post_key = getRef(position).getKey();


                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(),model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setLikeButton(post_key);

                //setting on clicklistener
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(MainActivity.this,post_key,Toast.LENGTH_LONG).show();
                        Intent blogSingleIntent = new Intent(MainActivity.this,SingleBlogActivity.class);
                        //blog id is passed so by using it whole detail of the blog can be taken
                        blogSingleIntent.putExtra("blog_Id",post_key);
                        startActivity(blogSingleIntent);
                    }
                });

                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mProcessLike = true ;

                        final String currentUserId = mFirebaseAuth.getCurrentUser().getUid();

                        //when like is pressed
                        mDatabaseReferenceLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mFirebaseAuth.getCurrentUser().getUid())) {

                                        //dislike
                                        mDatabaseReferenceLike.child(post_key).child(mFirebaseAuth.getCurrentUser().getUid()).removeValue();

                                        //once the like is pressed the button should be false
                                        mProcessLike = false;
                                    } else {

                                        //get the value of the post and add data to the database
                                        mDatabaseReferenceLike.child(post_key).child(mFirebaseAuth.getCurrentUser().getUid()).setValue(currentUserId);

                                        //once the like is pressed the button should be false
                                        mProcessLike = false;
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

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
        public void setLikeButton(final String post_key)
        {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //if like is pressed
                    if(dataSnapshot.child(post_key).hasChild(mFirebaseAuth.getCurrentUser().getUid()))
                    {
                        mLikeButton.setImageResource(R.mipmap.ic_favorite_black_24dp);

                    }
                    //if like is not pressed
                    else
                    {
                        mLikeButton.setImageResource(R.mipmap.ic_favorite_border_black_24dp);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
    //So this method is use to check whether the user data is present in our database or not
    //if user has signed up using normal email without using the facebook or gmail then their data is present on the database
    //if the user has signed in by using the gmail or facebook then there data is not present on the database
    //so for such user we should ask about more additional details like full name , profile photo and other things
    private void chekUserExists() {

        if(mFirebaseAuth.getCurrentUser()!= null) {

            final String user_id = mFirebaseAuth.getCurrentUser().getUid();

            //this will check whether user is in the databse or not
            mDatabaseReferenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //if user is there then go to main activity
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
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






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_home)
        {
            startActivity(new Intent(MainActivity.this,MainActivity.class));
        }
        if(item.getItemId() == R.id.action_profile)
        {
            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
        }
        if(item.getItemId() == R.id.action_forum)
        {
            startActivity(new Intent(MainActivity.this,MessageActivity.class));
        }

        if(item.getItemId() == R.id.action_logout)
        {
            logout();
        }



        return super.onOptionsItemSelected(item);
    }

    //when logout is clicked
    private void logout() {
        mFirebaseAuth.signOut();
    }

}
