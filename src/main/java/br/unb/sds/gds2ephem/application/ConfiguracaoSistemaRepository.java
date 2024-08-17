package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@RepositoryRestResource(collectionResourceRel = "configuracoes", path = "configuracoes")
public interface ConfiguracaoSistemaRepository extends PagingAndSortingRepository<ConfiguracaoSistema, Long> {
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}
