package br.com.lucas.valli.fluxodecaixa.Classes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import br.com.lucas.valli.fluxodecaixa.Atividades.ContasAReceber;
import br.com.lucas.valli.fluxodecaixa.R;

public class AlarmReceber extends BroadcastReceiver {
    private static final String CHANNEL_ID = "canal_de_notificacoes_r";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Obter o título e o texto da notificação dos extras do intent
        String text = intent.getStringExtra("notification_text_r");

        // Crie e exiba a notificação com o título e o texto obtidos
        createNotification(context, text);
    }

    private void createNotification(Context context, String text) {
        // Configurar a intenção para abrir o aplicativo quando a notificação for clicada
        Intent resultIntent = new Intent(context, ContasAReceber.class);

        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, flags);

        // Criar a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_contas_a_receber)
                .setContentTitle("Lembrete de recebimento")
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Exibir a notificação
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Cria o canal de notificação para dispositivos Android Oreo e superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            // Dentro do bloco if, depois de criar o canal de notificação
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
