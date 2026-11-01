package br.com.nevvesdev.financeflow.compartilhado;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validacao {

    private static final int ESCALA_MONETARIA = 2;
    private static final int PRECISAO_MONETARIA = 19;

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

    public static BigDecimal valorMonetario(BigDecimal valor) {
        obrigatorio(valor, "valor");
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("valor deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > ESCALA_MONETARIA) {
            throw new RegraDeNegocioException("valor deve ter no máximo duas casas decimais");
        }
        BigDecimal normalizado = valor.setScale(ESCALA_MONETARIA, RoundingMode.UNNECESSARY);
        if (normalizado.precision() > PRECISAO_MONETARIA) {
            throw new RegraDeNegocioException("valor excede o limite permitido");
        }
        return normalizado;
    }
}
