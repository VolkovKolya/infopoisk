package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import javafx.util.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.ArticleTerm;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Term;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticleTermRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.ArticlesRepository;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.TermsRepository;

import java.util.*;


import static java.util.stream.Collectors.*;


@Service
@RequiredArgsConstructor
public class CosineSearch {

    @NonNull WordsTransformer wordsTransformer;
    @NonNull ArticleTermRepository articleTermRepository;
    @NonNull TermsRepository termsRepository;
    @NonNull ArticlesRepository articlesRepository;


    @Transactional
    /**
     * Find articles url, that contains at least one word in query,
     * calculate cosine measure with query and return top 10
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

        //find ArticleTerms group by Article which contains any term, and map it to convenient view
        final Map<Article, Map<Term, Double>> map = queryTerms.stream()
                .flatMap(term -> articleTermRepository.findById_Term(term).stream())

                .collect(groupingBy(
                        articleTerm -> articleTerm.getId().getArticle(),
                        toMap(articleTerm -> articleTerm.getId().getTerm(),
                                ArticleTerm::getTfIdf)
                        )
                );


        //build vectors
        final long allSize = articlesRepository.count();
        List<Double> queryVector = queryTerms.stream()
                .map(term -> {
                    long containsTermSize = map.values().stream()
                            .filter(articleMap -> articleMap.containsKey(term))
                            .count();
                    if (containsTermSize != 0) {
                        return Math.log((double) allSize / containsTermSize);
                    } else {
                        return 0d;
                    }
                })
                .collect(toList());

        final List<Pair<String, Double>> result = map.entrySet().stream()
                //map to pair of article url and terms vector
                .map(entry -> {
                    List<Double> vector = new ArrayList<>(queryVector.size());
                    for (Term term : queryTerms) {
                        Optional<Double> optional = Optional.ofNullable(entry.getValue().get(term));
                        vector.add(optional.orElse(0d));
                    }

                    return new Pair<String, List<Double>>(entry.getKey().getUrl(), vector);
                })
                //map to result
                .map(pair -> new Pair<String, Double>(pair.getKey(), cosineMeasure(queryVector, pair.getValue())))

                .sorted(Comparator.comparing(Pair<String, Double>::getValue).reversed())
                .limit(10)

                .collect(toList());

        return result;
    }


    public static Double cosineMeasure(List<Double> list1, List<Double> list2) {
        double sum = 0d;
        double len1 = 0d;
        double len2 = 0d;
        final Iterator<Double> iterator1 = list1.iterator();
        final Iterator<Double> iterator2 = list2.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            double val1 = iterator1.next();
            double val2 = iterator2.next();
            len1 += val1 * val1;
            len2 += val2 * val2;
            sum += val1 * val2;
        }
        while (iterator1.hasNext()) {
            double val = iterator1.next();
            len1 += val * val;
        }
        while (iterator2.hasNext()) {
            double val = iterator2.next();
            len2 += val * val;
        }

        return sum / (Math.sqrt(len1) * Math.sqrt(len2));
    }
}
