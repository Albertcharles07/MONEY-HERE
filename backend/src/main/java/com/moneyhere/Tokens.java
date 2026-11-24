package com.moneyhere;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface Tokens extends JpaRepository<VerificationToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<VerificationToken> findByTokenHashAndPurposeAndUsedFalse(String tokenHash, String purpose);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndPurpose(Long userId, String purpose);
}
