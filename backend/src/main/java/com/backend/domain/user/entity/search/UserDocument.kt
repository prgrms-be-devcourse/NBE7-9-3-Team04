package com.backend.domain.user.entity.search

import com.backend.domain.user.entity.Role
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "users", createIndex = false)
open class UserDocument(

    @Id
    var id: String? = null,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    var name: String? = null,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    var nickname: String? = null,

    @Field(type = FieldType.Keyword)
    var email: String? = null,

    @Field(type = FieldType.Keyword)
    var role: Role? = null

) {
    protected constructor() : this(null, null, null, null, null)

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var id: String? = null
        private var name: String? = null
        private var nickname: String? = null
        private var email: String? = null
        private var role: Role? = null

        fun id(id: String) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun nickname(nickname: String) = apply { this.nickname = nickname }
        fun email(email: String) = apply { this.email = email }
        fun role(role: Role) = apply { this.role = role }

        fun build(): UserDocument {
            return UserDocument(
                id = id,
                name = name,
                nickname = nickname,
                email = email,
                role = role
            )
        }
    }
}
