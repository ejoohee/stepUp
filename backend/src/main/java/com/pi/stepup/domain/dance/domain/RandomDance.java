package com.pi.stepup.domain.dance.domain;

import com.pi.stepup.domain.dance.constant.DanceType;
import com.pi.stepup.domain.user.domain.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RANDOM_DANCE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RandomDance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RANDOM_DANCE_ID")
    private Long randomDanceId;

    private String title;

    private String content;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private DanceType danceType;

    private int maxUser;

    private String thumbnail;

    //호스트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User host;

    //사용하는 노래 목록
    @OneToMany(mappedBy = "randomDance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DanceMusic> danceMusicList = new ArrayList<>();

    //예약한 유저 목록
    @OneToMany(mappedBy = "randomDance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservation = new ArrayList<>();

    @Builder
    public RandomDance(Long randomDanceId, String title, String content, LocalDateTime startAt,
        LocalDateTime endAt, DanceType danceType, int maxUser, String thumbnail, User host) {
        this.randomDanceId = randomDanceId;
        this.title = title;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.danceType = danceType;
        this.maxUser = maxUser;
        this.thumbnail = thumbnail;
        this.host = host;
    }
}
