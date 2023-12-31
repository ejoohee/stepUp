package com.pi.stepup.domain.dance.dto;

import com.pi.stepup.domain.dance.domain.RandomDance;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public class DanceResponseDto {

    @Getter
    public static class DanceFindResponseDto {

        private final Long randomDanceId;
        private final String title;
        private final String content;
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;
        private final String danceType;
        private final int maxUser;
        private final String thumbnail;
        private final String hostNickname;

        @Builder
        private DanceFindResponseDto(RandomDance randomDance) {
            this.randomDanceId = randomDance.getRandomDanceId();
            this.title = randomDance.getTitle();
            this.content = randomDance.getContent();
            this.startAt = randomDance.getStartAt();
            this.endAt = randomDance.getEndAt();
            this.danceType = String.valueOf(randomDance.getDanceType());
            this.maxUser = randomDance.getMaxUser();
            this.thumbnail = randomDance.getThumbnail();
            this.hostNickname = randomDance.getHost().getNickname();
        }
    }

    @Getter
    public static class DanceSearchResponseDto {

        private final Long randomDanceId;
        private final String title;
        private final String content;
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;
        private final String danceType;
        private final int maxUser;
        private final String thumbnail;
        private final String hostNickname;
        private final String progressType;
        private final Integer reserveStatus;
        private final int isEnd;

        @Builder
        private DanceSearchResponseDto(RandomDance randomDance, String progressType,
            Integer reserveStatus) {
            this.randomDanceId = randomDance.getRandomDanceId();
            this.title = randomDance.getTitle();
            this.content = randomDance.getContent();
            this.startAt = randomDance.getStartAt();
            this.endAt = randomDance.getEndAt();
            this.danceType = String.valueOf(randomDance.getDanceType());
            this.maxUser = randomDance.getMaxUser();
            this.thumbnail = randomDance.getThumbnail();
            this.hostNickname = randomDance.getHost().getNickname();
            this.progressType = progressType;
            this.reserveStatus = reserveStatus;
            this.isEnd = LocalDateTime.now().isBefore(this.endAt) ? 0 : 1;
        }
    }
}
