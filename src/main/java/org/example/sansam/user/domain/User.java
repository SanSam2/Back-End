package org.example.sansam.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50, nullable=false, unique=true)
    private String email;

    @Column(length=50, nullable=false)
    private String name;

    @Column(length=100, nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Column(nullable = true)
    private Long salary;

    @Column(name = "mobile_number",length=11)
    private String mobileNumber;

    @Column(nullable = true)
    private Boolean activated;

    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "email_agree",nullable=false)
    private Boolean emailAgree;
}
