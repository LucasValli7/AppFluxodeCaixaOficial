package br.com.lucas.valli.fluxodecaixa;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyfirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        // Recupera os dados personalizados da mensagem
        String screen = remoteMessage.getData().get("screen");

        // Determina qual tela abrir com base nos dados recebidos
        Class<?> targetActivity = null;
        if (screen != null) {
            switch (screen) {
                // Adicione mais casos conforme necessário para outras telas
                case "TelaSobre":
                    targetActivity = TelaSobre.class;
                    break;
                case "TelaPrincipal":
                    targetActivity = TelaPrincipal.class;
                    break;
                case "TelaSaida":
                    targetActivity = TelaPrincipalSaidas.class;
                    break;
                case "TelaEntrada":
                    targetActivity = TelaPrincipalEntradas.class;
                    break;
                case "TelaHistorico":
                    targetActivity = PerfilHistoricos.class;
                    break;
                case "TelaContasPagar":
                    targetActivity = ContasAPagar.class;
                    break;


            }
        }
        if (targetActivity != null) {
            // Cria um Intent para abrir a tela correspondente
            Intent intent = new Intent(this, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Cria a notificação
            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                            .setSmallIcon(R.drawable.ic_add_money_icon)
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Cria o canal de notificação se estiver executando no Android Oreo ou superior
                NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            // Exibe a notificação
            notificationManager.notify(0, notificationBuilder.build());

        }
    }
}
