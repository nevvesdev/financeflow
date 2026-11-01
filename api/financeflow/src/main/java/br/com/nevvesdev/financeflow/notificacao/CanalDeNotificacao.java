package br.com.nevvesdev.financeflow.notificacao;

public interface CanalDeNotificacao {

    void enviar(AvisoParaEnvio aviso);
}
