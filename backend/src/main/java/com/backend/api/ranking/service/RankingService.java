package com.backend.api.ranking.service;

import com.backend.api.question.service.QuestionService;
import com.backend.api.ranking.dto.response.RankingResponse;
import com.backend.api.ranking.dto.response.RankingSummaryResponse;
import com.backend.api.userQuestion.service.UserQuestionService;
import com.backend.domain.ranking.entity.Ranking;
import com.backend.domain.ranking.entity.Tier;
import com.backend.domain.ranking.repository.RankingRepository;
import com.backend.domain.user.entity.User;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final UserQuestionService userQuestionService;
    private final QuestionService questionService;

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REDIS_PREFIX = "ranking_";

    @Transactional
    public Ranking createRanking(User user) {

        if (rankingRepository.existsByUser(user)) {
            throw new ErrorException(ErrorCode.RANKING_ALREADY_EXISTS);
        }

        Ranking ranking = Ranking.builder()
                .user(user)
                .totalScore(0)
                .tier(Tier.UNRATED)
                .rankValue(0)
                .build();

        return rankingRepository.save(ranking);
    }


    @Transactional
    public void updateUserRanking(User user){


        int totalScore = userQuestionService.getTotalUserQuestionScore(user);

        if(totalScore < 0){
            throw new ErrorException(ErrorCode.INVALID_SCORE);
        }

        Ranking ranking = rankingRepository.findByUser(user)
                .orElseGet(() -> createRanking(user)); // 존재하지 않으면 생성

        // 점수 / 티어 업데이트
        ranking.updateTotalScore(totalScore);
        ranking.updateTier(Tier.fromScore(totalScore));

        rankingRepository.save(ranking);

        stringRedisTemplate.opsForZSet().add(
                REDIS_PREFIX,
                String.valueOf(user.getId()),
                totalScore
        );


    }

    //마이페이지용
    @Transactional(readOnly = true)
    public RankingResponse getMyRanking(User user) {
        Ranking ranking = rankingRepository.findByUser(user)
                .orElseThrow(() -> new ErrorException(ErrorCode.RANKING_NOT_FOUND));

        Long rankIndex = stringRedisTemplate.opsForZSet()
                .reverseRank(REDIS_PREFIX, String.valueOf(user.getId()));

        if (rankIndex == null) {
            // Redis에 랭킹 정보가 없는 경우
            throw new ErrorException(ErrorCode.RANKING_NOT_AVAILABLE);
        }

        int rankValue = rankIndex.intValue() + 1;

        int solvedCount = userQuestionService.countSolvedQuestion(user);
        int questionCount = questionService.countByUser(user);

        return RankingResponse.from(ranking, rankValue, solvedCount, questionCount);
    }


    //상위 10명
    @Transactional(readOnly = true)
    public List<RankingResponse> getTopRankings() {

        Set<ZSetOperations.TypedTuple<String>> topRanks = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_PREFIX, 0, 9);

        if (topRanks == null || topRanks.isEmpty()) {
            throw new ErrorException(ErrorCode.RANKING_NOT_AVAILABLE);
        }

        //Redis 결과에서 사용자 ID, 점수 추출
        List<Long> userIds = topRanks.stream()
                .map(tuple -> Long.valueOf(tuple.getValue()))
                .toList();

        List<Ranking> dbRankings = rankingRepository.findByUser_IdIn(userIds);

        // Redis 순위와 DB 정보를 결합
        List<RankingResponse> responses = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : topRanks) {
            Long userId = Long.valueOf(tuple.getValue());

            Ranking ranking = dbRankings.stream()
                    .filter(r -> r.getUser().getId()==userId)
                    .findFirst()
                    .orElse(null);

            if (ranking != null) {
                int solved = userQuestionService.countSolvedQuestion(ranking.getUser());
                int submitted = questionService.countByUser(ranking.getUser());

                // Redis에서 가져온 rank 사용
                responses.add(RankingResponse.from(ranking, rank, solved, submitted));
            }
            rank++;
        }
        return responses;
    }

    //상위 10명 + 내 랭킹
    @Transactional(readOnly = true)
    public RankingSummaryResponse getRankingSummary(User user) {

        RankingResponse myRanking = getMyRanking(user);
        List<RankingResponse> topRankings = getTopRankings();

        return RankingSummaryResponse.from(myRanking, topRankings);
    }
}
