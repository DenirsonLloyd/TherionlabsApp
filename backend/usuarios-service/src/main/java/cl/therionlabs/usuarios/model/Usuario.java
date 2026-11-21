package cl.therionlabs.usuarios.model;
import jakarta.persistence.*;
@Entity
@Table(name="usuarios", uniqueConstraints=@UniqueConstraint(columnNames="correo"))
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String nombre;
    @Column(nullable=false, unique=true) public String correo;
    public Usuario() {}
    public Usuario(String nombre, String correo) { this.nombre=nombre; this.correo=correo; }
}
