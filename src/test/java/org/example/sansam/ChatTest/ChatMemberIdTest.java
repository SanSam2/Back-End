package org.example.sansam.ChatTest;

import org.example.sansam.chat.domain.ChatMemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMemberIdTest {

    @Test
    @DisplayName("ChatMemberId equals: 동일 객체")
    void equals_sameObject() {
        ChatMemberId id = new ChatMemberId(1L, 2L);
        assertTrue(id.equals(id));
    }

    @Test
    @DisplayName("ChatMemberId equals: 동일 값")
    void equals_sameValues() {
        ChatMemberId id1 = new ChatMemberId(1L, 2L);
        ChatMemberId id2 = new ChatMemberId(1L, 2L);
        assertTrue(id1.equals(id2));
        assertTrue(id2.equals(id1));
    }

    @Test
    @DisplayName("ChatMemberId equals: 다른 값")
    void equals_differentValues() {
        ChatMemberId id1 = new ChatMemberId(1L, 2L);
        ChatMemberId id2 = new ChatMemberId(1L, 3L);
        ChatMemberId id3 = new ChatMemberId(2L, 2L);

        assertFalse(id1.equals(id2));
        assertFalse(id1.equals(id3));
    }

    @Test
    @DisplayName("ChatMemberId equals: null 및 다른 클래스")
    void equals_nullOrDifferentClass() {
        ChatMemberId id = new ChatMemberId(1L, 2L);

        assertFalse(id.equals(null));
        assertFalse(id.equals("string object"));
    }
}