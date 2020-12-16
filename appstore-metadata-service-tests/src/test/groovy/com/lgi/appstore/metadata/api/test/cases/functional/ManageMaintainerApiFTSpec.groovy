package com.lgi.appstore.metadata.api.test.cases.functional

import com.lgi.appstore.metadata.api.test.AsmsFeatureSpecBase
import com.lgi.appstore.metadata.model.Maintainer
import io.restassured.path.json.JsonPath
import io.restassured.response.ExtractableResponse
import io.restassured.response.Response
import spock.lang.Unroll

import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerQueryParams.LIMIT
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerQueryParams.NAME
import static com.lgi.appstore.metadata.api.test.framework.model.request.ApiMaintainerQueryParams.OFFSET
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.mapping
import static com.lgi.appstore.metadata.api.test.framework.model.request.QueryParams.queryParams
import static com.lgi.appstore.metadata.api.test.framework.model.response.MaintainerDetailsPath.field
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.devAddress
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.devCode
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.devEmail
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.devHomepage
import static com.lgi.appstore.metadata.api.test.framework.utils.DataUtils.devName
import static org.apache.http.HttpStatus.SC_OK
import static org.assertj.core.api.Assertions.assertThat

class ManageMaintainerApiFTSpec extends AsmsFeatureSpecBase {
    def static final DEFAULT_LIMIT = 10

    @Unroll
    def "query for maintainers list returns amount corresponding to given limit=#limit offset=#offset"() {
        given: "assuming there is a default developer already in DB maintainer creates 2 additional developers"
        def dev1name = devName()
        def dev1email = devEmail()
        def dev1homepage = devHomepage()
        def dev1address = devAddress()
        def dev1Details = new Maintainer()
                .code(devCode())
                .name(dev1name)
                .email(dev1email)
                .homepage(dev1homepage)
                .address(dev1address)
        maintainerSteps.createNewMaintainer_expectSuccess(dev1Details)

        def dev2name = devName()
        def dev2email = devEmail()
        def dev2homepage = devHomepage()
        def dev2address = devAddress()
        def dev2Details = new Maintainer()
                .code(devCode())
                .name(dev2name)
                .email(dev2email)
                .homepage(dev2homepage)
                .address(dev2address)
        maintainerSteps.createNewMaintainer_expectSuccess(dev2Details)

        when: "maintainer asks for list of developers specifying limit and offset"
        Map<String, Object> queryParams = queryParams(
                mapping(LIMIT, limit),
                mapping(OFFSET, offset)
        )
        ExtractableResponse<Response> response = maintainerSteps.getMaintainersList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets positive response"
        receivedStatus == SC_OK
        assertThat(field().maintainers().from(jsonBody)).asList().hasSizeLessThanOrEqualTo(returnedLimit)

        and: "the amount of items is as desired"
        field().meta().resultSet().count().from(jsonBody) == count
        field().meta().resultSet().total().from(jsonBody) == total
        field().meta().resultSet().limit().from(jsonBody) == returnedLimit
        field().meta().resultSet().offset().from(jsonBody) == offset

        where:
        limit | offset || count | total | returnedLimit
        4     | 0      || 3     | 3     | limit
        null  | 0      || 3     | 3     | DEFAULT_LIMIT
        1     | 0      || 1     | 3     | limit
        1     | 1      || 1     | 3     | limit
        1     | 3      || 0     | 3     | limit
    }

    @Unroll
    def "query for maintainers list filtering by name #scenario"() {
        given: "assuming there is a default developer already in DB maintainer creates 2 additional devs with similar names"
        def dev1name = "Dev2"
        def dev1email = devEmail()
        def dev1homepage = devHomepage()
        def dev1address = devAddress()
        def dev1Details = new Maintainer()
                .code(devCode())
                .name(dev1name)
                .email(dev1email)
                .homepage(dev1homepage)
                .address(dev1address)
        maintainerSteps.createNewMaintainer_expectSuccess(dev1Details)

        def dev2name = "DEV3"
        def dev2email = devEmail()
        def dev2homepage = devHomepage()
        def dev2address = devAddress()
        def dev2Details = new Maintainer()
                .code(devCode())
                .name(dev2name)
                .email(dev2email)
                .homepage(dev2homepage)
                .address(dev2address)
        maintainerSteps.createNewMaintainer_expectSuccess(dev2Details)

        when: "maintainer asks for list of developers specifying name pattern"
        Map<String, Object> queryParams = queryParams(
                mapping(NAME, nameQueryParam)
        )
        ExtractableResponse<Response> response = maintainerSteps.getMaintainersList(queryParams).extract()
        JsonPath jsonBody = response.jsonPath()
        def receivedStatus = response.statusCode()

        then: "he gets positive response"
        receivedStatus == SC_OK
        assertThat(field().maintainers().from(jsonBody)).asList().hasSizeLessThanOrEqualTo(count)

        and: "the amount of items is as desired"
        if (count > 0) {
            Comparator<String> caseInsensitiveComparator = new Comparator<String>() {
                @Override
                int compare(String s1, String s2) {
                    return s1.toString().toLowerCase().compareTo(s2.toString().toLowerCase());
                }
            };

            field().meta().resultSet().count().from(jsonBody) == count
            assertThat(field().maintainers().name().at(0).from(jsonBody)).asString().usingComparator(caseInsensitiveComparator).startsWith(nameQueryParam.toLowerCase())
        }

        where:
        scenario                                       | nameQueryParam        || count
        "can find single item that starts with"        | "Lib"                 || 1
        "can find multiple items that start with"      | "D"                   || 2
        "is case insensitive"                          | "dE"                  || 2
        "empty list returned when nothing starts with" | "Liberty Global Inc." || 0
        "empty value results in all returned"          | ""                    || 3
        "does not use regex nor common * wildcard"     | "*"                   || 0
    }
}