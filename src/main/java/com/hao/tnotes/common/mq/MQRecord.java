package com.hao.tnotes.common.mq;

import lombok.Data;

@Data
public class MQRecord {

    public static final String EXCHANGE_NAME = "tnotes.direct.exchange";
    public static final String AUTH_CODE_QUEUE = "tnotes.auth.code.queue";
    public static final String INIT_USER_NOTE_QUEUE = "tnotes.init.user.note.queue";
    public static final String INIT_USER_CLOUD_QUEUE = "tnotes.init.user.cloud.queue";
    public static final String AUTH_CODE_ROUTING_KEY = "tnotes.auth.code.routing.key";
    public static final String INIT_USER_NOTE_ROUTING_KEY = "tnotes.init.user.note.routing.key";
    public static final String INIT_USER_CLOUD_ROUTING_KEY = "tnotes.init.user.cloud.routing.key";


}
