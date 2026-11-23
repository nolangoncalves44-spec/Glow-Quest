package fr.lina3d.glowquest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    static final String[] MSG = {
        "Tes missions du jour t'attendent !",
        "Une petite quête avant de souffler ?",
        "Ta série compte sur toi !",
        "Encore quelques points pour valider ta journée !",
        "Ton perso a hâte de gagner des pièces !"
    };

    @Override
    public void onReceive(Context c, Intent intent) {
        int i = intent.getIntExtra("i", 0);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        boolean done = today.equals(Reminders.prefs(c).getString("done", ""));
        if (!done) {
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel("rappels", "Rappels", NotificationManager.IMPORTANCE_DEFAULT));
                b = new Notification.Builder(c, "rappels");
            } else {
                b = new Notification.Builder(c);
            }
            Intent open = new Intent(c, MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(c, 100, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            b.setSmallIcon(R.drawable.ic_stat).setContentTitle("Glow Quest").setContentText(MSG[i % MSG.length]).setContentIntent(pi).setAutoCancel(true);
            try { nm.notify(1, b.build()); } catch (SecurityException e) { }
        }
        Reminders.reschedule(c);
    }
}
