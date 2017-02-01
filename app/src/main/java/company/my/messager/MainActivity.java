package company.my.messager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sasho on 01.02.2017.
 */

public class MainActivity extends AppCompatActivity implements  View.OnFocusChangeListener
{
    SharedPreferences mSettings;
    Cursor cursor;
    EditText login_form,password_form;
    SQLiteDatabase chatDBlocal;
    public final String DB_NAME="db_users.db";
    public final String CREATE_DB="CREATE TABLE IF NOT EXISTS "+ Person.TABLE_NAME +
            " (_id integer primary key autoincrement,"+ Person.COLUMN_NAME_PASSWORD+","+ Person.COLUMN_NAME_LOGIN+","
            + Person.COLUMN_NAME_FIRST_NAME+","+ Person.COLUMN_NAME_LAST_NAME+","+ Person.COLUMN_NAME_PHONE+")";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSettings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isSaved=mSettings.getBoolean(getString(R.string.SAVE_KEY),false);
        if (isSaved)

        {
            Intent intent=new Intent(this,newActivity.class);
            startActivity(intent);
        }
        login_form=(EditText)findViewById(R.id.login_form);
        password_form=(EditText)findViewById(R.id.password_form);
        login_form.setOnFocusChangeListener(this);
        password_form.setOnFocusChangeListener(this);


    }
    public void sing_in(View view)
    {
        if (!login_form.getText().toString().isEmpty() && !password_form.getText().toString().isEmpty())
        {
            chatDBlocal = openOrCreateDatabase(DB_NAME,
                    Context.MODE_PRIVATE, null);
            chatDBlocal.execSQL(CREATE_DB);
            cursor = chatDBlocal.query(Person.TABLE_NAME, null, Person.COLUMN_NAME_LOGIN + "=? AND "+Person.COLUMN_NAME_PASSWORD+"=?", new String[]{login_form.getText().toString(),password_form.getText().toString()}, null, null, null);
            if (cursor.moveToLast())
            {
                mSettings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=mSettings.edit();
                editor.putBoolean(getString(R.string.SAVE_KEY),true);
                editor.putString(Person.COLUMN_NAME_LOGIN,cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LOGIN)));
                editor.putString(Person.COLUMN_NAME_PASSWORD,cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PASSWORD)));
                editor.putString(Person.COLUMN_NAME_FIRST_NAME,cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_FIRST_NAME)));
                editor.putString(Person.COLUMN_NAME_LAST_NAME,cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LAST_NAME)));
                editor.putString(Person.COLUMN_NAME_PHONE,cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PHONE)));
                editor.apply();
                Intent intent=new Intent(this,newActivity.class);
                startActivity(intent);
            }
            else Toast.makeText(this,getString(R.string.SING_IN_ERROR_NO_NAME),Toast.LENGTH_LONG).show();

        }
        else
        Toast.makeText(this,getString(R.string.SING_IN_ERROR_EMPTY),Toast.LENGTH_LONG).show();
    }
    public void sing_up(View view)
    {
        RegFragmentDialog dialog= new RegFragmentDialog(this);
        dialog.getWindow().getAttributes().windowAnimations=R.style.RegistrationDialogAnimation;
        dialog.show();
    }
    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b)
        {
            hideKeyboard(view);
        }
    }
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
