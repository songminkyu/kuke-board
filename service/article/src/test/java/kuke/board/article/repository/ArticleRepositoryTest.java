package kuke.board.article.repository;

import kuke.board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("articles.size = {}", articles.size());

        for(Article article : articles) {
            log.info("article = {}", article);
        }
        assertThat(articles.size()).isEqualTo(30);
    }

    @Test
    void countTest() {
        Long count = articleRepository.count(1L, 10000L);
        log.info("count = {}", count);
        assertThat(count).isEqualTo(10000);
    }

    @Test
    void findInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllInfiniteScroll(1L, 30L);
        for(Article article : articles) {
            log.info("article = {}", article.getArticleId());
        }
        Long lastArticleId = articles.getLast().getArticleId();
        List<Article> lastArticles = articleRepository.findAllInfiniteScroll(1L, 30L, lastArticleId);

        for(Article article : lastArticles) {
            log.info("article = {}", article.getArticleId());
        }
    }

}