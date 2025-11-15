package com.backend.domain.ranking.entity

import lombok.AllArgsConstructor


@AllArgsConstructor
enum class Tier(
    val label: String,
    val minScore: Int
) {
    UNRATED("Unrated", 0),
    BRONZE("Bronze", 300),
    SILVER("Silver", 600),
    GOLD("Gold", 900),
    PLATINUM("Platinum", 1200),
    DIAMOND("Diamond", 1500),
    RUBY("Ruby", 1800),
    MASTER("Master", 2100);



    companion object {
        //현재 score로 티어 계산
        @JvmStatic
        fun fromScore(score: Int): Tier =
            entries
                .filter { score >= it.minScore }
                .maxByOrNull { it.minScore }
                ?: UNRATED
    }

    //다음 티어 계산. master 찍으면 master를 반환한다
    fun nextTier(): Tier {

        val values= entries
        val index = this.ordinal

        return if(index < values.size-1){
            values[index+1]
        }
        else{
            values[index]
        }
    }

}
