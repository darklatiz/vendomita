package tech.terabyte.labs.vendomita;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.terabyte.labs.vendomita.controller.VendomitaController;
import tech.terabyte.labs.vendomita.filter.CorrelationIdFilter;
import tech.terabyte.labs.vendomita.filter.CorrelationResponseAdvice;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.specification.SpecDto;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.factory.SpecParser;

import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración del controller con MockMvc.
 * - Mockeamos SpecParser para controlar el Specification resultante.
 * - Sembramos catálogo con /generate antes de /filter y /download.
 */
@WebMvcTest(controllers = VendomitaController.class)
@Import({CorrelationIdFilter.class, CorrelationResponseAdvice.class})
class VendomitaControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SpecParser specParser;

    private String correlationId;

    @BeforeEach
    void setUp() {
        correlationId = UUID.randomUUID().toString();
    }

    @Test
    void generate_shouldReturnSuccessAndCount() throws Exception {
        mvc.perform(get("/api/products/generate")
            .param("count", "50")
            .header("X-Correlation-Id", correlationId))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Correlation-Id", correlationId))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value("success"))
          .andExpect(jsonPath("$.message").value("Catalog generated"))
          .andExpect(jsonPath("$.data.count").value(50))
          .andExpect(jsonPath("$.meta.count").value(50));
    }

    @Test
    void filter_shouldReturnPagedDataAndMeta_andEchoCorrelationId() throws Exception {
        // 1) Genera catálogo de 30
        mvc.perform(get("/api/products/generate")
            .param("count", "30")
            .header("X-Correlation-Id", correlationId))
          .andExpect(status().isOk());

        // 2) Mock del parser: devolver specification que acepta todo (para tener matches = total)
        Mockito.when(specParser.fromDto(any(SpecDto.class)))
          .thenReturn(alwaysTrue());

        String body = """
                { "type": "InStockSpecification" }
                """;

        // 3) Filtra con paginación (page=1, size=10) → esperamos "returned"=10, total=30, totalPages=3
        mvc.perform(post("/api/products/filter")
            .param("page", "1")
            .param("size", "10")
            .header("X-Correlation-Id", correlationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Correlation-Id", correlationId))
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value("success"))
          .andExpect(jsonPath("$.message").value("Filter executed"))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data.length()").value(10))
          .andExpect(jsonPath("$.meta.total").value(30))
          .andExpect(jsonPath("$.meta.page").value(1))
          .andExpect(jsonPath("$.meta.size").value(10))
          .andExpect(jsonPath("$.meta.returned").value(10))
          .andExpect(jsonPath("$.meta.totalPages").value(3))
          .andExpect(jsonPath("$.meta.hasNext").value(true))
          .andExpect(jsonPath("$.meta.hasPrev").value(true));
    }

    @Test
    void download_shouldReturnTxtWithAttachment() throws Exception {
        // 1) Genera catálogo para habilitar descarga
        mvc.perform(get("/api/products/generate")
            .param("count", "5")
            .header("X-Correlation-Id", correlationId))
          .andExpect(status().isOk());

        // 2) Mock parser → acepta todo
        Mockito.when(specParser.fromDto(any(SpecDto.class)))
          .thenReturn(alwaysTrue());

        String body = """
                { "type": "InStockSpecification" }
                """;

        // 3) Descarga
        MvcResult res = mvc.perform(post("/api/products/download")
            .header("X-Correlation-Id", correlationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
          .andExpect(status().isOk())
          .andExpect(header().string("X-Correlation-Id", correlationId))
          .andExpect(header().string("Content-Disposition", Matchers.containsString("attachment; filename=filtered-products.txt")))
          .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
          .andReturn();

        String txt = res.getResponse().getContentAsString();
        // Debe contener encabezado "Filtered Products:"
        org.junit.jupiter.api.Assertions.assertTrue(txt.contains("Filtered Products:"));
    }

    // ---- helpers ----

    private Specification<Product> alwaysTrue() {
        return p -> true;
    }
}
