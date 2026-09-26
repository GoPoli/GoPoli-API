package com.gopoli.api.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.dto.MessageDtos;
import com.gopoli.api.dto.MessageDtos.MessageResponse;
import com.gopoli.api.model.Message;
import com.gopoli.api.model.TripMemberId;
import com.gopoli.api.model.User;
import com.gopoli.api.repository.MessageRepository;
import com.gopoli.api.repository.TripMemberRepository;
import com.gopoli.api.repository.UserRepository;
import com.gopoli.api.security.JwtService;

@RestController
public class MessageController {

    private static final String AUTH = "Authorization";
    private static final int MAX_LENGTH = 1000;

    private final MessageRepository messageRepository;
    private final TripMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final Clock clock;

    public MessageController(
            MessageRepository messageRepository,
            TripMemberRepository memberRepository,
            UserRepository userRepository,
            JwtService jwtService,
            Clock clock) {
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    @GetMapping("/trips/{tripId}/messages")
    public ResponseEntity<?> list(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al listar mensajes", () -> {
            if (!isMember(tripId, actor)) {
                return ApiResponses.status(403, "Solo los miembros del grupo pueden ver los mensajes");
            }
            List<Message> messages = messageRepository.findByTripIdOrderBySentAtAsc(tripId);
            Map<Integer, String> names = userRepository
                    .findAllById(messages.stream().map(Message::getUserId).distinct().toList()).stream()
                    .filter(u -> u.getName() != null)
                    .collect(Collectors.toMap(User::getId, User::getName, (a, b) -> a));
            return ResponseEntity.ok(messages.stream().map(m -> toResponse(m, names.get(m.getUserId()))).toList());
        });
    }

    @PostMapping("/trips/{tripId}/messages")
    public ResponseEntity<?> send(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId,
            @RequestBody MessageDtos.SendMessage request) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al enviar mensaje", () -> {
            if (!isMember(tripId, actor)) {
                return ApiResponses.status(403, "Solo los miembros del grupo pueden enviar mensajes");
            }
            if (request.content() == null) {
                return ApiResponses.status(400, "El texto es obligatorio");
            }
            String content = request.content().trim();
            if (content.isEmpty()) {
                return ApiResponses.status(400, "El texto no puede estar vacío");
            }
            if (content.length() > MAX_LENGTH) {
                return ApiResponses.status(400, "El texto no puede superar " + MAX_LENGTH + " caracteres");
            }
            Message message = new Message();
            message.setTripId(tripId);
            message.setUserId(actor);
            message.setContent(content);
            message.setSentAt(LocalDateTime.now(clock));
            Message saved = messageRepository.save(message);
            String name = userRepository.findById(actor).map(User::getName).orElse(null);
            return ResponseEntity.ok(toResponse(saved, name));
        });
    }

    private boolean isMember(Integer tripId, Integer userId) {
        return memberRepository.existsById(new TripMemberId(tripId, userId));
    }

    private static MessageResponse toResponse(Message message, String userName) {
        return new MessageResponse(
                message.getId(),
                message.getTripId(),
                message.getUserId(),
                message.getContent(),
                message.getSentAt(),
                userName);
    }
}
