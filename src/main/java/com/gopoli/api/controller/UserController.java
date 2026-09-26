package com.gopoli.api.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.dto.TripDtos;
import com.gopoli.api.dto.TripDtos.HistoryItem;
import com.gopoli.api.dto.TripDtos.MemberResponse;
import com.gopoli.api.dto.UserDto;
import com.gopoli.api.dto.UserDtos;
import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.Location;
import com.gopoli.api.model.Trip;
import com.gopoli.api.model.TripMember;
import com.gopoli.api.model.User;
import com.gopoli.api.model.UserStatus;
import com.gopoli.api.model.Vehicle;
import com.gopoli.api.policy.ProfilePolicy;
import com.gopoli.api.policy.VehicleValidator;
import com.gopoli.api.repository.LocationRepository;
import com.gopoli.api.repository.ProgramRepository;
import com.gopoli.api.repository.TripMemberRepository;
import com.gopoli.api.repository.TripRepository;
import com.gopoli.api.repository.UserRepository;
import com.gopoli.api.repository.VehicleRepository;
import com.gopoli.api.security.JwtService;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private static final String AUTH = "Authorization";
    private static final int MAX_PHOTO_LENGTH = 2_000_000;
    private static final int HISTORY_LIMIT = 10;
    private static final int DEFAULT_VEHICLE_CAPACITY = 4;

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final TripMemberRepository memberRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;
    private final ProgramRepository programRepository;
    private final JwtService jwtService;
    private final TransactionTemplate transaction;

    public UserController(
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            TripMemberRepository memberRepository,
            TripRepository tripRepository,
            LocationRepository locationRepository,
            ProgramRepository programRepository,
            JwtService jwtService,
            TransactionTemplate transaction) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.memberRepository = memberRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
        this.programRepository = programRepository;
        this.jwtService = jwtService;
        this.transaction = transaction;
    }

    private Optional<User> authenticatedUser(String authorization) {
        Integer id = jwtService.parseUserId(authorization);
        return id == null ? Optional.empty() : userRepository.findById(id);
    }

    private static boolean isDisabled(User user) {
        return user.getStatusId() != null && user.getStatusId() == UserStatus.DISABLED;
    }

    private UserDto withVehicle(User user) {
        return UserDto.from(user, vehicleRepository.findByUserId(user.getId()).orElse(null));
    }

    @GetMapping
    public ResponseEntity<?> profile(@RequestHeader(value = AUTH, required = false) String auth) {
        Optional<User> user = authenticatedUser(auth);
        if (user.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        if (isDisabled(user.get())) {
            return ApiResponses.status(403, "Cuenta inhabilitada");
        }
        return ResponseEntity.ok(withVehicle(user.get()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = AUTH, required = false) String auth,
            @RequestBody UserDtos.UpdateProfile request) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        User user = found.get();
        if (isDisabled(user)) {
            return ApiResponses.status(403, "Cuenta inhabilitada");
        }

        if (request.name() != null) {
            String error = ProfilePolicy.nameError(request.name());
            if (error != null) {
                return ApiResponses.status(400, error);
            }
            user.setName(request.name().trim());
        }
        if (request.phone() != null) {
            String error = ProfilePolicy.phoneError(request.phone().trim());
            if (error != null) {
                return ApiResponses.status(400, error);
            }
            user.setPhone(request.phone().trim());
        }
        if (request.email() != null) {
            String error = ProfilePolicy.emailError(request.email());
            if (error != null) {
                return ApiResponses.status(400, error);
            }
            String email = ProfilePolicy.normalizeEmail(request.email());
            Optional<User> other = userRepository.findByEmailIgnoreCase(email);
            if (other.isPresent() && !other.get().getId().equals(user.getId())) {
                return ApiResponses.status(409, "El correo ya está en uso");
            }
            user.setEmail(email);
        }
        if (request.programId() != null) {
            if (!programRepository.existsById(request.programId())) {
                return ApiResponses.status(400, "La carrera seleccionada no existe");
            }
            user.setProgramId(request.programId());
        }
        return ResponseEntity.ok(withVehicle(userRepository.save(user)));
    }

    @PutMapping("/photo")
    public ResponseEntity<?> updatePhoto(
            @RequestHeader(value = AUTH, required = false) String auth,
            @RequestBody UserDtos.UpdatePhoto request) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        String photo = request.photoBase64();
        if (photo == null || photo.isBlank()) {
            return ApiResponses.status(400, "La foto es obligatoria");
        }
        if (photo.length() > MAX_PHOTO_LENGTH) {
            return ApiResponses.status(400, "La imagen es demasiado grande");
        }
        User user = found.get();
        user.setProfilePhoto(photo);
        return ResponseEntity.ok(withVehicle(userRepository.save(user)));
    }

    @PostMapping("/driver")
    public ResponseEntity<?> registerDriver(
            @RequestHeader(value = AUTH, required = false) String auth,
            @RequestBody UserDtos.RegisterDriver request) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        User user = found.get();
        if (GoPoliConstants.isDriver(user.getUserTypeId())) {
            return ApiResponses.status(400, "Ya eres conductor");
        }
        Map<String, String> errors = VehicleValidator.validate(
                request.brand(), request.model(), request.color(), request.plate());
        if (!errors.isEmpty()) {
            return ApiResponses.status(400, errors);
        }
        String plate = VehicleValidator.normalizePlate(request.plate());
        if (vehicleRepository.existsByPlate(plate)) {
            return ApiResponses.status(409, Map.of("plate", "Esta placa ya está registrada"));
        }

        return ApiResponses.guarded("Error al registrar el vehículo", () -> {
            User saved = transaction.execute(status -> {
                Vehicle vehicle = new Vehicle();
                vehicle.setUserId(user.getId());
                vehicle.setBrand(request.brand().trim());
                vehicle.setModel(request.model().trim());
                vehicle.setColor(request.color().trim());
                vehicle.setPlate(plate);
                vehicle.setCapacity(DEFAULT_VEHICLE_CAPACITY);
                vehicleRepository.save(vehicle);
                user.setUserTypeId(GoPoliConstants.USER_TYPE_DRIVER);
                return userRepository.save(user);
            });
            return ResponseEntity.ok(withVehicle(saved));
        });
    }

    @DeleteMapping("/driver")
    public ResponseEntity<?> unregisterDriver(@RequestHeader(value = AUTH, required = false) String auth) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        User user = found.get();
        if (!GoPoliConstants.isDriver(user.getUserTypeId())) {
            return ApiResponses.status(400, "No eres conductor");
        }
        if (hasDriverTrip(user.getId(), GoPoliConstants.TRIP_STATUS_ACTIVE)) {
            return ApiResponses.status(400,
                    "Tienes un viaje conductor activo. Cancélalo o finalízalo antes de dejar de ser conductor.");
        }
        if (hasDriverTrip(user.getId(), GoPoliConstants.TRIP_STATUS_IN_PROGRESS)) {
            return ApiResponses.status(400,
                    "Tienes un viaje conductor en curso. Finalízalo antes de dejar de ser conductor.");
        }

        return ApiResponses.guarded("Error al dejar de ser conductor", () -> {
            User saved = transaction.execute(status -> {
                vehicleRepository.findByUserId(user.getId()).ifPresent(vehicleRepository::delete);
                user.setUserTypeId(GoPoliConstants.USER_TYPE_PASSENGER);
                return userRepository.save(user);
            });
            return ResponseEntity.ok(withVehicle(saved));
        });
    }

    private boolean hasDriverTrip(Integer userId, int statusId) {
        return tripRepository.findByCreatorIdAndStatusId(userId, statusId).stream()
                .anyMatch(t -> GoPoliConstants.isDriverTrip(t.getTripTypeId()));
    }

    @GetMapping("/trip-history")
    public ResponseEntity<?> tripHistory(@RequestHeader(value = AUTH, required = false) String auth) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al consultar el historial", () ->
                ResponseEntity.ok(buildHistory(found.get().getId())));
    }

    private List<HistoryItem> buildHistory(Integer userId) {
        Map<Integer, TripMember> myMemberships = memberRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(TripMember::getTripId, Function.identity(), (a, b) -> a));
        List<Trip> finished = tripRepository.findAllById(myMemberships.keySet()).stream()
                .filter(t -> t.getStatusId() != null && t.getStatusId() == GoPoliConstants.TRIP_STATUS_FINISHED)
                .sorted(Comparator
                        .comparing(Trip::getDepartureDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Trip::getDepartureTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(HISTORY_LIMIT)
                .toList();
        if (finished.isEmpty()) {
            return List.of();
        }

        List<Integer> tripIds = finished.stream().map(Trip::getId).toList();
        Map<Integer, List<TripMember>> membersByTrip = memberRepository.findByTripIdIn(tripIds).stream()
                .collect(Collectors.groupingBy(TripMember::getTripId));
        Set<Integer> userIds = membersByTrip.values().stream()
                .flatMap(List::stream)
                .map(TripMember::getUserId)
                .collect(Collectors.toSet());
        Map<Integer, String> userNames = userRepository.findAllById(userIds).stream()
                .filter(u -> u.getName() != null)
                .collect(Collectors.toMap(User::getId, User::getName, (a, b) -> a));
        Set<Integer> locationIds = finished.stream()
                .flatMap(t -> Stream.of(t.getDepartureLocationId(), t.getArrivalLocationId()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Integer, String> locationNames = locationRepository.findAllById(locationIds).stream()
                .filter(l -> l.getName() != null)
                .collect(Collectors.toMap(Location::getId, Location::getName, (a, b) -> a));

        List<HistoryItem> history = new ArrayList<>();
        for (Trip trip : finished) {
            TripMember mine = myMemberships.get(trip.getId());
            List<MemberResponse> participants = membersByTrip.getOrDefault(trip.getId(), List.of()).stream()
                    .map(m -> new MemberResponse(
                            m.getUserId(),
                            m.getGroupRole(),
                            m.getParticipationRole(),
                            TripDtos.participationLabel(m.getParticipationRole()),
                            userNames.get(m.getUserId())))
                    .toList();
            history.add(new HistoryItem(
                    trip.getId(),
                    trip.getDepartureDate(),
                    trip.getDepartureTime(),
                    trip.getDescription(),
                    trip.getTripTypeId(),
                    TripDtos.tripTypeKey(trip.getTripTypeId()),
                    TripDtos.tripTypeLabel(trip.getTripTypeId()),
                    trip.getDepartureLocationId(),
                    trip.getArrivalLocationId(),
                    locationNames.get(trip.getDepartureLocationId()),
                    locationNames.get(trip.getArrivalLocationId()),
                    mine.getParticipationRole(),
                    TripDtos.participationLabel(mine.getParticipationRole()),
                    participants));
        }
        return history;
    }

    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivate(@RequestHeader(value = AUTH, required = false) String auth) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        User user = found.get();
        user.setStatusId(UserStatus.DISABLED);
        userRepository.save(user);
        return ResponseEntity.ok("Cuenta inhabilitada");
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestHeader(value = AUTH, required = false) String auth) {
        Optional<User> found = authenticatedUser(auth);
        if (found.isEmpty()) {
            return ApiResponses.unauthorized();
        }
        User user = found.get();
        return ApiResponses.guarded("Error al eliminar la cuenta", () -> {
            transaction.executeWithoutResult(status -> {
                vehicleRepository.findByUserId(user.getId()).ifPresent(vehicleRepository::delete);
                userRepository.delete(user);
            });
            return ResponseEntity.ok("Cuenta eliminada permanentemente");
        });
    }
}
