package org.example.sansam.notification.exception;

// NotificationTemplateNotFoundException.java
public class NotificationTemplateNotFoundException extends NotificationException {
    public NotificationTemplateNotFoundException(Long templateId) {
        super(String.format("알림 템플릿을 찾을 수 없습니다. (템플릿 ID: %d)", templateId));
    }
}
