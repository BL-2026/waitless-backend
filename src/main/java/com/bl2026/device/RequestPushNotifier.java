package com.bl2026.device;

import com.bl2026.request.ServiceRequestChangedEvent;
import com.bl2026.request.ServiceRequestEvent;
import com.bl2026.request.ServiceRequestResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Delivers floor-call pushes. Wording comes from {@link FloorCallCopy} using the
 * locale stored on each {@link DeviceToken}; this class only builds the FCM envelope.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestPushNotifier {

    private static final String CHANNEL_ID = "floor_calls";

    private final DeviceTokenService deviceTokenService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onServiceRequestChanged(ServiceRequestChangedEvent event) {
        if (event.type() != ServiceRequestEvent.Type.CREATED) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            return;
        }

        ServiceRequestResponse request = event.request();
        for (DeviceToken device : deviceTokenService.forStore(request.storeId())) {
            send(device, request);
        }
    }

    private void send(DeviceToken device, ServiceRequestResponse request) {
        String title = FloorCallCopy.title(device.getLocale());
        String body = FloorCallCopy.body(device.getLocale(), request.type(), request.tableNumber());

        Message message = Message.builder()
                .setToken(device.getToken())
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putData("requestId", request.id().toString())
                .putData("storeId", request.storeId().toString())
                .putData("tableNumber", String.valueOf(request.tableNumber()))
                .putData("type", request.type().name())
                .putData("locale", device.getLocale())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId(CHANNEL_ID)
                                .setSound("default")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder().setSound("default").setBadge(1).build())
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException ex) {
            log.warn("FCM send failed: {} — {}", ex.getMessagingErrorCode(), ex.getMessage());
            // Only UNREGISTERED means this token is dead. INVALID_ARGUMENT is often a
            // payload/API mistake — do not delete a working phone because of that.
            if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                deviceTokenService.dropToken(device.getToken());
            }
        }
    }
}
