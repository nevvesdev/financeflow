package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContasParaAvisoRepository extends Repository<Lancamento, UUID> {

    @Query("""
            select new br.com.nevvesdev.financeflow.lancamento.ContaParaAviso(
                l.id, l.usuario.id, l.descricao, l.valor, l.dataVencimento)
            from Lancamento l
            where l.tipo = :tipo
              and l.paga = false
              and l.dataVencimento between :inicio and :fim
            order by l.dataVencimento asc, l.id asc
            """)
    List<ContaParaAviso> buscarAbertasComVencimentoEntre(
        @Param("tipo") TipoLancamento tipo,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);
}
