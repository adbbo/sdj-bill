package com.shengdijia.support.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RemarkRequest(
        @NotBlank(message = "请填写备注内容")
        @Size(max = 2000, message = "备注不超过 2000 字")
        String content
) {
}
