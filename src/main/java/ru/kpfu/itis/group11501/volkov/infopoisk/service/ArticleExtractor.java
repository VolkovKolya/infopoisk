package ru.kpfu.itis.group11501.volkov.infopoisk.service;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.kpfu.itis.group11501.volkov.infopoisk.domain.Article.KEYWORD_DELIMITER;

@Service
public class ArticleExtractor {
    private static final String COUNT_ARTICLE_EXPRESSION = "count(//article)";
    private static final String ARTICLE_URL_PATTERN = "//article[%s]//a[@rel='bookmark']//@href";

    private static final String TITLE_EXPRESSION = "//article//header//a[@rel='bookmark' and @title]";
    private static final String KEYWORDS_EXPRESSION = "//article//footer//a[@rel='tag']";
    private static final String CONTENT_EXPRESSION = "//article//div[@itemprop='text']/p";

    @SneakyThrows
    List<String> findArticles(String url) {
        org.jsoup.nodes.Document document = Jsoup.parse(new URL(url), 0);
        Document xmlDocument = new W3CDom().fromJsoup(document);

        XPath xPath = XPathFactory.newInstance().newXPath();
        int size = ((Number) xPath.compile(COUNT_ARTICLE_EXPRESSION)
                .evaluate(xmlDocument, XPathConstants.NUMBER)).intValue();

        List<String> list = new ArrayList<>(size);
        for (int i=1; i<=size; i++){
            String expression = String.format(ARTICLE_URL_PATTERN,i);
            String articleUrl = (String) xPath.compile(expression)
                    .evaluate(xmlDocument, XPathConstants.STRING);
            list.add(articleUrl);
        }
        return list;
    }
    @SneakyThrows
    Article getArticleByUrl(String url) {

        org.jsoup.nodes.Document document = Jsoup.parse(new URL(url), 0);
        Document xmlDocument = new W3CDom().fromJsoup(document);

        XPath xPath = XPathFactory.newInstance().newXPath();

        String title = (String) xPath.compile(TITLE_EXPRESSION)
                .evaluate(xmlDocument, XPathConstants.STRING);
        title = title.replaceAll("\\s+", " ");
        title = title.trim();

        NodeList contentNodes = (NodeList) xPath.compile(CONTENT_EXPRESSION)
                .evaluate(xmlDocument, XPathConstants.NODESET);
        String content = transformContentToString(contentNodes);

        NodeList keywordsNodes = (NodeList) xPath.compile(KEYWORDS_EXPRESSION)
                .evaluate(xmlDocument, XPathConstants.NODESET);
        String keywords = new ArticleExtractor().transformKeywordsToString(keywordsNodes);

        return Article.builder()
                .id(UUID.randomUUID())
                .title(title)
                .keywords(keywords)
                .content(content)
                .url(url)
                .build();
    }

    private String transformContentToString(NodeList content) {
        StringBuilder sb = new StringBuilder();

        for (int i=0; i< content.getLength();i++){
            String text = content.item(i).getTextContent();
            text = text.replaceAll("\\s+", " ");

            sb.append(text);
        }
        String result = sb.toString();
        return result.trim();
    }
    private String transformKeywordsToString(NodeList keywords) {
        StringBuilder sb = new StringBuilder();

        for (int i=0; i< keywords.getLength();i++){
            String keyword = keywords.item(i).getTextContent();
            keyword = keyword.replaceAll("\\s+", " ");
            keyword = keyword.trim();

            sb.append(keyword);
            sb.append(KEYWORD_DELIMITER);
        }

        return sb.toString();
    }



}
