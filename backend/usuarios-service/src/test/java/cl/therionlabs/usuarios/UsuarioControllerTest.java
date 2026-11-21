package cl.therionlabs.usuarios;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class UsuarioControllerTest {
    @Autowired MockMvc mvc;
    @Test void crearListarActualizarEliminar() throws Exception {
        String dato="{\"nombre\":\"Alumno Ejemplo\",\"correo\":\"demo@test.cl\"}";
        String respuesta=mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(dato))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.correo").value("demo@test.cl"))
            .andReturn().getResponse().getContentAsString();
        long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(respuesta).get("id").asLong();
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(dato)).andExpect(status().isConflict());
        mvc.perform(get("/api/usuarios")).andExpect(status().isOk());
        mvc.perform(put("/api/usuarios/"+id).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Alumno Cambio\",\"correo\":\"demo@test.cl\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Alumno Cambio"));
        mvc.perform(delete("/api/usuarios/"+id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/"+id)).andExpect(status().isNotFound());
    }
    @Test void consultaCorreoYCubreErroresDelCrud() throws Exception {
        String primero = "{\"nombre\":\"Alumno Primero\",\"correo\":\"primero@test.cl\"}";
        String segundo = "{\"nombre\":\"Alumno Segundo\",\"correo\":\"segundo@test.cl\"}";
        String r1 = mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(primero))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String r2 = mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(segundo))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id1 = new com.fasterxml.jackson.databind.ObjectMapper().readTree(r1).get("id").asLong();
        long id2 = new com.fasterxml.jackson.databind.ObjectMapper().readTree(r2).get("id").asLong();
        mvc.perform(get("/api/usuarios/"+id1)).andExpect(status().isOk());
        mvc.perform(get("/api/usuarios/correo/primero@test.cl"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Alumno Primero"));
        mvc.perform(get("/api/usuarios/correo/noexiste@test.cl")).andExpect(status().isNotFound());
        mvc.perform(put("/api/usuarios/"+id1).contentType(MediaType.APPLICATION_JSON).content(segundo))
            .andExpect(status().isConflict());
        mvc.perform(delete("/api/usuarios/99999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/usuarios/"+id1)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/usuarios/"+id2)).andExpect(status().isNoContent());
    }
    @Test void invalido() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"A\",\"correo\":\"incorrecto\"}"))
            .andExpect(status().isBadRequest());
    }
}
