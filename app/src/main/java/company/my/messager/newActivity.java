package company.my.messager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by sasho on 30.01.2017.
 */

public class newActivity extends AppCompatActivity
{
    User cur_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_users);
        storeUserDate();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(cur_user.first_name+" "+cur_user.last_name);


    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_layout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId()==R.id.out)
        {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPref.edit();
            editor.putBoolean("isSaved",false);
            editor.apply();
            finish();
        }
        return true;
    }

    public  void storeUserDate()
    {
        SharedPreferences mSettings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        User cur_user=new User();
        cur_user.first_name=mSettings.getString(Person.COLUMN_NAME_FIRST_NAME,"");
        cur_user.last_name=mSettings.getString(Person.COLUMN_NAME_LAST_NAME,"");
        cur_user.password=mSettings.getString(Person.COLUMN_NAME_PASSWORD,"");
        cur_user.login=mSettings.getString(Person.COLUMN_NAME_LOGIN,"");
    }
}
