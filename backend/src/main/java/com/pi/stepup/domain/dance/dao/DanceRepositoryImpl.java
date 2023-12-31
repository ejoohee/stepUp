package com.pi.stepup.domain.dance.dao;

import com.pi.stepup.domain.dance.domain.AttendHistory;
import com.pi.stepup.domain.dance.domain.DanceMusic;
import com.pi.stepup.domain.dance.domain.RandomDance;
import com.pi.stepup.domain.dance.domain.Reservation;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class DanceRepositoryImpl implements DanceRepository {

    private final EntityManager em;

    @Override
    public RandomDance insert(RandomDance randomDance) {
        em.persist(randomDance);
        return randomDance;
    }

    @Override
    public Optional<RandomDance> findOne(Long randomDanceId) {
        Optional<RandomDance> randomDance = null;
        try {
            randomDance = Optional.ofNullable(em.find(RandomDance.class, randomDanceId));
        } catch (NoResultException e) {
            randomDance = Optional.empty();
        } finally {
            return randomDance;
        }
    }

    @Override
    public void delete(Long randomDanceId) {
        RandomDance randomDance = em.find(RandomDance.class, randomDanceId);
        em.remove(randomDance);
    }

    @Override
    public void updateAllHostDeletedByUserID(Long userId) {
        em.createQuery("UPDATE RandomDance rd SET rd.host = 0 WHERE rd.host.userId=:userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public List<DanceMusic> findAllDanceMusic(Long randomDanceId) {
        return em.createQuery("SELECT d FROM DanceMusic d "
                + "WHERE d.randomDance.randomDanceId = :randomDanceId", DanceMusic.class)
            .setParameter("randomDanceId", randomDanceId)
            .getResultList();
    }

    @Override
    public List<RandomDance> findAllMyOpenDance(String id) {
        return em.createQuery("SELECT r FROM RandomDance r "
                + "WHERE r.host.id = :id "
                + "ORDER BY r.startAt DESC", RandomDance.class)
            .setParameter("id", id)
            .getResultList();
    }

    @Override
    public List<RandomDance> findAllDance(String keyword) {
        String sql = "SELECT r FROM RandomDance r "
            + "LEFT JOIN AttendHistory a "
            + "ON r.randomDanceId = a.randomDance.randomDanceId "
            + "LEFT JOIN Reservation s "
            + "ON r.randomDanceId = s.randomDance.randomDanceId "
            + "WHERE r.endAt >= now() ";

        if (StringUtils.hasText(keyword) && !keyword.equals("")) {
            sql += "AND (r.title LIKE '%" + keyword + "%' OR " +
                "r.content LIKE '%" + keyword + "%') ";
        }

        sql += "GROUP BY r ORDER BY COUNT(a) DESC, COUNT(s) DESC, ABS(TIMESTAMPDIFF(SECOND, now(), r.startAt)) ASC";

        return em.createQuery(sql, RandomDance.class).getResultList();
    }

    @Override
    public List<RandomDance> findScheduledDance(String keyword) {
        String sql = "SELECT r FROM RandomDance r "
            + "WHERE r.startAt > now() ";

        if (StringUtils.hasText(keyword) && !keyword.equals("")) {
            sql += "AND (r.title LIKE '%" + keyword + "%' OR " +
                "r.content LIKE '%" + keyword + "%') ";
        }

        sql += "ORDER BY ABS(TIMESTAMPDIFF(SECOND, now(), r.startAt)) ASC";

        return em.createQuery(sql, RandomDance.class).getResultList();
    }

    @Override
    public List<RandomDance> findInProgressDance(String keyword) {
        String sql = "SELECT r FROM RandomDance r "
            + "WHERE r.startAt <= now() "
            + "AND r.endAt >= now() ";

        if (StringUtils.hasText(keyword) && !keyword.equals("")) {
            sql += "AND (r.title LIKE '%" + keyword + "%' OR " +
                "r.content LIKE '%" + keyword + "%') ";
        }

        sql += "ORDER BY ABS(TIMESTAMPDIFF(SECOND, now(), r.startAt)) ASC";

        return em.createQuery(sql, RandomDance.class).getResultList();
    }

    @Override
    public Reservation insertReservation(Reservation reservation) {
        em.persist(reservation);
        return reservation;
    }

    @Override
    public List<Reservation> insertReservationList(List<Reservation> reservationList) {
        for (int i = 0; i < reservationList.size(); i++) {
            em.persist(reservationList.get(i));
        }
        return reservationList;
    }

    @Override
    public Optional<Reservation> findReservationByRandomDanceIdAndUserId(Long randomDanceId,
        Long userId) {
        Optional<Reservation> reservation = null;
        try {
            reservation = Optional.ofNullable(em.createQuery("SELECT r FROM Reservation r "
                    + "WHERE r.randomDance.randomDanceId = :randomDanceId "
                    + "AND r.user.userId = :userId "
                    + "AND r.randomDance.startAt > now() ", Reservation.class)
                .setParameter("randomDanceId", randomDanceId)
                .setParameter("userId", userId)
                .getSingleResult());
        } catch (NoResultException e) {
            reservation = Optional.empty();
        } finally {
            return reservation;
        }
    }

    @Override
    public Optional<Reservation> findReservationByReservationIdAndRandomDanceId(Long reservationId,
        Long randomDanceId) {
        Optional<Reservation> reservation = null;
        try {
            reservation = Optional.ofNullable(em.createQuery("SELECT r FROM Reservation r "
                    + "WHERE r.reservationId = :reservationId "
                    + "AND r.randomDance.randomDanceId = :randomDanceId "
                    + "AND r.randomDance.startAt > now() ", Reservation.class)
                .setParameter("reservationId", reservationId)
                .setParameter("randomDanceId", randomDanceId)
                .getSingleResult());
        } catch (NoResultException e) {
            reservation = Optional.empty();
        } finally {
            return reservation;
        }
    }

    @Override
    public void deleteReservation(Long randomDanceId, Long userId) {
        em.createQuery("DELETE FROM Reservation r "
                + "WHERE r.randomDance.randomDanceId = :randomDanceId "
                + "AND r.user.userId = :userId")
            .setParameter("randomDanceId", randomDanceId)
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public void deleteAllReservationByUserId(Long userId) {
        em.createQuery("DELETE FROM Reservation r "
                + "WHERE r.user.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public List<Reservation> findAllMyReservation(Long userId) {
        return em.createQuery("SELECT r FROM Reservation r "
                + "WHERE r.user.userId = :userId "
                + "AND r.randomDance.startAt > now() "
                + "ORDER BY r.createdAt DESC", Reservation.class)
            .setParameter("userId", userId)
            .getResultList();
    }


    @Override
    public AttendHistory insertAttend(AttendHistory attendHistory) {
        em.persist(attendHistory);
        return attendHistory;
    }

    @Override
    public void deleteAllAttendByUserId(Long userId) {
        em.createQuery("DELETE FROM AttendHistory ah WHERE ah.user.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }

    @Override
    public Optional<AttendHistory> findAttendByRandomDanceIdAndUserId(Long randomDanceId,
        Long userId) {
        Optional<AttendHistory> attend = null;
        try {
            attend = Optional.ofNullable(em.createQuery("SELECT a FROM AttendHistory a "
                    + "WHERE a.randomDance.randomDanceId = :randomDanceId "
                    + "AND a.user.userId = :userId", AttendHistory.class)
                .setParameter("randomDanceId", randomDanceId)
                .setParameter("userId", userId)
                .getSingleResult());
        } catch (NoResultException e) {
            attend = Optional.empty();
        } finally {
            return attend;
        }
    }

    @Override
    public List<AttendHistory> findAllMyAttend(Long userId) {
        return em.createQuery("SELECT a FROM AttendHistory a "
                + "WHERE a.user.userId = :userId "
                + "ORDER BY a.createdAt DESC", AttendHistory.class)
            .setParameter("userId", userId)
            .getResultList();
    }
}
