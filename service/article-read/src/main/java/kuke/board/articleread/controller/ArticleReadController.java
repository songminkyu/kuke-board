package kuke.board.articleread.controller;

import kuke.board.articleread.service.ArticleReadService;
import kuke.board.articleread.service.response.ArticleReadPageResponse;
import kuke.board.articleread.service.response.ArticleReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    @GetMapping("/v1/article-read/test")
    public String readTest() {
        return "article-read test";
    }

    @GetMapping("/v1/article-read/{articleId}")
    public ArticleReadResponse read(@PathVariable("articleId") Long articleId) {
        return articleReadService.read(articleId);
    }

    @GetMapping("/v1/article-read")
    public ArticleReadPageResponse readAll(
            @RequestParam("boardId") Long boardId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAll(boardId, page, pageSize);
    }

    @GetMapping("/v1/article-read/infinite-scroll")
    public List<ArticleReadResponse> readAllInfiniteScroll(
            @RequestParam("boardId") Long boardId,
            @RequestParam(value = "lastArticleId", required = false) Long lastArticleId,
            @RequestParam("pageSize") Long pageSize
    ) {
        return articleReadService.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
    }

}
