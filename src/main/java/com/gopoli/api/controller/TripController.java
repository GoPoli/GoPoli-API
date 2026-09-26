package com.gopoli.api.controller;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.dto.TripDtos;
import com.gopoli.api.dto.TripDtos.MemberResponse;
import com.gopoli.api.dto.TripDtos.TripResponse;
import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.Trip;
import com.gopoli.api.model.TripMember;
import com.gopoli.api.model.TripMemberId;
import com.gopoli.api.model.User;
import com.gopoli.api.policy.TripPolicy;
import com.gopoli.api.repository.LocationRepository;
import com.gopoli.api.repository.TripMemberRepository;
import com.gopoli.api.repository.TripRepository;
import com.gopoli.api.repository.UserRepository;
import com.gopoli.api.security.JwtService;

@RestController
public class TripController {

    private static final String AUTH = "Authorization";

    private final TripRepository tripRepository;
    private final TripMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final JwtService jwtService;
    private final TransactionTemplate transaction;
    private final Clock clock;

    public TripController(
            TripRepository tripRepository,
            TripMemberRepository memberRepository,
            UserRepository userRepository,
            LocationRepository locationRepository,
            JwtService jwtService,
            TransactionTemplate transaction,
            Clock clock) {
        this.tripRepository = tripRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.jwtService = jwtService;
        this.transaction = transaction;
        this.clock = clock;
    }

    @PostMapping("/trips")
    public ResponseEntity<?> create(
            @RequestHeader(value = AUTH, required = false) String auth,
            @RequestBody TripDtos.CreateTrip request) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al crear el servicio", () -> createTrip(actor, request));
    }

    private ResponseEntity<?> createTrip(Integer actor, TripDtos.CreateTrip request) {
        String creationError = TripPolicy.creationError(
                request.departureLocationId(), request.arrivalLocationId(), request.capacity());
        if (creationError != null) {
            return ApiResponses.status(400, creationError);
        }
        String descriptionError = TripPolicy.descriptionError(request.description());
        if (descriptionError != null) {
            return ApiResponses.status(400, descriptionError);
        }
        if (!locationRepository.existsById(request.departureLocationId())
                || !locationRepository.existsById(request.arrivalLocationId())) {
            return ApiResponses.status(400, "La salida o el destino no existe");
        }
        LocalDate today = LocalDate.now(clock);
        if (request.departureDate() == null) {
            return ApiResponses.status(400, "La fecha es obligatoria");
        }
        if (request.departureDate().isBefore(today)) {
            return ApiResponses.status(400, "La fecha no puede ser en el pasado");
        }
        if (request.departureTime() == null) {
            return ApiResponses.status(400, "La hora de salida es obligatoria");
        }
        if (request.departureDate().isEqual(today) && request.departureTime().isBefore(LocalTime.now(clock))) {
            return ApiResponses.status(400, "La hora no puede ser en el pasado");
        }
        Integer tripTypeId = request.tripTypeId() == null
                ? GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP
                : request.tripTypeId();
        if (!TripPolicy.isValidTripType(tripTypeId)) {
            return ApiResponses.status(400, "Tipo de viaje no válido");
        }
        Optional<User> creator = userRepository.findById(actor);
        if (creator.isEmpty()) {
            return ApiResponses.status(404, "Usuario creador no encontrado");
        }
        if (GoPoliConstants.isDriverTrip(tripTypeId) && !GoPoliConstants.isDriver(creator.get().getUserTypeId())) {
            return ApiResponses.status(403, "Solo un conductor puede crear viajes tipo grupo conductor");
        }
        if (!tripRepository.findByCreatorIdAndStatusId(actor, GoPoliConstants.TRIP_STATUS_ACTIVE).isEmpty()) {
            return ApiResponses.status(400, "Ya tienes un servicio activo, no puedes crear otro");
        }

        Trip saved = transaction.execute(status -> {
            Trip trip = new Trip();
            trip.setDepartureDate(request.departureDate());
            trip.setDepartureTime(request.departureTime());
            trip.setDescription(TripPolicy.normalizeDescription(request.description()));
            trip.setDepartureLocationId(request.departureLocationId());
            trip.setArrivalLocationId(request.arrivalLocationId());
            trip.setCapacity(request.capacity());
            trip.setTripTypeId(tripTypeId);
            trip.setCreatorId(actor);
            trip.setStatusId(GoPoliConstants.TRIP_STATUS_ACTIVE);
            Trip persisted = tripRepository.save(trip);

            TripMember member = new TripMember();
            member.setTripId(persisted.getId());
            member.setUserId(actor);
            member.setGroupRole(GoPoliConstants.GROUP_ROLE_CREATOR);
            member.setParticipationRole(GoPoliConstants.isDriverTrip(tripTypeId)
                    ? GoPoliConstants.PARTICIPATION_DRIVER
                    : GoPoliConstants.PARTICIPATION_PASSENGER);
            memberRepository.save(member);
            return persisted;
        });
        return ResponseEntity.ok(TripResponse.from(saved));
    }

    @PutMapping("/trips/{tripId}/cancel")
    public ResponseEntity<?> cancel(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        return changeStatus(auth, tripId, GoPoliConstants.TRIP_STATUS_CANCELLED, "Servicio cancelado", "Error al cancelar");
    }

    @PutMapping("/trips/{tripId}/finish")
    public ResponseEntity<?> finish(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        return changeStatus(auth, tripId, GoPoliConstants.TRIP_STATUS_FINISHED, "Viaje finalizado", "Error al finalizar");
    }

    @PutMapping("/trips/{tripId}/start")
    public ResponseEntity<?> start(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        return changeStatus(auth, tripId, GoPoliConstants.TRIP_STATUS_IN_PROGRESS, "Viaje iniciado", "Error al iniciar");
    }

    private ResponseEntity<?> changeStatus(String auth, Integer tripId, int targetStatus, String success, String failure) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded(failure, () -> {
            Optional<Trip> found = tripRepository.findById(tripId);
            if (found.isEmpty()) {
                return ApiResponses.status(404, "Servicio no encontrado");
            }
            Trip trip = found.get();
            if (!actor.equals(trip.getCreatorId())) {
                return ApiResponses.status(403, "Solo el creador puede cambiar el estado del viaje");
            }
            String transitionError = TripPolicy.transitionError(trip.getStatusId(), targetStatus);
            if (transitionError != null) {
                return ApiResponses.status(409, transitionError);
            }
            trip.setStatusId(targetStatus);
            tripRepository.save(trip);
            return ResponseEntity.ok(success);
        });
    }

    @GetMapping("/trips/{tripId}/members")
    public ResponseEntity<?> members(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al traer miembros", () -> {
            if (!memberRepository.existsById(new TripMemberId(tripId, actor))) {
                return ApiResponses.status(403, "Solo los miembros pueden consultar el grupo");
            }
            List<TripMember> members = memberRepository.findByTripId(tripId);
            Map<Integer, String> names = userNames(members.stream().map(TripMember::getUserId).toList());
            List<MemberResponse> result = members.stream()
                    .map(m -> new MemberResponse(
                            m.getUserId(),
                            m.getGroupRole(),
                            m.getParticipationRole(),
                            TripDtos.participationLabel(m.getParticipationRole()),
                            names.get(m.getUserId())))
                    .toList();
            return ResponseEntity.ok(result);
        });
    }

    @GetMapping("/trips/active")
    public ResponseEntity<?> activeTrips(@RequestHeader(value = AUTH, required = false) String auth) {
        if (jwtService.parseUserId(auth) == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al listar servicios", () -> ResponseEntity.ok(
                tripRepository.findByStatusId(GoPoliConstants.TRIP_STATUS_ACTIVE).stream()
                        .map(TripResponse::from)
                        .toList()));
    }

    @PostMapping("/trips/{tripId}/join")
    public ResponseEntity<?> join(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al unirse", () -> {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip == null) {
                return ApiResponses.status(404, "Servicio no encontrado");
            }
            if (trip.getStatusId() != GoPoliConstants.TRIP_STATUS_ACTIVE) {
                return ApiResponses.status(400, "El servicio no está activo");
            }
            if (trip.getCreatorId().equals(actor)) {
                return ApiResponses.status(400, "Ya eres el creador de este grupo");
            }
            List<Integer> myTripIds = memberRepository.findByUserId(actor).stream()
                    .map(TripMember::getTripId)
                    .toList();
            boolean busy = tripRepository.findAllById(myTripIds).stream()
                    .anyMatch(t -> t.getStatusId() == GoPoliConstants.TRIP_STATUS_ACTIVE
                            || t.getStatusId() == GoPoliConstants.TRIP_STATUS_IN_PROGRESS);
            if (busy) {
                return ApiResponses.status(400, "Ya perteneces a un grupo activo o en curso");
            }
            if (memberRepository.countByTripId(tripId) >= trip.getCapacity()) {
                return ApiResponses.status(400, "El grupo está lleno");
            }
            TripMember member = new TripMember();
            member.setTripId(tripId);
            member.setUserId(actor);
            member.setGroupRole(GoPoliConstants.GROUP_ROLE_MEMBER);
            member.setParticipationRole(GoPoliConstants.PARTICIPATION_PASSENGER);
            memberRepository.save(member);
            return ResponseEntity.ok("Te uniste al grupo exitosamente");
        });
    }

    @DeleteMapping("/trips/{tripId}/members/{userId}")
    public ResponseEntity<?> leave(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId,
            @PathVariable Integer userId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        if (!actor.equals(userId)) {
            return ApiResponses.status(403, "No puedes salir en nombre de otra persona");
        }
        return ApiResponses.guarded("Error al salir", () -> {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip == null) {
                return ApiResponses.status(404, "Servicio no encontrado");
            }
            if (trip.getCreatorId().equals(actor)) {
                return ApiResponses.status(400, "El creador no puede salir. Cancela el viaje.");
            }
            if (trip.getStatusId() != GoPoliConstants.TRIP_STATUS_ACTIVE) {
                return ApiResponses.status(400, "Solo puedes salir de grupos en planificación");
            }
            TripMemberId id = new TripMemberId(tripId, userId);
            if (!memberRepository.existsById(id)) {
                return ApiResponses.status(404, "No eres miembro de este grupo");
            }
            memberRepository.deleteById(id);
            return ResponseEntity.ok("Saliste del grupo");
        });
    }

    @GetMapping("/users/{userId}/trips/active")
    public ResponseEntity<?> activeTripOfUser(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer userId) {
        ResponseEntity<?> denied = requireOwner(auth, userId);
        if (denied != null) {
            return denied;
        }
        return ApiResponses.guarded("Error al consultar el servicio activo", () ->
                tripRepository.findByCreatorIdAndStatusId(userId, GoPoliConstants.TRIP_STATUS_ACTIVE).stream()
                        .findFirst()
                        .<ResponseEntity<?>>map(t -> ResponseEntity.ok(TripResponse.from(t)))
                        .orElse(ApiResponses.status(404, "Sin servicio activo")));
    }

    @GetMapping("/users/{userId}/trips/member")
    public ResponseEntity<?> activeGroupOfUser(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer userId) {
        ResponseEntity<?> denied = requireOwner(auth, userId);
        if (denied != null) {
            return denied;
        }
        return ApiResponses.guarded("Error al consultar el grupo", () ->
                firstMembershipWithStatus(userId, GoPoliConstants.TRIP_STATUS_ACTIVE)
                        .<ResponseEntity<?>>map(t -> ResponseEntity.ok(TripResponse.from(t)))
                        .orElse(ApiResponses.status(404, "Sin grupo activo")));
    }

    @GetMapping("/users/{userId}/trips/in-progress")
    public ResponseEntity<?> tripInProgressOfUser(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer userId) {
        ResponseEntity<?> denied = requireOwner(auth, userId);
        if (denied != null) {
            return denied;
        }
        return ApiResponses.guarded("Error al consultar el viaje en curso", () -> {
            Optional<Trip> asCreator = tripRepository
                    .findByCreatorIdAndStatusId(userId, GoPoliConstants.TRIP_STATUS_IN_PROGRESS).stream()
                    .findFirst();
            return asCreator.or(() -> firstMembershipWithStatus(userId, GoPoliConstants.TRIP_STATUS_IN_PROGRESS))
                    .<ResponseEntity<?>>map(t -> ResponseEntity.ok(TripResponse.from(t)))
                    .orElse(ApiResponses.status(404, "Sin viaje en curso"));
        });
    }

    @GetMapping("/trips/{tripId}")
    public ResponseEntity<?> trip(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer tripId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al consultar el viaje", () -> {
            if (!memberRepository.existsById(new TripMemberId(tripId, actor))) {
                return ApiResponses.status(403, "Solo los miembros pueden consultar el viaje");
            }
            return tripRepository.findById(tripId)
                    .<ResponseEntity<?>>map(t -> ResponseEntity.ok(TripResponse.from(t)))
                    .orElse(ResponseEntity.status(404).build());
        });
    }

    private ResponseEntity<?> requireOwner(String auth, Integer userId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        if (!actor.equals(userId)) {
            return ApiResponses.status(403, "No puedes consultar los viajes de otra persona");
        }
        return null;
    }

    private Optional<Trip> firstMembershipWithStatus(Integer userId, int statusId) {
        List<Integer> tripIds = memberRepository.findByUserId(userId).stream()
                .map(TripMember::getTripId)
                .toList();
        if (tripIds.isEmpty()) {
            return Optional.empty();
        }
        return tripRepository.findByIdInAndStatusId(tripIds, statusId).stream().findFirst();
    }

    private Map<Integer, String> userNames(List<Integer> userIds) {
        return userRepository.findAllById(userIds).stream()
                .filter(u -> u.getName() != null)
                .collect(Collectors.toMap(User::getId, User::getName, (a, b) -> a));
    }
}
