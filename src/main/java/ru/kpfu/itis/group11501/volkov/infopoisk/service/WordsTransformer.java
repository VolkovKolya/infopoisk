package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class WordsTransformer {

    private static final String SPLIT_PATTERN = "[\\p{Z}\\s.,;:!?'\"()/\\\\]+";
    private static final String RUSSIAN_WORD_REGEX = "^[а-яА-Я\\-]+$";
    private static final String STOP_WORDS_PATH = "stopwords-ru.txt";
    private static final Set<String> STOP_WORDS = new HashSet<>();

    static {
        try {
            ClassPathResource res = new ClassPathResource(STOP_WORDS_PATH);
            Scanner sc = new Scanner(res.getInputStream());
            while (sc.hasNext()) {
                STOP_WORDS.add(sc.nextLine());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IllegalStateException("Can't read stop words");
        }
    }

    //todo: revise it. English word and numbers removed now. Stemmers can't work with it!
    public List<String> getWordsFromText(final String text) {
        return Arrays.stream(text.split(SPLIT_PATTERN))
                .map(String::toLowerCase)
                .filter(word -> !word.equals("–"))
                .filter(this::isRussianWord)
                .filter(word -> !isStopWord(word))
                .collect(Collectors.toList());
    }

    private boolean isStopWord(final String word) {
        return STOP_WORDS.contains(word);
    }

    private boolean isRussianWord(final String word) {
        return word.matches(RUSSIAN_WORD_REGEX);
    }

    private static final SnowballStemmer porterStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN);
    public String lemmatiseWithPorter(final String word) {
        return porterStemmer.stem(word).toString();
    }

    private static final MyStem myStemAnalyzer =
            new Factory("-igd --eng-gr --format json --weight")
                    .newMyStem("3.0", Option.empty()).get();

    @SneakyThrows
    public String lemmatiseWithMyStem(final String word) {
        final Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        myStemAnalyzer
                                .analyze(Request.apply(word))
                                .info()
                                .toIterable());
        for (final Info info : result) {
            return info.lex().get();
        }
        log.error("Can't work with word:", word);
        throw new IllegalArgumentException("Wrong word");
    }
}
