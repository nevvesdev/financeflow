package br.com.nevvesdev.financeflow.compartilhado;

public final class Validacao {

    private Validacao() {
    }

    public static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new RegraDeNegocioException("campo obrigatório: " + campo);
        }
        return valor;
    }

    public static String textoObrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("campo obrigatório: " + campo);
        }
        String normalizado = valor.strip();
        if (normalizado.length() > tamanhoMaximo) {
            throw new RegraDeNegocioException(
                "tamanho máximo de " + tamanhoMaximo + " caracteres excedido: " + campo);
        }
        return normalizado;
    }
}
