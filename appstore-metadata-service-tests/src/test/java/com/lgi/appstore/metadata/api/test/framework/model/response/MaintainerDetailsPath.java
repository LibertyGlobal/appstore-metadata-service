package com.lgi.appstore.metadata.api.test.framework.model.response;

public class MaintainerDetailsPath extends PathBase {
    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_HOMEPAGE = "homepage";

    private MaintainerDetailsPath() {
    }

    public MaintainerDetailsPath code() {
        fields.add(FIELD_CODE);
        return this;
    }

    public MaintainerDetailsPath name() {
        fields.add(FIELD_NAME);
        return this;
    }

    public MaintainerDetailsPath address() {
        fields.add(FIELD_ADDRESS);
        return this;
    }

    public MaintainerDetailsPath email() {
        fields.add(FIELD_EMAIL);
        return this;
    }

    public MaintainerDetailsPath homepage() {
        fields.add(FIELD_HOMEPAGE);
        return this;
    }

    public static MaintainerDetailsPath field() {
        return new MaintainerDetailsPath();
    }
}
