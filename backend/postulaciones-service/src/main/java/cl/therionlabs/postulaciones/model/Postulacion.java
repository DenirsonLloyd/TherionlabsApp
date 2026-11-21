package cl.therionlabs.postulaciones.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Table(name="postulaciones")
public class Postulacion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long usuarioServidorId;
    @Column(nullable=false) public String nombre;
    @Column(nullable=false) public String correo;
    @Column(nullable=false) public String telefono;
    @Column(nullable=false) public String rut;
    @Column(nullable=false) public String carrera;
    public String modalidad;
    // Guardamos el nombre del archivo, no la imagen dentro de H2.
    @JsonIgnore public String fotoNombre;
    public boolean isTieneFoto() { return fotoNombre != null && !fotoNombre.isBlank(); }
    @Column(nullable=false) public String estado="Recibida";
    @Column(nullable=false) public Long fechaCreacion;
    @PrePersist public void antesDeGuardar() {
        if (fechaCreacion==null) fechaCreacion=System.currentTimeMillis();
        if (estado==null || estado.isBlank()) estado="Recibida";
    }
}
