package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.WordsMyStem;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.WordsPorter;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticlesRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.WordsMyStemRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.WordsPorterRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WordsUploader {

    @NonNull WordsTransformer wordsTransformer;
    @NonNull ArticlesRepository articlesRepository;
    @NonNull WordsMyStemRepository wordsMyStemRepository;
    @NonNull WordsPorterRepository wordsPorterRepository;

    public void uploadWordsFromArticlesToDb() {
        articlesRepository.findAll()
                .forEach(this::uploadWordsFromArticleToDb);
    }

    private void uploadWordsFromArticleToDb(Article article) {
        List<String> words = getWordsFromArticle(article);
        words.stream()
                .map(word -> wordsTransformer.lemmatiseWithPorter(word))
                .map(word -> WordsPorter.builder()
                        .article(article)
                        .term(word)
                        .build())
                .forEach(wordsPorterRepository::save);
        words.stream()
                .map(word -> wordsTransformer.lemmatiseWithMyStem(word))
                .map(word -> WordsMyStem.builder()
                        .article(article)
                        .term(word)
                        .build())
                .forEach(wordsMyStemRepository::save);
    }

    private List<String> getWordsFromArticle(Article article) {
        return Stream.of(article.getContent(), article.getKeywords(), article.getTitle())
                .map(text -> wordsTransformer.getWordsFromText(text))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
