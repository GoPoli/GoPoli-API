package com.proyect.gopoli.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyect.gopoli.dto.UsuarioDto;
import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.model.ServicioUsuario;
import com.proyect.gopoli.model.Ubicacion;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.model.UsuarioEstado;
import com.proyect.gopoli.model.Vehiculo;
import com.proyect.gopoli.repository.CarreraRepository;
import com.proyect.gopoli.repository.ServicioRepository;
import com.proyect.gopoli.repository.ServicioUsuarioRepository;
import com.proyect.gopoli.repository.UbicacionRepository;
import com.proyect.gopoli.repository.UsuarioRepository;
import com.proyect.gopoli.repository.VehiculoRepository;
import com.proyect.gopoli.security.JwtService;
import com.proyect.gopoli.util.PerfilPolicy;
import com.proyect.gopoli.util.VehicleValidator;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private VehiculoRepository vehiculoRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ServicioUsuarioRepository servicioUsuarioRepo;

    @Autowired
    private ServicioRepository servicioRepo;

    @Autowired
    private UbicacionRepository ubicacionRepo;

    @Autowired
    private CarreraRepository carreraRepo;

    private Optional<Usuario> usuarioAutenticado(String authorization) {
        Integer id = jwtService.parseUserId(authorization);
        if (id == null) {
            return Optional.empty();
        }
        return repo.findById(id);
    }

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(401).body("Token inválido o ausente");
    }

    private UsuarioDto dtoConVehiculo(Usuario usuario) {
        Vehiculo v = vehiculoRepo.findByIdUsuario(usuario.getIdUsuario()).orElse(null);
        return UsuarioDto.from(usuario, v);
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerPerfil(@RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> usuario = usuarioAutenticado(auth);
        if (usuario.isEmpty()) {
            return noAutorizado();
        }
        if (usuario.get().getIdEstado() != null
                && usuario.get().getIdEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.status(403).body("Cuenta inhabilitada");
        }
        return ResponseEntity.ok(dtoConVehiculo(usuario.get()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> actualizarPerfil(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();
        if (usuario.getIdEstado() != null && usuario.getIdEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.status(403).body("Cuenta inhabilitada");
        }

        String nombre = body.get("nombre");
        String tel = body.get("tel");
        String correo = body.get("correo");
        String idCarreraRaw = body.get("idCarrera");

        if (nombre != null) {
            String error = PerfilPolicy.errorNombre(nombre);
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }
            usuario.setNombre(nombre.trim());
        }
        if (tel != null) {
            String error = PerfilPolicy.errorTelefono(tel.trim());
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }
            usuario.setTel(tel.trim());
        }
        if (correo != null) {
            String error = PerfilPolicy.errorCorreo(correo);
            if (error != null) {
                return ResponseEntity.badRequest().body(error);
            }
            String nuevoCorreo = PerfilPolicy.normalizarCorreo(correo);
            Optional<Usuario> otro = repo.findByCorreoIgnoreCase(nuevoCorreo);
            if (otro.isPresent() && !otro.get().getIdUsuario().equals(usuario.getIdUsuario())) {
                return ResponseEntity.status(409).body("El correo ya está en uso");
            }
            usuario.setCorreo(nuevoCorreo);
        }
        if (idCarreraRaw != null) {
            try {
                Integer idCarrera = Integer.valueOf(idCarreraRaw);
                if (!carreraRepo.existsById(idCarrera)) {
                    return ResponseEntity.badRequest().body("La carrera seleccionada no existe");
                }
                usuario.setIdCarrera(idCarrera);
            } catch (NumberFormatException ex) {
                return ResponseEntity.badRequest().body("La carrera seleccionada no es válida");
            }
        }

        return ResponseEntity.ok(dtoConVehiculo(repo.save(usuario)));
    }

    @PutMapping("/me/foto")
    public ResponseEntity<?> actualizarFoto(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        String foto = body.get("fotoBase64");
        if (foto == null || foto.isBlank()) {
            return ResponseEntity.status(400).body("fotoBase64 es obligatorio");
        }
        if (foto.length() > 2_000_000) {
            return ResponseEntity.status(400).body("La imagen es demasiado grande");
        }
        Usuario usuario = opt.get();
        usuario.setFotoPerfil(foto);
        return ResponseEntity.ok(dtoConVehiculo(repo.save(usuario)));
    }

    @PostMapping("/me/register-driver")
    public ResponseEntity<?> registerAsDriver(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();

        if (GoPoliConstants.esConductor(usuario.getIdTipoUsuario())) {
            return ResponseEntity.status(400).body("Ya eres conductor");
        }

        String marca = body.get("marca");
        String modelo = body.get("modelo");
        String color = body.get("color");
        String placa = body.get("placa");

        Map<String, String> errores = VehicleValidator.validate(marca, modelo, color, placa);
        if (!errores.isEmpty()) {
            return ResponseEntity.status(400).body(errores);
        }

        String placaNorm = VehicleValidator.normalizePlaca(placa);
        if (vehiculoRepo.existsByMatricula(placaNorm)) {
            return ResponseEntity.status(409).body(Map.of("placa", "Esta placa ya está registrada"));
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdUsuario(usuario.getIdUsuario());
        vehiculo.setMarca(marca.trim());
        vehiculo.setModelo(modelo.trim());
        vehiculo.setColor(color.trim());
        vehiculo.setMatricula(placaNorm);
        vehiculo.setCapacidad(4);
        vehiculoRepo.save(vehiculo);

        usuario.setIdTipoUsuario(GoPoliConstants.TIPO_USUARIO_CONDUCTOR);
        repo.save(usuario);

        return ResponseEntity.ok(dtoConVehiculo(usuario));
    }

    @PostMapping("/me/unregister-driver")
    public ResponseEntity<?> unregisterAsDriver(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();

        if (!GoPoliConstants.esConductor(usuario.getIdTipoUsuario())) {
            return ResponseEntity.status(400).body("No eres conductor");
        }

        List<Servicio> activosConductor = servicioRepo.findByIdCreadorAndIdEstadoServicio(
                usuario.getIdUsuario(), GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
        for (Servicio s : activosConductor) {
            if (GoPoliConstants.esViajeConductor(s.getIdTipoServicio())) {
                return ResponseEntity.status(400).body(
                        "Tienes un viaje conductor activo. Cancélalo o finalízalo antes de dejar de ser conductor.");
            }
        }

        List<Servicio> enCurso = servicioRepo.findByIdCreadorAndIdEstadoServicio(
                usuario.getIdUsuario(), GoPoliConstants.ESTADO_SERVICIO_EN_CURSO);
        for (Servicio s : enCurso) {
            if (GoPoliConstants.esViajeConductor(s.getIdTipoServicio())) {
                return ResponseEntity.status(400).body(
                        "Tienes un viaje conductor en curso. Finalízalo antes de dejar de ser conductor.");
            }
        }

        vehiculoRepo.findByIdUsuario(usuario.getIdUsuario()).ifPresent(vehiculoRepo::delete);
        usuario.setIdTipoUsuario(GoPoliConstants.TIPO_USUARIO_PASAJERO);
        repo.save(usuario);

        return ResponseEntity.ok(dtoConVehiculo(usuario));
    }

    @GetMapping("/me/historial-viajes")
    public ResponseEntity<?> historialViajes(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Integer idUsuario = opt.get().getIdUsuario();

        List<ServicioUsuario> membresias = servicioUsuarioRepo.findByIdUsuario(idUsuario);
        List<Map<String, Object>> historial = new ArrayList<>();

        for (ServicioUsuario su : membresias) {
            Optional<Servicio> servicioOpt = servicioRepo.findById(su.getIdServicio());
            if (servicioOpt.isEmpty()) continue;
            Servicio s = servicioOpt.get();
            if (s.getIdEstadoServicio() == null
                    || s.getIdEstadoServicio() != GoPoliConstants.ESTADO_SERVICIO_FINALIZADO) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idServicio", s.getIdServicio());
            item.put("fecha", s.getFecha());
            item.put("horaSalida", s.getHoraSalida());
            item.put("descripcion", s.getDescripcion());
            item.put("idTipoServicio", s.getIdTipoServicio());
            item.put("tripType", tripTypeKey(s.getIdTipoServicio()));
            item.put("tripTypeLabel", tripTypeLabel(s.getIdTipoServicio()));
            item.put("idLugarSalida", s.getIdLugarSalida());
            item.put("idLugarLlegada", s.getIdLugarLlegada());
            item.put("nombreSalida", nombreUbicacion(s.getIdLugarSalida()));
            item.put("nombreLlegada", nombreUbicacion(s.getIdLugarLlegada()));
            item.put("miRolParticipacion", su.getRolParticipacion());
            item.put("miRolParticipacionLabel", rolParticipacionLabel(su.getRolParticipacion()));

            List<Map<String, Object>> participantes = new ArrayList<>();
            for (ServicioUsuario m : servicioUsuarioRepo.findByIdServicio(s.getIdServicio())) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("idUsuario", m.getIdUsuario());
                p.put("rolGrupo", m.getRol());
                p.put("rolParticipacion", m.getRolParticipacion());
                p.put("rolParticipacionLabel", rolParticipacionLabel(m.getRolParticipacion()));
                repo.findById(m.getIdUsuario()).ifPresent(u -> p.put("nombreUsuario", u.getNombre()));
                participantes.add(p);
            }
            item.put("participantes", participantes);
            historial.add(item);
        }

        historial.sort(Comparator
                .comparing((Map<String, Object> m) -> (java.time.LocalDate) m.get("fecha"), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(m -> (java.time.LocalTime) m.get("horaSalida"), Comparator.nullsLast(Comparator.reverseOrder())));

        if (historial.size() > 10) {
            historial = historial.subList(0, 10);
        }

        return ResponseEntity.ok(historial);
    }

    private String nombreUbicacion(Integer id) {
        if (id == null) return null;
        return ubicacionRepo.findById(id).map(Ubicacion::getNombreUbicacion).orElse(null);
    }

    private static String tripTypeKey(Integer idTipoServicio) {
        if (GoPoliConstants.esViajeConductor(idTipoServicio)) {
            return "driver_group";
        }
        return "passenger_group";
    }

    private static String tripTypeLabel(Integer idTipoServicio) {
        if (GoPoliConstants.esViajeConductor(idTipoServicio)) {
            return "Grupo conductor";
        }
        return "Grupo de viaje";
    }

    private static String rolParticipacionLabel(String rol) {
        if (GoPoliConstants.ROL_PARTICIPACION_DRIVER.equals(rol)) {
            return "Conductor";
        }
        return "Pasajero";
    }

    @PostMapping("/me/inhabilitar")
    public ResponseEntity<?> inhabilitarCuenta(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();
        usuario.setIdEstado(UsuarioEstado.INHABILITADO);
        repo.save(usuario);
        return ResponseEntity.ok("Cuenta inhabilitada");
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> eliminarCuenta(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        vehiculoRepo.findByIdUsuario(opt.get().getIdUsuario()).ifPresent(vehiculoRepo::delete);
        repo.delete(opt.get());
        return ResponseEntity.ok("Cuenta eliminada permanentemente");
    }
}
