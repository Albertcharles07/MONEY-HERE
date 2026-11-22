package com.moneyhere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;
interface Users extends JpaRepository<User,Long> {
 Optional<User> findByEmail(String email);
 boolean existsByEmail(String email);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select u from User u where u.email = :email")
 Optional<User> findLockedByEmail(@org.springframework.data.repository.query.Param("email") String email);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id")
 Optional<User> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
 org.springframework.data.domain.Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
  String name,String email,org.springframework.data.domain.Pageable pageable);
}
interface Tokens extends JpaRepository<VerificationToken,Long> {
 @EntityGraph(attributePaths="user")
 Optional<VerificationToken> findByTokenHashAndPurposeAndUsedFalse(String tokenHash,String purpose);
 void deleteByUserIdAndPurpose(Long userId,String purpose);
}
interface Goals extends JpaRepository<Goal,Long> {List<Goal> findByUserIdOrderByIdDesc(Long id);Optional<Goal> findByIdAndUserId(Long id,Long userId);}
interface Entries extends JpaRepository<Entry,Long> {List<Entry> findByUserIdOrderByEntryDateDescIdDesc(Long id);Optional<Entry> findByIdAndUserId(Long id,Long userId);}
interface Progresses extends JpaRepository<Progress,Long> {List<Progress> findByUserId(Long id);boolean existsByUserIdAndModuleKey(Long userId,String moduleKey);Optional<Progress> findByUserIdAndModuleKey(Long id,String key);}
interface Favorites extends JpaRepository<Favorite,Long> {List<Favorite> findByUserId(Long id); Optional<Favorite> findByUserIdAndContentKey(Long id,String key);}
