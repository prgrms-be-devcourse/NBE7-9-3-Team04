package com.backend.domain.qna.repository

import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface QnaRepository : JpaRepository<Qna, Long> {

    @EntityGraph(attributePaths = ["author"])
    fun findByCategoryType(categoryType: QnaCategoryType, pageable: Pageable): Page<Qna>
}
