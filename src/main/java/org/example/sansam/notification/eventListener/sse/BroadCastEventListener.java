//package org.example.sansam.notification.eventListener.sse;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.example.sansam.notification.event.sse.BroadcastEvent;
//import org.example.sansam.notification.service.NotificationService;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Log4j2
//@Component
//@RequiredArgsConstructor
//public class BroadCastEventListener {
//    private final NotificationService notificationService;
//
//    @EventListener
//    public void handleBroadcastEvent(BroadcastEvent event){
//        notificationService.sendBroadcastNotification(event.getEventName(), event.getPayloadJson());
//    }
//}
