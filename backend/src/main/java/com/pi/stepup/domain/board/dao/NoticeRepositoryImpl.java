package com.pi.stepup.domain.board.dao;

import com.pi.stepup.domain.board.domain.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Notice insert(Notice notice) {
        em.persist(notice);
        return notice;
    }

    @Override
    public Optional<Notice> findOne(Long boardId) {
        try {
            return Optional.ofNullable(em.find(Notice.class, boardId));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Notice> findAll() {
        try {
            String jpql = "SELECT n FROM Notice n";
            return em.createQuery(jpql, Notice.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("공지사항 조회 오류", e);
        }
    }

    @Override
    public List<Notice> findAllByKeyword(String keyword) {
        try {
            String jpql = "SELECT n FROM Notice n WHERE n.title LIKE :keyword OR n.content LIKE :keyword";
            return em.createQuery(jpql, Notice.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("공지사항 검색 오류", e);
        }
    }

    @Override
    public void delete(Long boardId) {
        Notice notice = em.find(Notice.class, boardId);
        if (notice != null) {
            em.remove(notice);
        } else {
            throw new IllegalArgumentException("게시글 없음.");
        }
    }
}