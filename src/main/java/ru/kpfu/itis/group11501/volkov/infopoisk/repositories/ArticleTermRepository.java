package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.ArticleTerm;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.ArticleTermIdentity;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Term;

import java.util.List;

public interface ArticleTermRepository extends JpaRepository<ArticleTerm, ArticleTermIdentity> {
    List<ArticleTerm> findById_Term(Term term);
}
