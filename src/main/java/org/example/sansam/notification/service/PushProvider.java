package org.example.sansam.notification.service;

public interface PushProvider {

    void sendPushNotification(String title, String message);

    void sendEmailNotification(String title, String message);

    void connect();

    void disconnect();
}
