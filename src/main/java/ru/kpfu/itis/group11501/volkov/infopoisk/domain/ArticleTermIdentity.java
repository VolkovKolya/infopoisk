package ru.kpfu.itis.group11501.volkov.infopoisk.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleTermIdentity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "article_id")
    Article article;

    @ManyToOne
    @JoinColumn(name = "term_id")
    Term term;
}
