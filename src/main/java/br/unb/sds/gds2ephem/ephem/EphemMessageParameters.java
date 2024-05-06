package br.unb.sds.gds2ephem.ephem;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EphemMessageParameters {
    public static final String MODEL_DEFAULT_SORT = "id desc";
    public static final String MAIL_MESSAGE_MODEL_NAME = "mail.message";
    public static final String EOC_SIGNAL_TAGS_MODEL_NAME = "eoc.signal.tags";
    public static final String SIGNAL_MODEL_NAME = "eoc.signal";
    public static final Object[] FIELDS = new Object[]{"subject", "body", "date", "message_type", "partner_ids", "author_id", "res_id"};
    private Object[] fromIds;
    private Object[] signalIds;
    private Object[] partnerIds;
    private Long limit;
    private Long offset;
}
