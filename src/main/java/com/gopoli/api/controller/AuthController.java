package com.gopoli.api.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.dto.AuthDtos;
import com.gopoli.api.dto.UserDto;
import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.User;
import com.gopoli.api.model.UserStatus;
import com.gopoli.api.policy.ProfilePolicy;
import com.gopoli.api.repository.ProgramRepository;
import com.gopoli.api.repository.UserRepository;
import com.gopoli.api.security.JwtService;
import com.gopoli.api.security.PasswordService;

@RestController
public class AuthController {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    public AuthController(
            UserRepository userRepository,
            ProgramRepository programRepository,
            JwtService jwtService,
            PasswordService passwordService) {
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.Register request) {
        if (request.email() == null || request.email().isBlank()) {
            return ApiResponses.status(400, "El correo es obligatorio");
        }
        String emailError = ProfilePolicy.emailError(request.email());
        if (emailError != null) {
            return ApiResponses.status(400, emailError);
        }
        if (request.password() == null
                || request.password().length() < MIN_PASSWORD_LENGTH
                || exceedsBcryptLimit(request.password())) {
            return ApiResponses.status(400, "La contraseña debe tener entre 8 y 72 caracteres");
        }
        String nameError = ProfilePolicy.nameError(request.name());
        if (nameError != null) {
            return ApiResponses.status(400, nameError);
        }
        String phone = request.phone() == null || request.phone().isBlank() ? null : request.phone().trim();
        String phoneError = phone == null ? null : ProfilePolicy.phoneError(phone);
        if (phoneError != null) {
            return ApiResponses.status(400, phoneError);
        }
        String email = ProfilePolicy.normalizeEmail(request.email());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ApiResponses.status(409, "El correo ya está registrado");
        }
        if (request.programId() != null && !programRepository.existsById(request.programId())) {
            return ApiResponses.status(400, "La carrera seleccionada no existe");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordService.hash(request.password()));
        user.setName(request.name().trim());
        user.setPhone(phone);
        user.setProgramId(request.programId());
        user.setRating(0.0);
        user.setStatusId(UserStatus.ACTIVE);
        user.setUserTypeId(GoPoliConstants.USER_TYPE_PASSENGER);
        return ResponseEntity.ok(UserDto.from(userRepository.save(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.Login request) {
        if (request.email() == null || request.password() == null) {
            return ApiResponses.status(400, "Correo y contraseña son obligatorios");
        }
        if (exceedsBcryptLimit(request.password())) {
            return ApiResponses.status(401, "Correo o contraseña incorrectos");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null || !passwordService.matches(request.password(), user.getPassword())) {
            return ApiResponses.status(401, "Correo o contraseña incorrectos");
        }
        if (user.getStatusId() != null && user.getStatusId() == UserStatus.DISABLED) {
            return ApiResponses.status(403, "Tu cuenta está inhabilitada");
        }

        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(new AuthDtos.LoginResponse(token, UserDto.from(user)));
    }

    private static boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }
}
