package fr.lina3d.glowquest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;

public class Reminders {
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("rem", Context.MODE_PRIVATE); }

    static void save(Context c, boolean on, String times) { prefs(c).edit().putBoolean("on", on).putString("times", times).apply(); }

    static void setDone(Context c, String day) { prefs(c).edit().putString("done", day).apply(); }

    static PendingIntent pi(Context c, int i) {
        Intent it = new Intent(c, ReminderReceiver.class);
        it.putExtra("i", i);
        return PendingIntent.getBroadcast(c, i, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void reschedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < 5; i++) am.cancel(pi(c, i));
        if (!prefs(c).getBoolean("on", false)) return;
        String[] t = prefs(c).getString("times", "").split(",");
        for (int i = 0; i < t.length && i < 5; i++) {
            String[] hm = t[i].trim().split(":");
            if (hm.length < 2) continue;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            if (cal.getTimeInMillis() <= System.currentTimeMillis() + 1000) cal.add(Calendar.DAY_OF_MONTH, 1);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi(c, i));
        }
    }
}
