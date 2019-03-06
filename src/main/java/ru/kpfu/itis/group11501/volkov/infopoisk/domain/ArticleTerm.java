package ru.kpfu.itis.group11501.volkov.infopoisk.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "article_term")
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleTerm {

    @EmbeddedId
    ArticleTermIdentity articleTermIdentity;

    @Setter
    @Column(name = "\"tf-idf\"")
    Double tfIdf;

}
