package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.*;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class WordsUploader {

    @NonNull WordsTransformer wordsTransformer;
    @NonNull ArticlesRepository articlesRepository;
    @NonNull WordsMyStemRepository wordsMyStemRepository;
    @NonNull WordsPorterRepository wordsPorterRepository;
    @NonNull TermsRepository termsRepository;
    @NonNull ArticleTermRepository articleTermRepository;

    @Transactional
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
                .collect(toList());
    }

    @Transactional
    public void uploadWordsPorterToTerms() {
        Map<String, Set<Article>> map = wordsPorterRepository.findAll()
                .stream()
                .collect(groupingBy(
                        WordsPorter::getTerm,
                        mapping(WordsPorter::getArticle, toSet())
                        )
                );
        List<Term> terms = map.entrySet().stream()
                .map(entry -> Term.builder()
                        .articles(new ArrayList<>(entry.getValue()))
                        .text(entry.getKey())
                        .build())
                .sorted(Comparator.comparing(Term::getText))
                .collect(toList());
        termsRepository.saveAll(terms);
    }

    @Transactional
    public void uploadTfIdfToTerms() {
        Map<Article, List<String>> map = wordsPorterRepository.findAll()
                .stream()
                .collect(groupingBy(
                        WordsPorter::getArticle,
                        mapping(WordsPorter::getTerm, toList())
                        )
                );

        articleTermRepository.findAll().forEach(articleTerm -> {
            final Term term = articleTerm.getId().getTerm();
            final Article article = articleTerm.getId().getArticle();

            final List<String> articleTerms = map.get(article);
            final double tf = (double) articleTerms.stream()
                    .filter(value -> value.equals(term.getText()))
                    .count() / articleTerms.size();
            final double idf = (double) map.keySet().size() /
                    map.values().stream()
                            .filter(list -> list.contains(term.getText()))
                            .count();
            articleTerm.setTfIdf(tf * Math.log(idf));
        });

    }
}
