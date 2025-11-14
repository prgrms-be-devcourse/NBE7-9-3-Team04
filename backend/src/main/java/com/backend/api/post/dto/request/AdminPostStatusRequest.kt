package com.backend.api.post.dto.request;

import com.backend.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AdminPostStatusRequest(
        @NotNull(message = "게시글 상태는 필수입니다.")
        @Schema(description = "변경할 게시글 상태", example = "CLOSED")
        PostStatus status
) {}
