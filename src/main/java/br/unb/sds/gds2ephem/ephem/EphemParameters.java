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
    public static final List<String> SIGNAL_DEFAULT_PARAMETERS = asList("id", "description", "confidentiality", "tag_ids", "general_hazard_id", "specific_hazard_id", "country_id", "state_id", "district_ids", "signal_type", "report_date", "incident_date", "name", "message_ids", "signal_stage_state_id");

    private String nomeModelo;
    private List<String> fields;
    private Long id;
    private Integer offset;
    private Integer size;
    private String sort;
}
