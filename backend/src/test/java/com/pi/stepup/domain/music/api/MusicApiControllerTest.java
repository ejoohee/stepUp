package com.pi.stepup.domain.music.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.pi.stepup.domain.music.domain.Music;
import com.pi.stepup.domain.music.domain.MusicAnswer;
import com.pi.stepup.domain.music.dto.MusicRequestDto.MusicSaveRequestDto;
import com.pi.stepup.domain.music.dto.MusicResponseDto.MusicFindResponseDto;
import com.pi.stepup.domain.music.service.MusicService;
import com.pi.stepup.domain.user.constant.UserRole;
import com.pi.stepup.domain.user.domain.User;
import com.pi.stepup.domain.user.service.UserRedisService;
import com.pi.stepup.global.util.jwt.JwtTokenProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = MusicApiController.class)
class MusicApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MusicService musicService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider; // jwtTokenProvider 주입받기 실패했다고 떠서 추가

    @MockBean
    private UserRedisService userRedisService;

    private Gson gson;
    private MusicSaveRequestDto musicSaveRequestDto;
    private MusicFindResponseDto musicFindResponseDto;

    private Music music;
    private MusicAnswer musicAnswer;
    private User admin;

    @BeforeEach
    public void init() {
        makeAdmin();
        gson = new Gson();
        makeMusicSaveRequestDto();
        music = musicSaveRequestDto.toMusic();
        musicAnswer = musicSaveRequestDto.toMusicAnswer();
        musicFindResponseDto = MusicFindResponseDto.builder().music(music).musicAnswer(musicAnswer)
            .build();
    }

    @Test
    @DisplayName("노래 추가 컨트롤러 테스트")
    @WithMockUser
    // @WithMockUser(username = "admin",  roles = "ADMIN") // 관리자 권한 체크 ..?
    public void createMusicControllerTest() throws Exception {
        when(musicService.create(any())).thenReturn(music);

        String url = "/api/music";
        final ResultActions actions = mockMvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf())
                .content(gson.toJson(musicSaveRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
        );

        actions.andExpect(status().isCreated()).andDo(print());
    }

    @Test
    @DisplayName("노래 한 곡 조회 테스트")
    @WithUserDetails
    public void readOneMusicControllerTest() throws Exception {
        when(musicService.readOne(any())).thenReturn(musicFindResponseDto);

        long musicId = 1L;
        final ResultActions getAction = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/music/" + musicId)
        );

        getAction.andExpect(status().isOk()).andDo(print());
    }

    @Test
    @DisplayName("로그인 안한 사용자가 노래 한 곡 조회 할 경우 예외 처리 테스트")
    @WithAnonymousUser
    public void readOneMusicNotLoginUserControllerTest() throws Exception {
        long musicId = 1L;
        final ResultActions getAction = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/music/" + musicId)
        );

        getAction.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("노래 목록 조회 테스트")
    @WithUserDetails
    public void readAllMusicControllerTest() throws Exception {
        String keyword = "";
        when(musicService.readAll(keyword)).thenReturn(makeMusic());

        final ResultActions getAction = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/music?keyword=")
        );

        getAction.andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andDo(print());
    }

    @Test
    @DisplayName("노래 목록 키워드 조회 테스트")
    @WithUserDetails
    public void readAllByKeywordMusicControllerTest() throws Exception {
        String keyword = "1";
        when(musicService.readAll(keyword)).thenReturn(keywordMusic(keyword));

        final ResultActions getAction = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/music?keyword=" + keyword)
        );

        getAction.andExpect(status().isOk())
            .andExpect(jsonPath("data[3]").doesNotExist())
            .andDo(print());
    }

    @Test
    @DisplayName("로그인 안한 사용자가 노래 목록 조회 할 경우 예외 처리 테스트")
    @WithAnonymousUser
    public void readAllMusicNotLoginUserControllerTest() throws Exception {
        final ResultActions getAction = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/music?keyword=")
        );

        getAction.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("노래 삭제 테스트")
    @WithMockUser
    // @WithMockUser(username = "admin",  roles = "ADMIN")
    public void deleteMusicControllerTest() throws Exception {
        Long musicId = 1L;

        final ResultActions deleteAction = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/music/" + musicId).with(csrf())
        );

        verify(musicService, only()).delete(musicId);
        deleteAction.andExpect(status().isOk());
    }

    private List<MusicFindResponseDto> makeMusic() {
        List<MusicFindResponseDto> music = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Music tmp = Music.builder().title("title" + i).artist("artist" + (i + 1)).build();
            MusicAnswer tmpAnswer = MusicAnswer.builder().answer("answer" + i).build();
            music.add(new MusicFindResponseDto(tmp, tmpAnswer));
        }
        return music;
    }

    private List<MusicFindResponseDto> keywordMusic(String keyword) {
        List<MusicFindResponseDto> music = makeMusic();
        List<MusicFindResponseDto> result = new ArrayList<>();
        for (MusicFindResponseDto dto : music) {
            if (dto.getTitle().contains(keyword) || dto.getArtist().contains(keyword)) {
                result.add(dto);
            }
        }
        return result;
    }

    private void makeMusicSaveRequestDto() {
        musicSaveRequestDto = MusicSaveRequestDto.builder()
            .title("spicy")
            .artist("aespa")
            .answer("")
            .URL("url")
            .build();
    }

    private void makeAdmin() {
        admin = User.builder()
            .id("admin")
            .password("pass")
            .nickname("admin")
            .role(UserRole.ROLE_ADMIN)
            .build();
    }
}