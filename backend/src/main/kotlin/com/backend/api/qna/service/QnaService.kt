package com.backend.api.qna.service

import com.backend.api.qna.dto.request.QnaAddRequest
import com.backend.api.qna.dto.request.QnaUpdateRequest
import com.backend.api.qna.dto.response.QnaPageResponse
import com.backend.api.qna.dto.response.QnaResponse
import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import com.backend.domain.qna.repository.QnaRepository
import com.backend.domain.user.entity.Role
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
class QnaService(
    private val qnaRepository: QnaRepository
) {

    private fun validateUserAuthority(user: User) {
        if (user.role != Role.USER) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateQnaAuthor(qna: Qna, user: User) {
        if (qna.author.id != user.id) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateAdminAnswerExist(qna: Qna) {
        if (qna.isAnswered) {
            throw ErrorException(ErrorCode.QNA_ADMIN_ANSWER_FINISHED)
        }
    }

    @Transactional
    fun addQna(@Valid request: QnaAddRequest, user: User): QnaResponse {
        validateUserAuthority(user)

        val qna = Qna(
            title = request.title,
            content = request.content,
            author = user,
            categoryType = request.categoryType
        )

        val saved = qnaRepository.save(qna)
        return QnaResponse.from(saved)
    }

    @Transactional
    fun updateQna(qnaId: Long, request: QnaUpdateRequest, user: User): QnaResponse {
        validateUserAuthority(user)

        val qna = findByIdOrThrow(qnaId)
        validateAdminAnswerExist(qna)
        validateQnaAuthor(qna, user)

        qna.updateQna(
            title = request.title,
            content = request.content,
            categoryType = request.categoryType
        )

        return QnaResponse.from(qna)
    }

    @Transactional
    fun deleteQna(qnaId: Long, user: User) {
        validateUserAuthority(user)

        val qna = findByIdOrThrow(qnaId)
        validateAdminAnswerExist(qna)
        validateQnaAuthor(qna, user)

        qnaRepository.delete(qna)
    }

    fun getQna(qnaId: Long): QnaResponse {
        val qna = findByIdOrThrow(qnaId)
        return QnaResponse.from(qna)
    }

    fun getQnaAll(page: Int, categoryType: QnaCategoryType?): QnaPageResponse<QnaResponse> {
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

    private fun findByIdOrThrow(qnaId: Long): Qna {
        return qnaRepository.findByIdOrNull(qnaId)
            ?: throw ErrorException(ErrorCode.QNA_NOT_FOUND)
    }
}
