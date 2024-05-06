package br.unb.sds.gds2ephem.application.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Data;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Data
@TypeDef(name = "json", typeClass = JsonType.class)
@EntityListeners(AuditingEntityListener.class)
public class ConfiguracaoSistema {
    public static final Long DEFAULT_SYSTEM_CONFIG_ID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Long ephemAdminUserId;
    private Long ephemGdsUserId;
    private String ephemAdminApiKey;
}
