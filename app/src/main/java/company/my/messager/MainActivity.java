package company.my.messager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sasho on 01.02.2017.
 */

public class MainActivity extends AppCompatActivity implements  View.OnFocusChangeListener
{
    String server_name="http://funmessenger.gear.host/Home";
    SharedPreferences mSettings;
    Cursor cursor;
    EditText login_form,password_form;
    SQLiteDatabase chatDBlocal;
    public final String DB_NAME="db_users.db";
    public final String CREATE_DB="CREATE TABLE IF NOT EXISTS "+ Person.TABLE_NAME +
            " (_id integer primary key autoincrement,"+ Person.COLUMN_NAME_PASSWORD+","+ Person.COLUMN_NAME_LOGIN+","
            + Person.COLUMN_NAME_FIRST_NAME+","+ Person.COLUMN_NAME_LAST_NAME+")";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSettings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isSaved=mSettings.getBoolean(getString(R.string.SAVE_KEY),false);
        if (isSaved)

        {
            new SingInTask().execute(mSettings.getString(Person.COLUMN_NAME_LOGIN,""),mSettings.getString(Person.COLUMN_NAME_PASSWORD,""));
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
            new SingInTask().execute(login_form.getText().toString(),password_form.getText().toString());

        }
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
    public class SingInTask extends AsyncTask<String,String,String>
    {
        HttpURLConnection conn;
        String url_string;
        @Override
        protected String doInBackground(String... strings) {
            String result="error";
            Integer result_code=0;
            try {
                url_string=server_name+"/SingIn?login="+strings[0]+"&password="+strings[1];
                URL url=new URL(url_string);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();
                result_code = conn.getResponseCode();
                if (result_code==200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String bfr_st = null;
                    while ((bfr_st = br.readLine()) != null) {
                        sb.append(bfr_st);
                    }
                    result = sb.toString();
                    is.close(); // закроем поток
                    br.close(); // закроем буфер
                }


            } catch (Exception e)
            {
                Log.i("TAG", e.getMessage().toString());
            } finally
            {
                conn.disconnect();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            super.onPostExecute(s);
            if (s.equals("error"))
            {
                Toast.makeText(getBaseContext(),"error when sing_in",Toast.LENGTH_LONG).show();

            }
            else
            if (s.equalsIgnoreCase("True"))
            {
                chatDBlocal = openOrCreateDatabase(DB_NAME,
                    Context.MODE_PRIVATE, null);
                chatDBlocal.execSQL(CREATE_DB);
                cursor = chatDBlocal.query(Person.TABLE_NAME, null, Person.COLUMN_NAME_LOGIN + "=? AND "+Person.COLUMN_NAME_PASSWORD+"=?", new String[]{login_form.getText().toString(),password_form.getText().toString()}, null, null, null);
                if (cursor.moveToLast()) {
                    mSettings = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean(getString(R.string.SAVE_KEY), true);
                    editor.putString(Person.COLUMN_NAME_LOGIN, cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LOGIN)));
                    editor.putString(Person.COLUMN_NAME_PASSWORD, cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PASSWORD)));
                    editor.putString(Person.COLUMN_NAME_FIRST_NAME, cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_FIRST_NAME)));
                    editor.putString(Person.COLUMN_NAME_LAST_NAME, cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LAST_NAME)));
                    editor.apply();
                    Intent intent = new Intent(getBaseContext(), newActivity.class);
                    startActivity(intent);
                }
            }
            else
            {
                Toast.makeText(getBaseContext(),"no user with such login and password",Toast.LENGTH_LONG).show();
            }
        }
    }
    public void deleteDB(View view)
    {
        chatDBlocal = openOrCreateDatabase(DB_NAME,
                Context.MODE_PRIVATE, null);
        chatDBlocal.execSQL(CREATE_DB);
        cursor = chatDBlocal.query(Person.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst())
        {
            do
            {
                String password=cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PASSWORD));
                String first=cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_FIRST_NAME));
                String last=cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LAST_NAME));
                String login=cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_LOGIN));
                int id=cursor.getInt(cursor.getColumnIndex(Person._ID));
                Log.i("TAG",password+" "+login+ " "+first+" "+last+" "+id);
            }while (cursor.moveToNext());
//            chatDBlocal.execSQL("detele from "+Person.TABLE_NAME);
            chatDBlocal.delete(Person.TABLE_NAME,null,null);
            chatDBlocal.close();
        }


    }

}


