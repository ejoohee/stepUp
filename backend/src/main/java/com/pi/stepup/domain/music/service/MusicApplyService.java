package com.pi.stepup.domain.music.service;

import com.pi.stepup.domain.music.dto.MusicRequestDto.HeartSaveRequestDto;
import com.pi.stepup.domain.music.dto.MusicRequestDto.MusicApplySaveRequestDto;
import com.pi.stepup.domain.music.dto.MusicResponseDto.MusicApplyFindResponseDto;
import java.util.List;

public interface MusicApplyService {

    void create(MusicApplySaveRequestDto musicApplySaveRequestDto);

    List<MusicApplyFindResponseDto> readAllByKeyword(String keyword);

    MusicApplyFindResponseDto readOne(String id, Long musicApplyId);

    void delete(Long musicApplyId);

    List<MusicApplyFindResponseDto> readAllById();

    void createHeart(HeartSaveRequestDto heartSaveRequestDto);

    void deleteHeart(String id, Long musicRequestId);

    Integer findHeartStatus(String id, Long musicApplyId);
}
