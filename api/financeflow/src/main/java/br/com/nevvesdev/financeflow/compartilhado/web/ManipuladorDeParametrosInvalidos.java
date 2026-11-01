package br.com.nevvesdev.financeflow.compartilhado.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManipuladorDeParametrosInvalidos {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail aoConverterParametro(MethodArgumentTypeMismatchException excecao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + excecao.getName() + "'");
        problema.setTitle("Requisição inválida");
        return problema;
    }
}
