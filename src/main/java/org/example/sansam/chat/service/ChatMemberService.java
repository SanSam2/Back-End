package org.example.sansam.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.sansam.chat.repository.ChatMemberRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMemberService {

    private final ChatMemberRepository chatMemberRepository;

    @Async
    @Transactional
    public void updateLastReadAt(Long userId, Long roomId, LocalDateTime ts) {
        chatMemberRepository.updateLastReadAt(userId, roomId, ts);
    }
}
