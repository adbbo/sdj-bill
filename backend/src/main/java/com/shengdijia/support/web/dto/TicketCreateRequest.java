package com.shengdijia.support.web.dto;

import com.shengdijia.support.domain.Category;
import com.shengdijia.support.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank(message = "请填写问题摘要")
        @Size(max = 200, message = "摘要不超过 200 字")
        String title,

        @NotBlank(message = "请填写问题详情")
        @Size(max = 8000, message = "详情过长")
        String description,

        @NotNull(message = "请选择影响程度")
        Priority priority,

        @NotNull(message = "请选择问题分类")
        Category category,

        String contactPhone,
        String contactEmail,
        String merchantId
) {
}
