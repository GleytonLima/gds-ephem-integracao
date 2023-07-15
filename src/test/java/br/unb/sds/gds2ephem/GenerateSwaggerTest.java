package br.unb.sds.gds2ephem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@AutoConfigureMockMvc
class GenerateSwaggerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve gerar arquivo swagger para posterior uso na pasta target")
    void generateSwagger() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/api-docs"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo((result) -> {
                    System.out.printf("oi" + result);
                    Files.write(Path.of("target/swagger.json"), result.getResponse().getContentAsString().getBytes());
                });
    }
}
