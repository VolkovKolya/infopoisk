package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Student;


import java.util.UUID;
public interface StudentsRepository extends JpaRepository<Student,UUID> {
    Student findByNameAndSurname(String name,String surname);
}
