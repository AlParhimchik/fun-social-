package company.my.messager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by sasho on 30.01.2017.
 */

public class AutoRun extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            context.startService(new Intent(context,FoneService.class));
            Log.i("chat","+Autorun- wors");
        }
    }
}
