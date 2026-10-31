package br.com.nevvesdev.financeflow;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class BaseApiTest {

    @Autowired
    private WebApplicationContext contexto;

    protected MockMvc mockMvc;

    @BeforeEach
    protected void configurarMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
            .apply(springSecurity())
            .build();
    }
}
