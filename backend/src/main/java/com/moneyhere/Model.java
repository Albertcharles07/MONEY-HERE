package com.moneyhere;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

public final class Model { private Model(){} }
@Entity @Table(name="app_users") @org.hibernate.annotations.DynamicUpdate class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false,unique=true) public String email;
 @Column(name="password_hash",nullable=false) public String passwordHash;
 public boolean verified=false;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) public AccountRole role=AccountRole.USER;
 @Column(name="primary_admin",nullable=false,updatable=false) public boolean primaryAdmin;
 @Column(nullable=false) public boolean enabled=true;
 @Column(name="must_change_password",nullable=false) public boolean mustChangePassword=false;
 @Column(name="session_version",nullable=false) public int sessionVersion;
 public String goal; public String level;
 @Column(name="learning_format") public String learningFormat;
 public String theme="system"; public boolean notifications=true;
 @Column(name="created_at") public Instant createdAt=Instant.now();
}
enum AccountRole { USER, ADMIN }
@Entity @Table(name="verification_tokens") class VerificationToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") public User user;
 @Column(name="token_hash") public String tokenHash;
 public String purpose;
 @Column(name="expires_at") public Instant expiresAt;
 public boolean used=false;
}
@Entity @Table(name="financial_goals") class Goal {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") public User user;
 public String title;
 @Column(name="target_amount") public BigDecimal targetAmount;
 @Column(name="current_amount") public BigDecimal currentAmount=BigDecimal.ZERO;
 public LocalDate deadline;
 @Column(name="created_at") public Instant createdAt=Instant.now();
}
@Entity @Table(name="budget_entries") class Entry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") public User user;
 public String title; public String category; public String kind;
 public BigDecimal amount;
 @Column(name="entry_date") public LocalDate entryDate;
}
@Entity @Table(name="learning_progress") class Progress {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") public User user;
 @Column(name="module_key") public String moduleKey;
 @Column(name="completed_at") public Instant completedAt=Instant.now();
}
@Entity @Table(name="favorites") class Favorite {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") public User user;
 @Column(name="content_key") public String contentKey;
}
