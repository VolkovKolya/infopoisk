package ru.kpfu.itis.group11501.volkov.infopoisk.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name ="words_mystem")
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordsMyStem {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(
            name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    UUID id;
    String term;
    @ManyToOne
    @JoinColumn(name = "article_id")
    Article article;
}
