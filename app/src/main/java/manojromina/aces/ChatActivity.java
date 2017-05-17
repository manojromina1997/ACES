package manojromina.aces;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
            startActivity(new Intent(ChatActivity.this,MainActivity.class));
        }
        if(item.getItemId() == R.id.action_profile)
        {
            startActivity(new Intent(ChatActivity.this,ProfileActivity.class));
        }
        if(item.getItemId() == R.id.action_forum)
        {
            startActivity(new Intent(ChatActivity.this,MessageActivity.class));
        }



        return super.onOptionsItemSelected(item);
    }
}
