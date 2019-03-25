package ru.kpfu.itis.group11501.volkov.infopoisk;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import com.sun.management.OperatingSystemMXBean;
import lombok.NonNull;
import org.apache.commons.io.FileSystemUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.*;

@SpringBootApplication
public class InfopoiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfopoiskApplication.class, args);
    }

    @Bean
    @Profile(value = "uploadArticle")
    public CommandLineRunner uploadArticle(@NonNull ArticleUploader articleUploader) {
        return args -> {
            articleUploader.uploadArticlesToDb();
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "uploadWords")
    public CommandLineRunner uploadWords(@NonNull WordsUploader wordsUploader) {
        return args -> {
            wordsUploader.uploadWordsFromArticlesToDb();
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "uploadTerms")
    public CommandLineRunner uploadTerms(@NonNull WordsUploader wordsUploader) {
        return args -> {
            wordsUploader.uploadWordsPorterToTerms();
            System.exit(0);
        };
    }


    @Bean
    @Profile(value = "booleanSearch")
    public CommandLineRunner findArticleUrlsByText(@NonNull BooleanSearch booleanSearch) {
        return args -> {
            booleanSearch.searchText("более вЫгодный курс").forEach(System.out::println);
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "uploadTfIdf")
    public CommandLineRunner uploadTfIdf(@NonNull WordsUploader wordsUploader) {
        return args -> {
            wordsUploader.uploadTfIdfToTerms();
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "cosineSearch")
    public CommandLineRunner findArticleUrlsByCosineMeasure(@NonNull CosineSearch cosineSearch) {
        return args -> {
            cosineSearch.searchText("более вЫгодный курс биткоин биржа программа валюта")
                    .forEach(pair -> System.out.println("Url: " + pair.getKey()
                            + " cosine_measure: " + pair.getValue()));
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "bm25Search")
    public CommandLineRunner findArticleUrlsByBm25Measure(@NonNull Bm25Search bm25Search) {
        return args -> {
            bm25Search.searchText("более вЫгодный курс биткоин биржа программа валюта")
                    .forEach(pair -> System.out.println("Url: " + pair.getKey()
                            + " bm25_measure: " + pair.getValue()));
            System.exit(0);
        };
    }

    @Bean
    @Profile(value = "singularDecomposition")
    public CommandLineRunner singularDecomposition(@NonNull SingularDecompositionSearch search) {
        return args -> {
            search.searchText("более вЫгодный курс биткоин биржа программа валюта")
                    .forEach(pair -> System.out.println("Url: " + pair.getKey()
                            + " SVD_cosine_measure: " + pair.getValue()));
            System.exit(0);
        };
    }

}
