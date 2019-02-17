package ru.kpfu.itis.group11501.volkov.infopoisk;

import lombok.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.ArticleExtractor;
import ru.kpfu.itis.group11501.volkov.infopoisk.service.ArticleUploader;

@SpringBootApplication
public class InfopoiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfopoiskApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(@NonNull ArticleUploader articleUploader) {
        return args -> {
                articleUploader.uploadArticlesToDb();
        };
    }


}
