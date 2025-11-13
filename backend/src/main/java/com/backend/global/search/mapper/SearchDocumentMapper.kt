package com.backend.global.search.mapper

interface SearchDocumentMapper<E, D> {
    fun toDocument(entity: E): D
}
