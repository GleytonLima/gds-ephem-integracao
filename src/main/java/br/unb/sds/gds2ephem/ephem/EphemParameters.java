package br.unb.sds.gds2ephem.ephem;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import static java.util.Arrays.asList;

@Builder
@Data
public class EphemParameters {
    public static final String MODEL_DEFAULT_SORT = "id desc";
    public static final String MAIL_MESSAGE_MODEL_NAME = "mail.message";
    public static final String EOC_SIGNAL_TAGS_MODEL_NAME = "eoc.signal.tags";
    public static final String SIGNAL_MODEL_NAME = "eoc.signal";
    public static final List<String> SIGNAL_DEFAULT_PARAMETERS = asList("id",
            "description",
            "confidentiality",
            "tag_ids",
            "general_hazard_id",
            "specific_hazard_id",
            "country_id",
            "source_country_id",
            "state_id",
            "district_ids",
            "signal_type",
            "report_date",
            "incident_date",
            "name",
            "outcome",
            "outcome_justification_id",
            "people_affected",
            "message_ids",
            "signal_stage_state_id",
            "active",
            "aetiology_id",
            "animals_affected",
            "create_date",
            "date_closed",
            "date_outcome_decided",
            "description",
            "notification_date",
            "display_name",
            "is_event_closed",
            "under_verification_date",
            "verification",
            "verification_source_id",
            "verified_date",
            "was_closed",
            "was_closed_date",
            "was_discarded",
            "was_discarded_date",
            "was_event",
            "was_event_date",
            "was_monitored",
            "was_monitored_date",
            "was_under_verification",
            "was_under_verification_date",
            "triage_date",
            "__last_update"
            );

    private String nomeModelo;
    private String contextLang;
    private List<String> fields;
    private Object filtros;
    private Long id;
    private Integer offset;
    private Integer size;
    private String sort;
    Object[] idList;
}
