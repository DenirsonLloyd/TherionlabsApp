package cl.therionlabs.postulaciones;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
@TestPropertySource(properties="therionlabs.fotos.directorio=./target/test-fotos")
class PostulacionControllerTest {
    @Autowired MockMvc mvc;
    @Test void flujoCrud() throws Exception {
        String datos="{\"usuarioServidorId\":4,\"nombre\":\"Alumno Demo\",\"correo\":\"demo@test.cl\",\"telefono\":\"912345678\",\"rut\":\"12345678-5\",\"carrera\":\"Informática\",\"modalidad\":\"Online\"}";
        String respuesta=mvc.perform(post("/api/postulaciones").contentType(MediaType.APPLICATION_JSON).content(datos))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("Recibida"))
            .andReturn().getResponse().getContentAsString();
        long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(respuesta).get("id").asLong();
        // Subir y recuperar una foto real en formato PNG.
        BufferedImage imagen = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen,"png",salida);
        MockMultipartFile foto = new MockMultipartFile("foto", "imagen.png", "image/png", salida.toByteArray());
        mvc.perform(multipart("/api/postulaciones/"+id+"/foto").file(foto))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tieneFoto").value(true));
        mvc.perform(get("/api/postulaciones/"+id+"/foto"))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(multipart("/api/postulaciones/"+id+"/foto")
            .file(new MockMultipartFile("foto","archivo.txt","text/plain","hola".getBytes())))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/postulaciones")).andExpect(status().isOk());
        mvc.perform(get("/api/postulaciones/usuario/4")).andExpect(status().isOk());
        mvc.perform(put("/api/postulaciones/"+id).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isOk());
        mvc.perform(put("/api/postulaciones/"+id+"/estado").contentType(MediaType.APPLICATION_JSON)
            .content("{\"estado\":\"Aceptada\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("Aceptada"));
        mvc.perform(put("/api/postulaciones/"+id+"/estado").contentType(MediaType.APPLICATION_JSON)
            .content("{\"estado\":\"No válido\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/postulaciones/"+id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/postulaciones/"+id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/postulaciones/"+id+"/foto")).andExpect(status().isNotFound());
    }
    @Test void rechazaDatosIncompletos() throws Exception {
        mvc.perform(post("/api/postulaciones").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }
}
