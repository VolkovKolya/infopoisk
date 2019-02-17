package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Student;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticlesRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.StudentsRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ArticleUploader {
    private static final String name = "nikolay";
    private static final String surname = "volkov";
    private static final String baseUrl = "https://bitblog.tech";
    private static final String[] urls = {
            baseUrl + "/2019/01/",
            baseUrl + "/2018/12/",
            baseUrl + "/2018/11/",
            baseUrl + "/2018/10/",
            baseUrl + "/2018/07/",
            baseUrl + "/2018/03/",
            baseUrl + "/2018/02/",
            baseUrl + "/2018/01/"};

    @NonNull ArticleExtractor articleExtractor;
    @NonNull ArticlesRepository articlesRepository;
    @NonNull StudentsRepository studentsRepository;

    @SneakyThrows
    public void uploadArticlesToDb() {
        Student student = studentsRepository.findByNameAndSurname(name, surname);

        Arrays.stream(urls)
                .map(articleExtractor::findArticles)
                .flatMap(Collection::stream)
                .map(articleExtractor::getArticleByUrl)
                .peek(article -> article.setStudent(student))
                .limit(30)
                .forEach(articlesRepository::save);
    }
}
