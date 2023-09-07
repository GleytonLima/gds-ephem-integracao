package br.unb.sds.gds2ephem.application.model;

import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import java.io.Serializable;
import java.time.Instant;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QEventoIntegracao extends EntityPathBase<EventoIntegracao> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final QEventoIntegracao eventoIntegracao = new QEventoIntegracao("eventoIntegracao");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final StringPath status = createString("status");
    public final StringPath statusMessage = createString("statusMessage");
    public final NumberPath<Long> signalId = createNumber("signalId", Long.class);
    public final StringPath eventSourceId = createString("eventSourceId");
    public final StringPath eventSourceLocation = createString("eventSourceLocation");
    public final NumberPath<Long> userId = createNumber("userId", Long.class);
    public final StringPath userEmail = createString("userEmail");
    public final DateTimePath<Instant> createdAt = createDateTime("createdAt", Instant.class);
    public final DateTimePath<Instant> updatedAt = createDateTime("updatedAt", Instant.class);

    public QEventoIntegracao(String variable) {
        super(EventoIntegracao.class, forVariable(variable));
    }
}

