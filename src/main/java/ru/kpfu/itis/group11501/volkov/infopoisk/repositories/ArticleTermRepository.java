package ru.kpfu.itis.group11501.volkov.infopoisk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.ArticleTerm;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.ArticleTermIdentity;

public interface ArticleTermRepository  extends JpaRepository<ArticleTerm, ArticleTermIdentity> {
}
