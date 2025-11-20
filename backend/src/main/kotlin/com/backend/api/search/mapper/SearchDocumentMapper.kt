package com.backend.api.search.mapper

import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.search.PostDocument
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.search.UserDocument
import org.springframework.stereotype.Component

@Component
class SearchDocumentMapper {

    fun toUserDocument(user: User): UserDocument {
        return UserDocument(
            id = user.id.toString(),
            name = user.name,
            nickname = user.nickname,
            email = user.email,
            role = user.role
        )
    }

    fun toPostDocument(post: Post): PostDocument {
        return PostDocument(
            id = post.id.toString(),
            title = post.title,
            introduction = post.introduction,
            content = post.content,
            deadline = post.deadline.toString(),
            recruitCount = post.recruitCount,
            postCategoryType = post.postCategoryType,
            authorNickname = post.users.nickname,
            createdDate = post.createDate.toString(),
            modifyDate = post.modifyDate.toString()
        )
    }
}
