package br.com.nevvesdev.financeflow.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CanalDeLog implements CanalDeNotificacao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanalDeLog.class);

    @Override
    public void enviar(AvisoParaEnvio aviso) {
        LOGGER.info("Aviso {} ({}) entregue ao usuário {}", aviso.notificacaoId(), aviso.tipo(), aviso.usuarioId());
    }
}
