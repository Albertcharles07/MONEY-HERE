package com.moneyhere;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
interface Goals extends JpaRepository<Goal,Long> {List<Goal> findByUserIdOrderByIdDesc(Long id);Optional<Goal> findByIdAndUserId(Long id,Long userId);}
interface Entries extends JpaRepository<Entry,Long> {List<Entry> findByUserIdOrderByEntryDateDescIdDesc(Long id);Optional<Entry> findByIdAndUserId(Long id,Long userId);List<Entry> findByUserIdAndEntryDateBetween(Long id,LocalDate start,LocalDate end);}
interface Progresses extends JpaRepository<Progress,Long> {List<Progress> findByUserId(Long id);boolean existsByUserIdAndModuleKey(Long userId,String moduleKey);Optional<Progress> findByUserIdAndModuleKey(Long id,String key);}
interface Favorites extends JpaRepository<Favorite,Long> {List<Favorite> findByUserId(Long id); Optional<Favorite> findByUserIdAndContentKey(Long id,String key);}
