package org.example.sansam.ChatTest;

import org.example.sansam.chat.repository.ChatMemberRepository;
import org.example.sansam.chat.service.ChatMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class ChatMemberServiceTest {

    @Mock
    private ChatMemberRepository chatMemberRepository;

    @InjectMocks
    private ChatMemberService chatMemberService;

    public ChatMemberServiceTest() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    @DisplayName("updateLastReadAt - 올바른 userId, roomId, 시간으로 repository 메서드가 호출되어야 함")
    void updateLastReadAt_callsRepositoryWithCorrectParameters() {
        // given
        Long userId = 1L;
        Long roomId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // when
        chatMemberService.updateLastReadAt(userId, roomId, now);

        // then
        verify(chatMemberRepository, times(1)).updateLastReadAt(userId, roomId, now);
    }
}