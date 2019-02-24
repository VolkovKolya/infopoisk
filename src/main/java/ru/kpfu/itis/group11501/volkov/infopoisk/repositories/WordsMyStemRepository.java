package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.WordsMyStem;

import java.util.UUID;

public interface WordsMyStemRepository extends CrudRepository<WordsMyStem, UUID> {
}
