package com.pi.stepup.domain.user.service;

import com.pi.stepup.domain.user.dto.TokenInfo;
import com.pi.stepup.domain.user.dto.UserRequestDto.AuthenticationRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckEmailRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckIdRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.CheckNicknameRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.FindIdRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.FindPasswordRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.ReissueTokensRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.SignUpRequestDto;
import com.pi.stepup.domain.user.dto.UserRequestDto.UpdateUserRequestDto;
import com.pi.stepup.domain.user.dto.UserResponseDto.CountryResponseDto;
import com.pi.stepup.domain.user.dto.UserResponseDto.UserInfoResponseDto;
import java.util.List;

public interface UserService {

    List<CountryResponseDto> readAllCountries();

    void checkEmailDuplicated(CheckEmailRequestDto checkEmailRequestDto);

    void checkNicknameDuplicated(CheckNicknameRequestDto checkNicknameRequestDto);

    void checkIdDuplicated(CheckIdRequestDto checkIdRequestDto);

    TokenInfo login(AuthenticationRequestDto authenticationRequestDto);

    TokenInfo signUp(SignUpRequestDto signUpRequestDto);

    UserInfoResponseDto readOne(String id);

    void delete(String id);

    void checkPassword(AuthenticationRequestDto authenticationRequestDto);

    void update(UpdateUserRequestDto updateUserRequestDto);

    void findId(FindIdRequestDto findIdRequestDto);

    void findPassword(FindPasswordRequestDto findPasswordRequestDto);

    TokenInfo reissueTokens(String refreshToken, ReissueTokensRequestDto reissueTokensRequestDto);
}
