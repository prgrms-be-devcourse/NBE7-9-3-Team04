package com.backend.api.qna.service

import com.backend.api.qna.dto.request.QnaAnswerRequest
import com.backend.api.qna.dto.response.QnaPageResponse
import com.backend.api.qna.dto.response.QnaResponse
import com.backend.api.user.service.AdminUserService
import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import com.backend.domain.qna.repository.QnaRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.repository.findByIdOrNull

@Service
@Transactional(readOnly = true)
class AdminQnaService(
    private val qnaRepository: QnaRepository,
    private val adminUserService: AdminUserService
) {

    private fun findByIdOrThrow(qnaId: Long): Qna {
        return qnaRepository.findByIdOrNull(qnaId)
            ?: throw ErrorException(ErrorCode.QNA_NOT_FOUND)
    }

    @Transactional
    fun registerAnswer(qnaId: Long, @Valid request: QnaAnswerRequest, user: User?): QnaResponse {
        adminUserService.validateAdminAuthority(user)

        val qna = findByIdOrThrow(qnaId)

        if (qna.isAnswered) {
            throw ErrorException(ErrorCode.QNA_ALREADY_ANSWERED)
        }

        qna.registerAnswer(request.answer)
        return QnaResponse.from(qna)
    }

    @Transactional
    fun deleteQna(qnaId: Long, user: User?) {
        adminUserService.validateAdminAuthority(user)

        val qna = findByIdOrThrow(qnaId)
        qnaRepository.delete(qna)
    }

    fun getAllQna(page: Int, user: User?, categoryType: QnaCategoryType?): QnaPageResponse<QnaResponse> {
        adminUserService.validateAdminAuthority(user)

        val pageIndex = (page.takeIf { it > 0 } ?: 1) - 1

        val pageable: Pageable = PageRequest.of(
            pageIndex,
            15,
            Sort.by("createDate").descending()
        )

        val qnaPage = if (categoryType == null) {
            qnaRepository.findAll(pageable)
        } else {
            qnaRepository.findByCategoryType(categoryType, pageable)
        }

        if (qnaPage.isEmpty) {
            throw ErrorException(ErrorCode.QNA_NOT_FOUND)
        }

        val qnaList = qnaPage.content.map { QnaResponse.from(it) }

        return QnaPageResponse.from(qnaPage, qnaList)
    }

    fun getQna(qnaId: Long, user: User?): QnaResponse {
        adminUserService.validateAdminAuthority(user)

        val qna = findByIdOrThrow(qnaId)
        return QnaResponse.from(qna)
    }
}
