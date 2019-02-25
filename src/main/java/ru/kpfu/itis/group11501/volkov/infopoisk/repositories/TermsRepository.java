package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Term;

import java.util.Optional;
import java.util.UUID;

public interface TermsRepository extends JpaRepository<Term, UUID> {
    Optional<Term> findByText(String text);
}
