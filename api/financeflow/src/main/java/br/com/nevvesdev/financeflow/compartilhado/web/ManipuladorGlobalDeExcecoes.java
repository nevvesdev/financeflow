package br.com.nevvesdev.financeflow.compartilhado.web;

import br.com.nevvesdev.financeflow.compartilhado.ConflitoException;
import br.com.nevvesdev.financeflow.compartilhado.NaoAutenticadoException;
import br.com.nevvesdev.financeflow.compartilhado.RecursoNaoEncontradoException;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ManipuladorGlobalDeExcecoes {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail aoValidarArgumentos(MethodArgumentNotValidException excecao) {
        Map<String, String> erros = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors()
            .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ProblemDetail problema = problema(
            HttpStatus.BAD_REQUEST, "Requisição inválida", "Um ou mais campos são inválidos");
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail aoLerCorpo() {
        return problema(
            HttpStatus.BAD_REQUEST, "Requisição inválida", "Corpo da requisição ausente ou malformado");
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail aoViolarRegraDeNegocio(RegraDeNegocioException excecao) {
        return problema(HttpStatus.BAD_REQUEST, "Regra de negócio violada", excecao.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail aoNaoEncontrarRecurso(RecursoNaoEncontradoException excecao) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", excecao.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail aoDetectarConflito(ConflitoException excecao) {
        return problema(HttpStatus.CONFLICT, "Conflito", excecao.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ProblemDetail> aoNaoAutenticar(NaoAutenticadoException excecao) {
        ProblemDetail problema = problema(HttpStatus.UNAUTHORIZED, "Não autenticado", excecao.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
            .body(problema);
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
