package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "eventos", path = "eventos")
public interface EventoIntegracaoRepository extends PagingAndSortingRepository<EventoIntegracao, Long> {
    @QueryHints({
            @QueryHint(
                    name = "javax.persistence.lock.timeout",
                    value = "-2")
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventoIntegracao> findTop10ByStatusOrderByCreatedAtAsc(String status);
}
