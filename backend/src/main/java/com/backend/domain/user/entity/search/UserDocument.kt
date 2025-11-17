package com.backend.domain.user.entity.search

import com.backend.domain.user.entity.Role
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "users", createIndex = false)
class UserDocument(

    @Id
    var id: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    var name: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    var nickname: String,

    @Field(type = FieldType.Keyword)
    var email: String,

    @Field(type = FieldType.Keyword)
    var role: Role

) {

    companion object {
        // Kotlin 팩토리 메서드
        fun from(user: com.backend.domain.user.entity.User): UserDocument {
            return UserDocument(
                id = user.id.toString(),
                name = user.name,
                nickname = user.nickname,
                email = user.email,
                role = user.role
            )
        }
    }
}
