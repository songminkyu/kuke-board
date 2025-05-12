package kuke.board.like.repository;

import kuke.board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    
    // 메서드 이름 기반 쿼리 생성
    // JPQL로 암시적으로 생성 해줌
    // SELECT a FROM ArticleLike a WHERE a.articleId = ?1 AND a.userId = ?2
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
