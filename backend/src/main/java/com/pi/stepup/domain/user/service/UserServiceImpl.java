package com.pi.stepup.domain.user.service;

import static com.pi.stepup.domain.rank.constant.RankExceptionMessage.RANK_NOT_FOUND;
import static com.pi.stepup.domain.user.constant.UserExceptionMessage.EMAIL_DUPLICATED;
import static com.pi.stepup.domain.user.constant.UserExceptionMessage.ID_DUPLICATED;
import static com.pi.stepup.domain.user.constant.UserExceptionMessage.NICKNAME_DUPLICATED;
import static com.pi.stepup.domain.user.constant.UserExceptionMessage.USER_NOT_FOUND;
import static com.pi.stepup.domain.user.constant.UserExceptionMessage.WRONG_PASSWORD;

import com.pi.stepup.domain.board.dao.board.BoardRepository;
import com.pi.stepup.domain.board.dao.comment.CommentRepository;
import com.pi.stepup.domain.dance.dao.DanceRepository;
import com.pi.stepup.domain.music.dao.MusicApplyRepository;
import com.pi.stepup.domain.rank.constant.RankName;
import com.pi.stepup.domain.rank.dao.PointHistoryRepository;
import com.pi.stepup.domain.rank.dao.RankRepository;
import com.pi.stepup.domain.rank.domain.Rank;
import com.pi.stepup.domain.rank.exception.RankNotFoundException;
import com.pi.stepup.domain.user.constant.EmailGuideContent;
import com.pi.stepup.domain.user.dao.UserRepository;
import com.pi.stepup.domain.user.domain.Country;
import com.pi.stepup.domain.user.domain.EmailContent;
import com.pi.stepup.domain.user.domain.EmailMessage;
import com.pi.stepup.domain.user.domain.User;
import com.pi.stepup.domain.user.domain.UserInfo;
import com.pi.stepup.domain.user.dto.TokenInfo;
import com.pi.stepup.domain.user.dto.UserRequestDto.ChangePasswordRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckEmailRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckIdRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckNicknameRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckPasswordRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.FindIdRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.FindPasswordRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.LoginRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.SignUpRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.UpdateUserRequestDto;
import com.pi.stepup.domain.user.dto.UserResponseDto.AuthenticatedResponseDto;
import com.pi.stepup.domain.user.dto.UserResponseDto.CountryResponseDto;
import com.pi.stepup.domain.user.dto.UserResponseDto.UserInfoResponseDto;
import com.pi.stepup.domain.user.dto.statistics.UserCountryStatisticsDto;
import com.pi.stepup.domain.user.exception.EmailDuplicatedException;
import com.pi.stepup.domain.user.exception.IdDuplicatedException;
import com.pi.stepup.domain.user.exception.NicknameDuplicatedException;
import com.pi.stepup.domain.user.exception.UserNotFoundException;
import com.pi.stepup.domain.user.util.EmailMessageMaker;
import com.pi.stepup.domain.user.util.RandomPasswordGenerator;
import com.pi.stepup.global.config.security.SecurityUtils;
import com.pi.stepup.global.error.exception.ForbiddenException;
import com.pi.stepup.global.util.jwt.JwtTokenProvider;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RankRepository rankRepository;
    private final UserRedisService userRedisService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final MusicApplyRepository musicApplyRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final DanceRepository danceRepository;
    private final PointHistoryRepository pointHistoryRepository;


    @Override
    public List<CountryResponseDto> readAllCountries() {
        return userRepository.findAllCountries()
            .stream()
            .map(c -> CountryResponseDto.builder().country(c).build())
            .collect(Collectors.toList());
    }

    @Override
    public List<UserCountryStatisticsDto> readStatisticsOfUserCountry() {
        return userRepository.findStatisticsOfUserCountry();
    }

    @Override
    public void checkEmailDuplicated(CheckEmailRequestDto checkEmailRequestDto) {
        try {
            User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

            if (!user.getEmail().equals(checkEmailRequestDto.getEmail())) {
                throw new ForbiddenException();
            }
        } catch (ForbiddenException e) {
            if (userRepository.findByEmail(checkEmailRequestDto.getEmail()).isPresent()) {
                throw new EmailDuplicatedException(EMAIL_DUPLICATED.getMessage());
            }
        }
    }

    @Override
    public void checkNicknameDuplicated(CheckNicknameRequestDto checkNicknameRequestDto) {
        try {
            User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

            if (!user.getNickname().equals(checkNicknameRequestDto.getNickname())) {
                throw new ForbiddenException();
            }
        } catch (ForbiddenException e) {
            if (userRepository.findByNickname(checkNicknameRequestDto.getNickname()).isPresent()) {
                throw new NicknameDuplicatedException(NICKNAME_DUPLICATED.getMessage());
            }
        }
    }

    @Override
    public void checkIdDuplicated(CheckIdRequestDto checkIdRequestDto) {
        if (userRepository.findById(checkIdRequestDto.getId()).isPresent()) {
            throw new IdDuplicatedException(ID_DUPLICATED.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthenticatedResponseDto login(LoginRequestDto loginRequestDto) {
        TokenInfo tokenInfo = setFirstAuthentication(loginRequestDto.getId(),
            loginRequestDto.getPassword());

        User user = userRepository.findById(loginRequestDto.getId()).get();
        userRedisService.saveRefreshToken(user.getId(), tokenInfo.getRefreshToken());

        return AuthenticatedResponseDto.builder()
            .tokenInfo(tokenInfo)
            .user(user)
            .build();
    }

    @Override
    @Transactional
    public AuthenticatedResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        validateSignUpUserInfo(signUpRequestDto);

        User user = signUpRequestDto.toUser(
            passwordEncoder.encode(signUpRequestDto.getPassword()),
            userRepository.findOneCountry(signUpRequestDto.getCountryId()));

        Rank bronzeRank = rankRepository.getRankByName(RankName.BRONZE)
            .orElseThrow(() -> new RankNotFoundException(RANK_NOT_FOUND.getMessage()));

        user.setRank(bronzeRank);
        user.setPointZero();

        userRedisService.saveUserInfo(user);

        userRepository.insert(user);

        TokenInfo tokenInfo = setFirstAuthentication(signUpRequestDto.getId(),
            signUpRequestDto.getPassword());

        userRedisService.saveRefreshToken(user.getId(), tokenInfo.getRefreshToken());

        return AuthenticatedResponseDto.builder()
            .tokenInfo(tokenInfo)
            .user(user)
            .build();
    }

    @Override
    public UserInfoResponseDto readOne() {
        UserInfo userInfo = userRedisService.getUserInfo(SecurityUtils.getLoggedInUserId());

        if (userInfo != null) {
            return new UserInfoResponseDto(userInfo);
        }

        User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        userRedisService.saveUserInfo(user);

        return UserInfoResponseDto.builder().user(user).build();
    }

    @Override
    @Transactional
    public void delete() {
        User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        userRedisService.deleteRefreshToken(user.getId());
        userRedisService.deleteUserInfo(user);

        musicApplyRepository.deleteAllHeartsByUserId(user.getUserId());
        musicApplyRepository.findAllByUserId(user.getUserId())
            .forEach(ma -> musicApplyRepository.delete(ma.getMusicApplyId()));

        commentRepository.deleteAllByUserId(user.getUserId());
        boardRepository.findAllBoardByUserId(user.getUserId())
            .forEach(boardRepository::delete);

        danceRepository.deleteAllAttendByUserId(user.getUserId());
        danceRepository.deleteAllReservationByUserId(user.getUserId());
        pointHistoryRepository.deleteAllByUserId(user.getUserId());

        danceRepository.updateAllHostDeletedByUserID(user.getUserId());


        userRepository.delete(user);
    }

    @Override
    public void checkPassword(CheckPasswordRequestDto checkPasswordRequestDto) {
        User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        if (!isSamePassword(user.getPassword(), checkPasswordRequestDto.getPassword())) {
            throw new UserNotFoundException(WRONG_PASSWORD.getMessage());
        }
    }

    @Override
    @Transactional
    public void update(UpdateUserRequestDto updateUserRequestDto) {
        User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        validateUpdateUserInfo(user, updateUserRequestDto);

        Country country = null;
        if (updateUserRequestDto.getCountryId() != null) {
            country = userRepository.findOneCountry(updateUserRequestDto.getCountryId());
        }

        user.updateUserBasicInfo(updateUserRequestDto, country);
        userRedisService.saveUserInfo(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDto changePasswordRequestDto) {
        User user = userRepository.findById(SecurityUtils.getLoggedInUserId())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        if (StringUtils.hasText(changePasswordRequestDto.getPassword())) {
            if (!isSamePassword(user.getPassword(), changePasswordRequestDto.getPassword())) {
                user.updatePassword(passwordEncoder.encode(changePasswordRequestDto.getPassword()));
            }
        }
    }

    @Override
    public void findId(FindIdRequestDto findIdRequestDto) {
        User user = userRepository.findByEmailAndBirth(findIdRequestDto.getEmail(),
                findIdRequestDto.getBirth())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        emailService.sendFindIdMail(
            makeEmailMessage(
                EmailGuideContent.FIND_ID_GUIDE,
                user.getNickname(),
                user.getId(),
                user.getEmail()
            )
        );
    }

    @Override
    @Transactional
    public void findPassword(FindPasswordRequestDto findPasswordRequestDto) {
        User user = userRepository.findByIdAndEmail(findPasswordRequestDto.getId(),
                findPasswordRequestDto.getEmail())
            .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        String randomPassword = RandomPasswordGenerator.generateRandomPassword();

        emailService.sendFindIdMail(
            makeEmailMessage(
                EmailGuideContent.FIND_PASSWORD_GUIDE,
                user.getNickname(),
                randomPassword,
                user.getEmail()
            )
        );

        user.updatePassword(passwordEncoder.encode(randomPassword));
    }

    private EmailMessage makeEmailMessage(
        EmailGuideContent emailGuideContent,
        String nickname,
        String data,
        String to
    ) {
        EmailContent emailContent = EmailContent.builder()
            .emailGuideContent(emailGuideContent)
            .nickname(nickname)
            .data(data)
            .build();

        String convertedContent = EmailMessageMaker.makeEmailMessage(emailContent);

        return EmailMessage.builder()
            .to(to)
            .subject(emailContent.getEmailGuideContent().getMailTitle())
            .message(convertedContent)
            .build();
    }

    private void validateSignUpUserInfo(SignUpRequestDto signUpRequestDto) {
        checkIdDuplicated(CheckIdRequestDto.builder().id(signUpRequestDto.getId()).build());
        checkNicknameDuplicated(
            CheckNicknameRequestDto.builder().nickname(signUpRequestDto.getNickname()).build());
        checkEmailDuplicated(
            CheckEmailRequestDto.builder().email(signUpRequestDto.getEmail()).build());
    }

    private void validateUpdateUserInfo(User user, UpdateUserRequestDto updateUserRequestDto) {
        if (StringUtils.hasText(updateUserRequestDto.getEmail())) {
            checkEmailDuplicated(
                CheckEmailRequestDto.builder().email(updateUserRequestDto.getEmail()).build());
        }

        if (StringUtils.hasText(updateUserRequestDto.getNickname())) {
            checkNicknameDuplicated(
                CheckNicknameRequestDto.builder().nickname(updateUserRequestDto.getNickname())
                    .build());
        }
    }

    private boolean isSamePassword(String answerPassword, String comparePassword) {
        if (!StringUtils.hasText(comparePassword)) {
            return false;
        }

        if (!passwordEncoder.matches(comparePassword, answerPassword)) {
            return false;
        }

        return true;
    }

    private TokenInfo setFirstAuthentication(String id, String password) {
        // 1. id, pw 기반 Authentication 객체 생성, 해당 객체는 인증 여부를 확인하는 authenticated 값이 false.
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(id, password);

        // 2. 검증 진행 - CustomUserDetailsService.loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }
}
