package com.backend.domain.post.entity.search

import com.backend.domain.post.entity.PostCategoryType
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

@Document(indexName = "posts", createIndex = false)
data class PostDocument(

    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    val title: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    val introduction: String,

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    val content: String,

    @Field(type = FieldType.Keyword)
    val postCategoryType: PostCategoryType,

    @Field(type = FieldType.Keyword)
    val authorNickname: String,

    @Field(type = FieldType.Date)
    val createdDate: String,

    @Field(type = FieldType.Date)
    val updatedDate: String
)
