package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create(){
        //첫번째 댓글 입력
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        //상위 댓글의 첫번째 댓글
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
        //상위 댓글의 첫번째 댓글
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));

    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read(){
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 179060865682051072L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete(){

        //commentId=179061928581599232
        //	commentId=179061929433042944
        //	commentId=179061929709867008
        restClient.delete()
                .uri("/v1/comments/{commentId}",179061928581599232L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         * 1번 페이지 수행 결과
         comment.getCommentId() = 179060865682051072
            comment.getCommentId() = 179060867179417600
            comment.getCommentId() = 179060867334606848
         comment.getCommentId() = 179061928581599232
            comment.getCommentId() = 179061929433042944
            comment.getCommentId() = 179061929709867008
         comment.getCommentId() = 179065967874379776
            comment.getCommentId() = 179065968025374733
         comment.getCommentId() = 179065967874379777
            comment.getCommentId() = 179065968008597505
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> responses1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for (CommentResponse comment : responses1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = responses1.getLast().getParentCommentId();
        Long lastCommentId = responses1.getLast().getCommentId();

        List<CommentResponse> responses2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("secondPage");
        for (CommentResponse comment : responses2) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         firstPage
         comment.getCommentId() = 179060865682051072
            comment.getCommentId() = 179060867179417600
            comment.getCommentId() = 179060867334606848
         comment.getCommentId() = 179061928581599232
            comment.getCommentId() = 179061929433042944
            comment.getCommentId() = 179061929709867008
         secondPage
         comment.getCommentId() = 179065967874379776
            comment.getCommentId() = 179065968025374733
         comment.getCommentId() = 179065967874379777
            comment.getCommentId() = 179065968008597505
         */
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
