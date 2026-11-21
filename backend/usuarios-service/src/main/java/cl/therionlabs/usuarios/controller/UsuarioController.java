package cl.therionlabs.usuarios.controller;
import cl.therionlabs.usuarios.model.Usuario;
import cl.therionlabs.usuarios.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository repositorio;
    public UsuarioController(UsuarioRepository repositorio) { this.repositorio = repositorio; }

    public record DatosUsuario(@NotBlank @Size(min=3) String nombre, @NotBlank @Email String correo) {}

    @GetMapping public List<Usuario> listar() { return repositorio.findAll(); }
    @GetMapping("/{id}") public Usuario buscar(@PathVariable Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado"));
    }
    @GetMapping("/correo/{correo}") public Usuario porCorreo(@PathVariable String correo) {
        return repositorio.findByCorreoIgnoreCase(correo.trim()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado"));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody DatosUsuario datos) {
        String correo = datos.correo().trim().toLowerCase();
        if (repositorio.findByCorreoIgnoreCase(correo).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Correo ya registrado");
        return repositorio.save(new Usuario(datos.nombre().trim(),correo));
    }
    @PutMapping("/{id}") public Usuario actualizar(@PathVariable Long id, @Valid @RequestBody DatosUsuario datos) {
        Usuario usuario = buscar(id);
        String correo = datos.correo().trim().toLowerCase();
        repositorio.findByCorreoIgnoreCase(correo).ifPresent(otro -> {
            if (!otro.id.equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Correo ya registrado");
        });
        usuario.nombre=datos.nombre().trim(); usuario.correo=correo;
        return repositorio.save(usuario);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!repositorio.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado");
        repositorio.deleteById(id);
    }
}
