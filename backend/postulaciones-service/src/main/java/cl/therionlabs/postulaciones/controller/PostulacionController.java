package cl.therionlabs.postulaciones.controller;
import cl.therionlabs.postulaciones.model.Postulacion;
import cl.therionlabs.postulaciones.repository.PostulacionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/postulaciones")
public class PostulacionController {
    private final PostulacionRepository repositorio;
    private static final int MAX_FOTO = 5 * 1024 * 1024;
    @Value("${therionlabs.fotos.directorio:./datos/fotos}")
    private String directorioFotos;
    public PostulacionController(PostulacionRepository repositorio) { this.repositorio=repositorio; }
    public record DatosPostulacion(
        @NotNull @Positive Long usuarioServidorId,
        @NotBlank String nombre, @NotBlank @Email String correo,
        @NotBlank String telefono, @NotBlank String rut,
        @NotBlank String carrera, String modalidad) {}
    public record NuevoEstado(@NotBlank String estado) {}
    private static final List<String> ESTADOS=List.of("Recibida","En revisión","Aceptada","Rechazada");

    @GetMapping public List<Postulacion> listar() { return repositorio.findAll(); }
    @GetMapping("/usuario/{usuarioId}") public List<Postulacion> deUsuario(@PathVariable Long usuarioId) {
        return repositorio.findByUsuarioServidorIdOrderByIdDesc(usuarioId);
    }
    @GetMapping("/{id}") public Postulacion buscar(@PathVariable Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Postulación no encontrada"));
    }
    private void copiarDatos(Postulacion p, DatosPostulacion d) {
        p.usuarioServidorId=d.usuarioServidorId(); p.nombre=d.nombre().trim();
        p.correo=d.correo().trim(); p.telefono=d.telefono().trim();
        p.rut=d.rut().trim(); p.carrera=d.carrera().trim();
        p.modalidad=d.modalidad()==null ? "Presencial" : d.modalidad();
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Postulacion crear(@Valid @RequestBody DatosPostulacion datos) {
        Postulacion nuevo=new Postulacion(); copiarDatos(nuevo,datos);
        return repositorio.save(nuevo);
    }
    @PutMapping("/{id}") public Postulacion actualizar(@PathVariable Long id,@Valid @RequestBody DatosPostulacion datos) {
        Postulacion p=buscar(id); copiarDatos(p,datos); return repositorio.save(p);
    }
    @PutMapping("/{id}/estado") public Postulacion cambiarEstado(@PathVariable Long id,@Valid @RequestBody NuevoEstado datos) {
        if (!ESTADOS.contains(datos.estado())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Estado inválido");
        Postulacion p=buscar(id); p.estado=datos.estado(); return repositorio.save(p);
    }
    // La foto se envía por separado, después de registrar la postulación.
    @PostMapping(value="/{id}/foto", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Postulacion subirFoto(@PathVariable Long id, @RequestPart("foto") MultipartFile foto) {
        Postulacion p=buscar(id);
        if (foto.isEmpty() || foto.getSize()>MAX_FOTO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La foto debe pesar como máximo 5 MB");
        String tipo=foto.getContentType();
        if (!"image/jpeg".equals(tipo) && !"image/png".equals(tipo))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Solo se permiten imágenes JPG o PNG");
        try {
            byte[] contenido=foto.getBytes();
            if (!imagenValida(contenido, tipo))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El archivo no es una imagen válida");

            Path carpeta=Paths.get(directorioFotos).toAbsolutePath().normalize();
            Files.createDirectories(carpeta);
            String extension="image/png".equals(tipo) ? ".png" : ".jpg";
            String nombre="foto_"+id+"_"+UUID.randomUUID()+extension;
            Path temporal=Files.createTempFile(carpeta,"subida_", ".tmp");
            try {
                Files.write(temporal,contenido);
                Files.move(temporal,carpeta.resolve(nombre),StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temporal); }

            String anterior=p.fotoNombre;
            p.fotoNombre=nombre;
            try { repositorio.save(p); }
            catch (Exception e) {
                Files.deleteIfExists(carpeta.resolve(nombre));
                throw e;
            }
            if (anterior != null && !anterior.isBlank()) {
                try { Files.deleteIfExists(carpeta.resolve(anterior)); }
                catch (IOException e) { /* El archivo viejo se puede limpiar después. */ }
            }
            return p;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"No se pudo guardar la fotografía");
        }
    }

    private boolean imagenValida(byte[] datos, String tipo) throws IOException {
        boolean jpg=datos.length>3 && (datos[0]&0xFF)==0xFF && (datos[1]&0xFF)==0xD8 && (datos[2]&0xFF)==0xFF;
        boolean png=datos.length>8 && (datos[0]&0xFF)==137 && datos[1]==80 && datos[2]==78 && datos[3]==71;
        if ("image/jpeg".equals(tipo) && !jpg) return false;
        if ("image/png".equals(tipo) && !png) return false;
        try {
            BufferedImage imagen=ImageIO.read(new ByteArrayInputStream(datos));
            return imagen != null && imagen.getWidth()>0 && imagen.getHeight()>0 &&
                    imagen.getWidth()<=5000 && imagen.getHeight()<=5000;
        } catch (IOException e) { return false; }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> obtenerFoto(@PathVariable Long id) {
        Postulacion p=buscar(id);
        if (!p.isTieneFoto())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"La postulación no tiene foto");
        Path carpeta=Paths.get(directorioFotos).toAbsolutePath().normalize();
        Path archivo=carpeta.resolve(p.fotoNombre).normalize();
        if (!archivo.startsWith(carpeta) || !Files.isRegularFile(archivo))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Foto no encontrada");
        MediaType tipo=archivo.getFileName().toString().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).body(new FileSystemResource(archivo));
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!repositorio.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Postulación no encontrada");
        Postulacion p=buscar(id);
        repositorio.deleteById(id);
        if (p.isTieneFoto()) {
            try {
                Path carpeta=Paths.get(directorioFotos).toAbsolutePath().normalize();
                Files.deleteIfExists(carpeta.resolve(p.fotoNombre));
            } catch (IOException e) {
                // La postulación ya fue eliminada; el archivo se puede borrar manualmente.
            }
        }
    }
}
