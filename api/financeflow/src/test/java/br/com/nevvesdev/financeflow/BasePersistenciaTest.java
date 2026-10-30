package br.com.nevvesdev.financeflow;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class BasePersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    protected void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }
}
