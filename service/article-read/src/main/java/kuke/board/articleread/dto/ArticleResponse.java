package kuke.board.articleread.dto;

import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
