package company.my.messager;

       import java.io.BufferedReader;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;

        import org.json.JSONArray;
        import org.json.JSONObject;

        import android.app.Notification;
       import android.app.NotificationManager;
       import android.app.PendingIntent;
        import android.app.Service;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.graphics.BitmapFactory;
       import android.graphics.Color;
       import android.os.IBinder;
        import android.util.Log;

public class FoneService extends Service {

    String server_name = "http://funmessenger.gear.host/Home/";
    public final String DB_NAME="db_users.db";
    public final String CREATE_DB="CREATE TABLE IF NOT EXISTS "+ Person.TABLE_NAME +
            " (_id integer primary key autoincrement,"+ Person.COLUMN_NAME_PASSWORD+","+ Person.COLUMN_NAME_LOGIN+","
            + Person.COLUMN_NAME_FIRST_NAME+","+ Person.COLUMN_NAME_LAST_NAME+")";
    SQLiteDatabase chatDBlocal;
    HttpURLConnection conn;
    Cursor cursor;
    Thread thr;
    ContentValues new_mess;
    int last_id;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent,int flags, int startId) {

        Log.i("chat", "+ FoneService - запуск сервиса");

        chatDBlocal = openOrCreateDatabase(DB_NAME,
                Context.MODE_PRIVATE, null);
        chatDBlocal.execSQL(CREATE_DB);
        Intent iN = new Intent(getApplicationContext(), newActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        PendingIntent pI = PendingIntent.getActivity(getApplicationContext(),
                0, iN, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder bI = new Notification.Builder(
                getApplicationContext());

        bI.setContentIntent(pI)
                .setSmallIcon( R.drawable.notif)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getApplicationContext()
                                .getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("работаю...");
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
       mNotifyMgr.notify(10,bI.build());
//        Notification notif=bI.build();
//
//        startForeground(1,notif);
        startLoop();
        return START_NOT_STICKY;

    }


    private void startLoop() {

        thr = new Thread(new Runnable() {

            // ansver = ответ на запрос
            // lnk = линк с параметрами
            String ansver, lnk;

            public void run() {

                while (true) { // стартуем бесконечный цикл

                    // глянем локальную БД на наличие сообщщений чата
                    cursor = chatDBlocal.rawQuery(
                            "SELECT * FROM chat ORDER BY id", null);

                    // если какие-либо сообщения есть - формируем запрос
                    // по которому получим только новые сообщения
                    if (cursor.moveToLast()) {
                        last_id = cursor.getInt(cursor
                                .getColumnIndex(Person._ID));
                        lnk = server_name + "/SelectUsers?id="
                                + last_id;


                    } else {
                        lnk = server_name + "/SelectUsers";
                    }

                    cursor.close();

                    // создаем соединение ---------------------------------->
                    try {
                        Log.i("chat",
                                "+ FoneService --------------- ОТКРОЕМ СОЕДИНЕНИЕ");

                        conn = (HttpURLConnection) new URL(lnk)
                                .openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.connect();

                    } catch (Exception e) {
                        Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                    }
                    // получаем ответ ---------------------------------->
                    try {
                        InputStream is = conn.getInputStream();
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(is, "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String bfr_st = null;
                        while ((bfr_st = br.readLine()) != null) {
                            sb.append(bfr_st);
                        }

                        Log.i("chat", "+ FoneService - полный ответ сервера:\n"
                                + sb.toString());
                        // сформируем ответ сервера в string
                        // обрежем в полученном ответе все, что находится за "]"
                        // это необходимо, т.к. json ответ приходит с мусором
                        // и если этот мусор не убрать - будет невалидным
                        ansver = sb.toString();
                        ansver = ansver.substring(0, ansver.indexOf("]") + 1);

                        is.close(); // закроем поток
                        br.close(); // закроем буфер

                    } catch (Exception e) {
                        Log.i("chat", "+ FoneService ошибка: " + e.getMessage());
                    } finally {
                        conn.disconnect();
                        Log.i("chat",
                                "+ FoneService --------------- ЗАКРОЕМ СОЕДИНЕНИЕ");
                    }

                    // запишем ответ в БД ---------------------------------->
                    if (ansver != null && !ansver.trim().equals("")) {

                        Log.i("chat",
                                "+ FoneService ---------- ответ содержит JSON:");

                        try {
                            // ответ превратим в JSON массив
                            JSONArray ja = new JSONArray(ansver);
                            JSONObject jo;

                            Integer i = 0;

                            while (i < ja.length()) {

                                // разберем JSON массив построчно
                                jo = ja.getJSONObject(i);

                                Log.i("chat",
                                        "=================>>> "
                                                + jo.getString("author")
                                                + " | "
                                                + jo.getString("client")
                                                + " | " + jo.getLong("data")
                                                + " | " + jo.getString("text"));

                                // создадим новое сообщение
                                new_mess = new ContentValues();
                                new_mess.put("author", jo.getString("author"));
                                new_mess.put("client", jo.getString("client"));
                                new_mess.put("data", jo.getLong("data"));
                                new_mess.put("text", jo.getString("text"));
                                // запишем новое сообщение в БД
                                chatDBlocal.insert("chat", null, new_mess);
                                new_mess.clear();

                                i++;

                                // отправим броадкаст для ChatActivity
                                // если она открыта - она обновить ListView
                                sendBroadcast(new Intent(
                                        "by.andreidanilevich.action.UPDATE_ListView"));
                            }
                        } catch (Exception e) {
                            // если ответ сервера не содержит валидный JSON
                            Log.i("chat",
                                    "+ FoneService ---------- ошибка ответа сервера:\n"
                                            + e.getMessage());
                        }
                    } else {
                        // если ответ сервера пустой
                        Log.i("chat",
                                "+ FoneService ---------- ответ не содержит JSON!");
                    }

                    try {
                        Thread.sleep(15000);
                    } catch (Exception e) {
                        Log.i("chat",
                                "+ FoneService - ошибка процесса: "
                                        + e.getMessage());
                    }
                }
            }
        });

        thr.setDaemon(true);
        thr.start();

    }
}