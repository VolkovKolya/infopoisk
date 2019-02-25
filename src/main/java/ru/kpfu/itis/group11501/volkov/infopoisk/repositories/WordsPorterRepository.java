package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.WordsPorter;

import java.util.UUID;

public interface WordsPorterRepository extends JpaRepository<WordsPorter, UUID> {
}
