package ru.kpfu.itis.group11501.volkov.infopoisk;

import lombok.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.ArticleExtractor;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.ArticleUploader;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.WordsUploader;

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


}
