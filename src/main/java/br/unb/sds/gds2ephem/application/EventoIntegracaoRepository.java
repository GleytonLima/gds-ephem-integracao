package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.QEventoIntegracao;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "eventos", path = "eventos")
public interface EventoIntegracaoRepository extends PagingAndSortingRepository<EventoIntegracao, Long>,
        QuerydslPredicateExecutor<EventoIntegracao>,
        QuerydslBinderCustomizer<QEventoIntegracao> {
    @QueryHints({
            @QueryHint(
                    name = "javax.persistence.lock.timeout",
                    value = "-2")
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventoIntegracao> findTop10ByStatusOrderByCreatedAtAsc(String status);

    @Override
    default void customize(QuerydslBindings bindings, QEventoIntegracao user) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.containsIgnoreCase(value));
    }

    Page<EventoIntegracao> findByUserId(Pageable pageable, Long userId);
}
