package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import javafx.util.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Term;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.WordsPorter;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticleTermRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticlesRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.TermsRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.WordsPorterRepository;

import java.util.*;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class Bm25Search {
    @NonNull WordsTransformer wordsTransformer;
    @NonNull ArticleTermRepository articleTermRepository;
    @NonNull TermsRepository termsRepository;
    @NonNull ArticlesRepository articlesRepository;
    @NonNull WordsPorterRepository wordsPorterRepository;

    @Transactional
    /**
     * Find articles url, that contains at least one word in query,
     * calculate bm25 measure with query and return top 10
     * Realization use PorterStemmer as stemmer.
     *
     * @param query incoming query text
     * @return list of articles url with cosine measure
     */
    public List<Pair<String, Double>> searchText(String query) {
        //extract term from query
        final List<Term> queryTerms = wordsTransformer.getWordsFromText(query)
                .stream()
                .map(wordsTransformer::lemmatiseWithPorter)
                .map(termsRepository::findByText)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        Map<Article, List<String>> articleWords = wordsPorterRepository.findAll()
                .stream()
                .collect(groupingBy(
                        WordsPorter::getArticle,
                        mapping(WordsPorter::getTerm, toList())
                        )
                );


        //count how many times each term contains in each article
        final Map<Article, Map<Term, Long>> map = queryTerms.stream()
                .flatMap(term -> articleTermRepository.findById_Term(term).stream())

                .collect(groupingBy(
                        articleTerm -> articleTerm.getId().getArticle(),
                        toMap(articleTerm -> articleTerm.getId().getTerm(),
                                articleTerm -> {
                                    Article article = articleTerm.getId().getArticle();
                                    Term term = articleTerm.getId().getTerm();
                                    long count = articleWords.get(article).stream()
                                            .filter(word -> word.equals(term.getText()))
                                            .count();
                                    return count;

                                }
                        )
                        )
                );

        final long allSize = articlesRepository.count();
        final double avgSize = articleWords.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0d);

        Map<Term, Double> termIdf = new HashMap<>(queryTerms.size());
        queryTerms.stream()
                .forEach(term -> {
                    long containsTermSize = map.values().stream()
                            .filter(articleMap -> articleMap.containsKey(term))
                            .count();

                    double idf = Math.log((double) allSize - containsTermSize + 0.5 / (containsTermSize + 0.5));
                    idf = Math.max(idf, 0);
                    termIdf.put(term, idf);

                });

        final List<Pair<String, Double>> result = map.entrySet().stream()
                //map to pair of article url and terms vector
                .map(entry -> {
                    double sum = 0;

                    for (Term term : queryTerms) {
                        long times = Optional.ofNullable(entry.getValue().get(term)).orElse(0L);
                        double bm25Measure = termIdf.get(term) * (times * (Bm25.K1 + 1) /
                                (times + Bm25.K1 * (1 - Bm25.B + Bm25.B * (articleWords.get(entry.getKey()).size()) / avgSize))
                        );
                        sum += bm25Measure;
                    }

                    return new Pair<String, Double>(entry.getKey().getUrl(), sum);
                })

                .sorted(Comparator.comparing(Pair<String, Double>::getValue).reversed())
                .limit(10)

                .collect(toList());

        return result;


    }


}


class Bm25 {
    public static final Double K1 = 1.2;
    public static final Double B = 0.75;
}
