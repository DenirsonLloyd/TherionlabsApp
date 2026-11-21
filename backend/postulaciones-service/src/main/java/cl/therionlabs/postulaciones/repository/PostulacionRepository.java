package cl.therionlabs.postulaciones.repository;
import cl.therionlabs.postulaciones.model.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PostulacionRepository extends JpaRepository<Postulacion,Long> {
    List<Postulacion> findByUsuarioServidorIdOrderByIdDesc(Long usuarioServidorId);
}
