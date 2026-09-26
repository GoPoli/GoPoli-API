package com.gopoli.api.dto;

import java.time.LocalDateTime;

public final class MessageDtos {

    private MessageDtos() {
    }

    public record SendMessage(String content) {
    }

    public record MessageResponse(
            Integer id,
            Integer tripId,
            Integer userId,
            String content,
            LocalDateTime sentAt,
            String userName) {
    }
}
