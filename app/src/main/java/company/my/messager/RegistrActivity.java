package company.my.messager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sasho on 31.01.2017.
 */

public class RegistrActivity extends AppCompatActivity implements View.OnFocusChangeListener
{
    Cursor cursor;
    EditText login_form,pass_form,first_name_form,last_name_form,phone_form;
    SQLiteDatabase chatDBlocal;
    public final String DB_NAME="db_users.db";
    public final String CREATE_DB="CREATE TABLE IF NOT EXISTS "+ Person.TABLE_NAME +
            " (_id integer primary key autoincrement,"+ Person.COLUMN_NAME_PASSWORD+","+ Person.COLUMN_NAME_LOGIN+","
            + Person.COLUMN_NAME_FIRST_NAME+","+ Person.COLUMN_NAME_LAST_NAME+","+ Person.COLUMN_NAME_PHONE+")";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registr_layout);
        login_form=(EditText)findViewById(R.id.login_form_reg); login_form.setOnFocusChangeListener(this);
        pass_form=(EditText)findViewById(R.id.password_form_reg); login_form.setOnFocusChangeListener(this);
        first_name_form=(EditText)findViewById(R.id.first_name); login_form.setOnFocusChangeListener(this);
        last_name_form=(EditText)findViewById(R.id.last_name); login_form.setOnFocusChangeListener(this);
        phone_form=(EditText)findViewById(R.id.phone); login_form.setOnFocusChangeListener(this);
    }
    public void sing_up(View view)
    {
        chatDBlocal = openOrCreateDatabase(DB_NAME,
                Context.MODE_PRIVATE, null);
        chatDBlocal.execSQL(CREATE_DB);
        cursor = chatDBlocal.query(Person.TABLE_NAME, null, Person.COLUMN_NAME_LOGIN + "=?", new String[]{login_form.getText().toString()}, null, null, null);
        if (cursor.moveToFirst())
        {
            Toast.makeText(this,"have such user",Toast.LENGTH_LONG);
            Log.i("TAG", "user not added");
        }
        else {
            ContentValues new_user = new ContentValues();
            new_user.put(Person.COLUMN_NAME_LOGIN, login_form.getText().toString());
            new_user.put(Person.COLUMN_NAME_PASSWORD, pass_form.getText().toString());
            new_user.put(Person.COLUMN_NAME_FIRST_NAME, first_name_form.getText().toString());
            new_user.put(Person.COLUMN_NAME_LAST_NAME, last_name_form.getText().toString());
            new_user.put(Person.COLUMN_NAME_PHONE, phone_form.getText().toString());
            chatDBlocal.insert(Person.TABLE_NAME, null, new_user);
            Log.i("TAG", "user added");
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            cursor.close();

        }
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
