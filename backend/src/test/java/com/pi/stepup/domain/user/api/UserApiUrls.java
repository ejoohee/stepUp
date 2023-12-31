package com.pi.stepup.domain.user.api;

public enum UserApiUrls {
    READ_ALL_COUNTRIES_URL("/country"),
    CHECK_EMAIL_DUPLICATED_URL("/dupemail"),
    CHECK_NICKNAME_DUPLICATED_URL("/dupnick"),
    CHECK_ID_DUPLICATED_URL("/dupid"),
    READ_ONE_URL(""),
    SIGN_UP_URL(""),
    LOGIN_URL("/login"),
    FIND_ID_URL("/findid"),
    FIND_PASSWORD_URL("/findpw"),
    DELETE_URL(""),
    REISSUE_TOKENS_URL("/auth"),
    UPDATE_URL(""),
    CHECK_PASSWORD_URL("/checkpw");

    private final String url;

    UserApiUrls(String url) {
        String baseUrl = "/api/user";
        this.url = baseUrl + url;
    }

    public String getUrl() {
        return this.url;
    }
}
