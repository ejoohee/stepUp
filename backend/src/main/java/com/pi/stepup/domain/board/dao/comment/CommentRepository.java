package com.pi.stepup.domain.board.dao.comment;

import com.pi.stepup.domain.board.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment insert(Comment comment);

    Optional<Comment> findOne(Long commentId);

    List<Comment> findByBoardId(Long boardId);

    void delete(Long commemt);

    void deleteAllByUserId(Long userId);
}
