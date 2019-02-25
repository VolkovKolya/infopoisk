package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Term;
import ru.kpfu.itis.group11501.volkov.infopoisk.repositories.TermsRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BooleanSearch {

    @NonNull WordsTransformer wordsTransformer;
    @NonNull TermsRepository termsRepository;

    @Transactional
    public List<String> searchText(String text) {
        List<String> words = wordsTransformer.getWordsFromText(text);
        //list terms order asc by articles size
        List<Term> terms = words.stream()
                .map(wordsTransformer::lemmatiseWithPorter)
                .map(termsRepository::findByText)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(term -> term.getArticles().size()))
                .collect(Collectors.toList());

        List<Article> result = terms.iterator().next().getArticles();
        for (Term term : terms) {
            result = intersect(result, term.getArticles());
            if (result.isEmpty()) {
                break;
            }
        }

        return result.stream()
                .map(Article::getUrl)
                .collect(Collectors.toList());
    }

    private List<Article> intersect(List<Article> list1, List<Article> list2) {
        return list1.stream()
                .filter(list2::contains)
                .collect(Collectors.toList());
    }
}

