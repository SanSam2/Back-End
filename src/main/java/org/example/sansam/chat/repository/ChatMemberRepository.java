package org.example.sansam.chat.repository;

import org.example.sansam.chat.domain.ChatMember;
import org.example.sansam.chat.domain.ChatMemberId;
import org.example.sansam.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, ChatMemberId> {
    List<ChatMember> findAllByIdUserId(Long userId);
}
