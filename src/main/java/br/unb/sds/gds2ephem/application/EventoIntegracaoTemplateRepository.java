package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@RepositoryRestResource(collectionResourceRel = "templates", path = "templates")
public interface EventoIntegracaoTemplateRepository extends PagingAndSortingRepository<EventoIntegracaoTemplate, Long> {
}
