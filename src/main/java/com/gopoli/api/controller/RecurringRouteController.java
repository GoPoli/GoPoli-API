package com.gopoli.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.gopoli.api.dto.RecurringRouteDtos.RouteRequest;
import com.gopoli.api.dto.RecurringRouteDtos.RouteResponse;
import com.gopoli.api.model.GoPoliConstants;
import com.gopoli.api.model.Location;
import com.gopoli.api.model.RecurringRoute;
import com.gopoli.api.policy.TripPolicy;
import com.gopoli.api.repository.LocationRepository;
import com.gopoli.api.repository.RecurringRouteRepository;
import com.gopoli.api.security.JwtService;

@RestController
public class RecurringRouteController {

    private static final String AUTH = "Authorization";

    private final RecurringRouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final JwtService jwtService;

    public RecurringRouteController(
            RecurringRouteRepository routeRepository,
            LocationRepository locationRepository,
            JwtService jwtService) {
        this.routeRepository = routeRepository;
        this.locationRepository = locationRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/users/{userId}/recurring-routes")
    public ResponseEntity<?> listByUser(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer userId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        if (!actor.equals(userId)) {
            return ApiResponses.status(403, "No puedes consultar la agenda de otra persona");
        }
        return ApiResponses.guarded("Error al listar rutas", () ->
                ResponseEntity.ok(toResponses(routeRepository.findByUserIdOrderByIdAsc(userId))));
    }

    @PostMapping("/recurring-routes")
    public ResponseEntity<?> create(
            @RequestHeader(value = AUTH, required = false) String auth,
            @RequestBody RouteRequest request) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al guardar la ruta", () -> {
            String error = validate(request);
            if (error != null) {
                return ApiResponses.status(400, error);
            }
            RecurringRoute route = new RecurringRoute();
            route.setUserId(actor);
            apply(route, request, GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP);
            return ResponseEntity.ok(toResponses(List.of(routeRepository.save(route))).get(0));
        });
    }

    @PutMapping("/recurring-routes/{routeId}")
    public ResponseEntity<?> update(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer routeId,
            @RequestBody RouteRequest request) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al actualizar la ruta", () -> {
            Optional<RecurringRoute> found = routeRepository.findById(routeId);
            if (found.isEmpty()) {
                return ApiResponses.status(404, "Ruta no encontrada");
            }
            RecurringRoute route = found.get();
            if (!actor.equals(route.getUserId())) {
                return ApiResponses.status(403, "No puedes editar esta ruta");
            }
            String error = validate(request);
            if (error != null) {
                return ApiResponses.status(400, error);
            }
            Integer fallbackType = route.getTripTypeId() != null
                    ? route.getTripTypeId()
                    : GoPoliConstants.TRIP_TYPE_PASSENGER_GROUP;
            apply(route, request, fallbackType);
            return ResponseEntity.ok(toResponses(List.of(routeRepository.save(route))).get(0));
        });
    }

    @DeleteMapping("/recurring-routes/{routeId}")
    public ResponseEntity<?> delete(
            @RequestHeader(value = AUTH, required = false) String auth,
            @PathVariable Integer routeId) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ApiResponses.unauthorized();
        }
        return ApiResponses.guarded("Error al eliminar", () -> {
            Optional<RecurringRoute> found = routeRepository.findById(routeId);
            if (found.isEmpty()) {
                return ApiResponses.status(404, "Ruta no encontrada");
            }
            if (!actor.equals(found.get().getUserId())) {
                return ApiResponses.status(403, "No puedes eliminar esta ruta");
            }
            routeRepository.delete(found.get());
            return ResponseEntity.ok("Ruta eliminada");
        });
    }

    private void apply(RecurringRoute route, RouteRequest request, Integer fallbackTripType) {
        route.setDepartureLocationId(request.departureLocationId());
        route.setArrivalLocationId(request.arrivalLocationId());
        route.setWeekdays(normalizeWeekdays(request.weekdays()));
        route.setDepartureTime(request.departureTime());
        route.setCapacity(request.capacity());
        route.setTripTypeId(request.tripTypeId() != null ? request.tripTypeId() : fallbackTripType);
        route.setDescription(TripPolicy.normalizeDescription(request.description()));
    }

    private String validate(RouteRequest request) {
        if (request.departureLocationId() == null) {
            return "El lugar de salida es obligatorio";
        }
        if (request.arrivalLocationId() == null) {
            return "El lugar de llegada es obligatorio";
        }
        if (request.departureLocationId().equals(request.arrivalLocationId())) {
            return "Salida y llegada deben ser distintas";
        }
        if (!locationRepository.existsById(request.departureLocationId())) {
            return "Lugar de salida no encontrado";
        }
        if (!locationRepository.existsById(request.arrivalLocationId())) {
            return "Lugar de llegada no encontrado";
        }
        if (request.departureTime() == null) {
            return "La hora de salida es obligatoria";
        }
        if (request.capacity() == null
                || request.capacity() < TripPolicy.MIN_CAPACITY
                || request.capacity() > TripPolicy.MAX_CAPACITY) {
            return "La capacidad debe ser entre 2 y 4 personas";
        }
        String weekdays = normalizeWeekdays(request.weekdays());
        if (weekdays == null || weekdays.isBlank()) {
            return "Selecciona al menos un día de la semana";
        }
        if (request.tripTypeId() != null && !TripPolicy.isValidTripType(request.tripTypeId())) {
            return "Tipo de viaje no válido";
        }
        return TripPolicy.descriptionError(request.description());
    }

    // Normaliza "1, 3, 5" a "1,3,5": solo días ISO 1–7, sin repetidos y en orden.
    static String normalizeWeekdays(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d{1,2}"))
                .map(Integer::parseInt)
                .filter(n -> n >= 1 && n <= 7)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<RouteResponse> toResponses(List<RecurringRoute> routes) {
        List<Integer> locationIds = routes.stream()
                .flatMap(r -> Stream.of(r.getDepartureLocationId(), r.getArrivalLocationId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> names = locationRepository.findAllById(locationIds).stream()
                .filter(l -> l.getName() != null)
                .collect(Collectors.toMap(Location::getId, Location::getName, (a, b) -> a));
        return routes.stream()
                .map(r -> new RouteResponse(
                        r.getId(),
                        r.getUserId(),
                        r.getDepartureLocationId(),
                        r.getArrivalLocationId(),
                        r.getWeekdays(),
                        r.getDepartureTime(),
                        r.getCapacity(),
                        r.getTripTypeId(),
                        r.getDescription(),
                        names.get(r.getDepartureLocationId()),
                        names.get(r.getArrivalLocationId())))
                .toList();
    }
}
