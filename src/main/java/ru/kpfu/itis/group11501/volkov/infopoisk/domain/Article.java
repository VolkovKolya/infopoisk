package ru.kpfu.itis.group11501.volkov.infopoisk.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;


@Entity
@Table(name ="articles")
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Article {

    public static final String KEYWORD_DELIMITER = ";";

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(
            name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    UUID id;
    String title;
    //list of keyword with ';' delimiter
    String keywords;
    String content;
    String url;


    @ManyToOne
    @JoinColumn(name = "student_id")
    @Setter Student student;

}
