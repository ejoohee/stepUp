package com.pi.stepup.domain.user.dto;

import com.pi.stepup.domain.rank.constant.RankName;
import com.pi.stepup.domain.user.constant.UserRole;
import com.pi.stepup.domain.user.domain.Country;
import com.pi.stepup.domain.user.domain.User;
import com.pi.stepup.domain.user.domain.UserInfo;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

public class UserResponseDto {

    @Getter
    public static class CountryResponseDto {

        private final Long countryId;
        private final String countryCode;

        @Builder
        private CountryResponseDto(Country country) {
            this.countryId = country.getCountryId();
            this.countryCode = country.getCode();
        }
    }

    @Getter
    public static class UserInfoResponseDto {

        private final String email;
        private final Integer emailAlert;
        private final Long countryId;
        private final String countryCode;
        private final String nickname;
        private final LocalDate birth;
        private final String profileImg;
        private final Integer point;
        private final RankName rankName;
        private final String rankImg;
        private final UserRole role;

        @Builder
        public UserInfoResponseDto(User user) {
            this.email = user.getEmail();
            this.emailAlert = user.getEmailAlert();
            this.countryId = user.getCountry().getCountryId();
            this.countryCode = user.getCountry().getCode();
            this.nickname = user.getNickname();
            this.birth = user.getBirth();
            this.profileImg = user.getProfileImg();
            this.point = user.getPoint();
            this.rankName = user.getRank().getName();
            this.rankImg = user.getRank().getRankImg();
            this.role = user.getRole();
        }

        public UserInfoResponseDto(UserInfo userInfo) {
            this.email = userInfo.getEmail();
            this.emailAlert = userInfo.getEmailAlert();
            this.countryId = userInfo.getCountryId();
            this.countryCode = userInfo.getCountryCode();
            this.nickname = userInfo.getNickname();
            this.birth = userInfo.getBirth();
            this.profileImg = userInfo.getProfileImg();
            this.point = userInfo.getPoint();
            this.rankName = RankName.valueOf(userInfo.getRankName());
            this.rankImg = userInfo.getRankImg();
            this.role = userInfo.getRole();
        }
    }

    @Getter
    public static class AuthenticatedResponseDto {

        private final TokenInfo tokens;
        private final UserInfoResponseDto userInfo;

        @Builder
        public AuthenticatedResponseDto(TokenInfo tokenInfo, User user) {
            tokens = tokenInfo;
            userInfo = UserInfoResponseDto.builder().user(user).build();
        }
    }
}
