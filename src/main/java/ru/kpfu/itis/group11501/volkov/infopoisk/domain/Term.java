package ru.kpfu.itis.group11501.volkov.infopoisk.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "terms_list")
@Getter
@Builder
@FieldDefaults(level = PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Term {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(
            name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "term_id")
    UUID id;
    @Column(name = "term_text")
    String text;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE
    })
    @JoinTable(name = "article_term",
            joinColumns = @JoinColumn(name = "term_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    List<Article> articles;

}
