package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
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
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class SingularDecompositionSearch {

    @NonNull WordsTransformer wordsTransformer;
    @NonNull ArticleTermRepository articleTermRepository;
    @NonNull TermsRepository termsRepository;
    @NonNull ArticlesRepository articlesRepository;

    @Transactional
    /**
     * Find articles url, that contains at least one word in query,
     * calculate singular decomposition than cosine measure with query and return top 10
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

        final Article[] articles = map.keySet().toArray(new Article[0]);
        final Term[] terms = termsRepository.findAll().toArray(new Term[0]);
        final int termsSize = terms.length;
        final int articleSize = articles.length;

        double[][] arr = new double[termsSize][articleSize];

        for (int i = 0; i < termsSize; i++) {
            for (int j = 0; j < articleSize; j++) {
                arr[i][j] = Optional.ofNullable(map.get(articles[j]).get(terms[i])).orElse(0.0);
            }
        }
        Matrix matrix = new Matrix(arr);
        SingularValueDecomposition decomposition = matrix.svd();
        Matrix S = decomposition.getS();
        Matrix U = decomposition.getU();
        Matrix V = decomposition.getV();
        //reduce dimension
        Matrix Sk = S.getMatrix(0, SingularDecomposition.K - 1, 0, SingularDecomposition.K - 1);
        Matrix Uk = U.getMatrix(0, U.getRowDimension() - 1, 0, SingularDecomposition.K - 1);
        Matrix Vk = V.getMatrix(0, V.getRowDimension() - 1, 0, SingularDecomposition.K - 1);
        double[][] arrVk = Vk.getArray();


        //build document vector
        List<Pair<Article, List<Double>>> documentVectors = new ArrayList<>();
        for (int i = 0; i < arrVk.length; i++) {
            List<Double> eigenvector = Arrays.stream(arrVk[i]).boxed().collect(toList());
            documentVectors.add(new Pair<>(articles[i], eigenvector));
        }
        //build query vector
        double[][] queryArr = new double[1][terms.length];
        for (int i = 0; i < terms.length; i++) {
            if (queryTerms.contains(terms[i])) {
                queryArr[0][i] = 1;
            } else {
                queryArr[0][i] = 0;
            }
        }
        Matrix queryMatrix = new Matrix(queryArr);
        Matrix inverseSk = Sk.inverse();
        Matrix svdQueryMatrix = queryMatrix.times(Uk).times(inverseSk);
        final List<Double> queryVector = Arrays.stream(svdQueryMatrix.getArray()[0]).boxed().collect(toList());

        //build result
        final List<Pair<String, Double>> result = documentVectors.stream()
                //map to url and cosine measure
                .map(pair -> new Pair<>(pair.getKey().getUrl(),
                        CosineSearch.cosineMeasure(queryVector, pair.getValue()))
                )

                .sorted(Comparator.comparing(Pair<String, Double>::getValue).reversed())
                .limit(10)

                .collect(toList());

        return result;
    }
}

class SingularDecomposition {
    static final int K = 5;
}
