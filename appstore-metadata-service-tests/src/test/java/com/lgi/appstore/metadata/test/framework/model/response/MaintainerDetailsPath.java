package com.lgi.appstore.metadata.test.framework.model.response;

public class MaintainerDetailsPath extends ListPathBase {
    private static final String FIELD_MAINTAINERS = "maintainers";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_HOMEPAGE = "homepage";

    private MaintainerDetailsPath() {
    }

    public MaintainerDetailsPath maintainers() {
        fields.add(FIELD_MAINTAINERS);
        return this;
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

    public MaintainerDetailsPath meta() {
        fields.add(FIELD_META);
        return this;
    }

    public MaintainerDetailsPath resultSet() {
        fields.add(FIELD_RESULT_SET);
        return this;
    }

    public MaintainerDetailsPath count() {
        fields.add(FIELD_COUNT);
        return this;
    }

    public MaintainerDetailsPath total() {
        fields.add(FIELD_TOTAL);
        return this;
    }

    public MaintainerDetailsPath offset() {
        fields.add(FIELD_OFFSET);
        return this;
    }

    public MaintainerDetailsPath limit() {
        fields.add(FIELD_LIMIT);
        return this;
    }

    public MaintainerDetailsPath at(int idx) {
        atPosition(idx);
        return this;
    }

    public static MaintainerDetailsPath field() {
        return new MaintainerDetailsPath();
    }
}
