/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2022 Liberty Global Technology Services BV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lgi.appstore.metadata.api.stb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgi.appstore.metadata.api.converter.StringToApplicationTypeConverter;
import com.lgi.appstore.metadata.api.converter.StringToCategoryConverter;
import com.lgi.appstore.metadata.api.converter.StringToPlatformConverter;
import com.lgi.appstore.metadata.api.error.GlobalExceptionHandler;
import com.lgi.appstore.metadata.api.stb.input.validator.PlatformAndVersionOptionalForWebValidator;
import com.lgi.appstore.metadata.model.AppIdWithType;
import com.lgi.appstore.metadata.model.ApplicationType;
import com.lgi.appstore.metadata.model.Category;
import com.lgi.appstore.metadata.model.Hardware;
import com.lgi.appstore.metadata.model.Localization;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.Meta;
import com.lgi.appstore.metadata.model.Platform;
import com.lgi.appstore.metadata.model.Requirements;
import com.lgi.appstore.metadata.model.ResultSetMeta;
import com.lgi.appstore.metadata.model.StbApplicationDetails;
import com.lgi.appstore.metadata.model.StbApplicationHeader;
import com.lgi.appstore.metadata.model.StbApplicationsList;
import com.lgi.appstore.metadata.model.StbSingleApplicationHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class StbAppsControllerTest {
    private static final List<ApplicationType> WEB_APPLICATIONS = List.of(ApplicationType.HTML5);

    private final AppsService appsService = Mockito.mock(AppsService.class);

    @Spy
    private final PlatformAndVersionOptionalForWebValidator platformAndVersionOptionalForWebValidator = new PlatformAndVersionOptionalForWebValidator(WEB_APPLICATIONS);

    private MockMvc mvc;

    @InjectMocks
    private StbAppsController stbAppsController;

    private JacksonTester<StbApplicationsList> jsonApplicationsList;
    private JacksonTester<StbApplicationDetails> jsonApplicationDetails;

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        final FormattingConversionService conversionService = new FormattingConversionService();
        conversionService.addConverter(new StringToCategoryConverter());
        conversionService.addConverter(new StringToPlatformConverter());
        conversionService.addConverter(new StringToApplicationTypeConverter());

        mvc = MockMvcBuilders.standaloneSetup(stbAppsController)
                .addFilter(((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setConversionService(conversionService)
                .build();

        doReturn(Optional.of(new AppIdWithType("native-app", ApplicationType.DAC_NATIVE.getValue())))
                .when(appsService).getApplicationType(Mockito.anyString());
    }

    private static final StbApplicationHeader FLUTTER_APPLICATION_HEADER = new StbApplicationHeader()
            .id("com.libertyglobal.app.flutter")
            .name("flutter")
            .type("application/vnd.rdk-app.dac.native")
            .size(10000000)
            .version("0.0.1")
            .icon("default_app_collection.png")
            .description("Container contains both Flutter application and Flutter engine running on wayland-egl, developed by Liberty Global while evaluating Google Flutter UI toolkit.")
            .category(Category.APPLICATION);

    private static final StbApplicationHeader WAYLAND_EGL_TEST_APPLICATION_HEADER = new StbApplicationHeader()
            .id("com.libertyglobal.app.waylandegltest")
            .name("wayland-egl-test")
            .type("application/vnd.rdk-app.dac.native")
            .size(10000000)
            .version("3.2.1")
            .icon("wayland.png")
            .description("Source code example of simple Wayland EGL application intended as tutorial for developers. Contains the few but necessary setup code for any direct to wayland-egl client application such as how to connect to wayland server, create/use EGL surface and draw on screen via opengles api. Application shows simple rectangle on screen. Applications based on this example should run on the various wayland compositors supporting the wayland-egl protocol out there.")
            .category(Category.APPLICATION);

    private static final StbApplicationHeader YOU_I_APPLICATION_HEADER = new StbApplicationHeader()
            .id("com.libertyglobal.app.youi")
            .name("you.i")
            .type("application/vnd.rdk-app.dac.native")
            .size(10000000)
            .version("1.2.3")
            .icon("default_app_collection.png")
            .description("Showcase application from the company youi.tv. The container package contains both the react native application and the You.i TV react native Gfx engine beneath.")
            .category(Category.APPLICATION)
            .localization(
                    List.of(
                            new Localization()
                                    .name("Jij.ik")
                                    .description("Showcase-applicatie van het bedrijf youi.tv. Het containerpakket bevat zowel de native-toepassing reageren als de You.i TV reageren native Gfx-engine eronder.")
                                    .languageCode("nld"),

                            new Localization()
                                    .name("Ty.ja")
                                    .description("Prezentacja aplikacji firmy youi.tv. Kontener zawiera zarówno natywną aplikację react, jak i znajdujący się poniżej natywny silnik Gfx.")
                                    .languageCode("pol")
                    )
            );

    private static final StbSingleApplicationHeader YOU_I_SINGLE_APPLICATION_HEADER = new StbSingleApplicationHeader()
            .id("com.libertyglobal.app.youi")
            .name("you.i")
            .type("application/vnd.rdk-app.dac.native")
            .size(10000000)
            .version("1.2.3")
            .icon("default_app_collection.png")
            .description("Showcase application from the company youi.tv. The container package contains both the react native application and the You.i TV react native Gfx engine beneath.")
            .category(Category.APPLICATION)
            .localization(
                    List.of(
                            new Localization()
                                    .name("Jij.ik")
                                    .description("Showcase-applicatie van het bedrijf youi.tv. Het containerpakket bevat zowel de native-toepassing reageren als de You.i TV reageren native Gfx-engine eronder.")
                                    .languageCode("nld"),

                            new Localization()
                                    .name("Ty.ja")
                                    .description("Prezentacja aplikacji firmy youi.tv. Kontener zawiera zarówno natywną aplikację react, jak i znajdujący się poniżej natywny silnik Gfx.")
                                    .languageCode("pol")
                    )
            );

    private static final StbApplicationDetails YOU_I_APPLICATION_DETAILS = new StbApplicationDetails()
            .header(YOU_I_SINGLE_APPLICATION_HEADER)
            .requirements(new Requirements()
                    .platform(new Platform()
                            .architecture("arm")
                    )
                    .hardware(new Hardware()
                            .ram("256")
                            .dmips("2000")
                            .persistent("20M")
                            .cache("50M")
                    )
            )
            .maintainer(new Maintainer()
                    .name("Liberty Global")
                    .address("Liberty Global B.V., Boeing Avenue 53, 1119 PE Schiphol Rijk, The Netherlands")
                    .homepage("www.libertyglobal.com")
                    .email("developer@libertyglobal.com")
            );

    private static final StbApplicationsList EMPTY_APPLICATIONS_LIST = new StbApplicationsList()
            .applications(List.of())
            .meta(new Meta()
                    .resultSet(new ResultSetMeta()
                            .limit(10)
                            .offset(0)
                            .count(0)
                            .total(0)
                    )
            );

    private static final StbApplicationsList NON_EMPTY_APPLICATIONS_LIST = new StbApplicationsList()
            .applications(List.of(FLUTTER_APPLICATION_HEADER, WAYLAND_EGL_TEST_APPLICATION_HEADER, YOU_I_APPLICATION_HEADER)
            )
            .meta(new Meta()
                    .resultSet(new ResultSetMeta()
                            .total(3)
                            .count(3)
                            .offset(0)
                            .limit(10)
                    )
            );

    private static final StbApplicationsList FLUTTER_ONLY_APPLICATIONS_LIST = new StbApplicationsList()
            .applications(List.of(FLUTTER_APPLICATION_HEADER)
            )
            .meta(new Meta()
                    .resultSet(new ResultSetMeta()
                            .total(1)
                            .count(1)
                            .offset(0)
                            .limit(10)
                    )
            );

    @Test
    void canListAppsWhenThereAreSomePresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, null, null, null, null))
                .willReturn(NON_EMPTY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(NON_EMPTY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsWhenThereAreNoPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, null, null, null, null))
                .willReturn(EMPTY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(EMPTY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByNameWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications("flutter", null, null, null, null, null, null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);


        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?name=flutter").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByDescriptionWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, "Container contains both Flutter", null, null, null, null, null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?description=Container contains both Flutter").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByVersionWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, "0.0.1", null, null, null, null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?version=0.0.1").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByTypeWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, "application/vnd.rdk-app.dac.native", null, null, null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?type=application/vnd.rdk-app.dac.native").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByPlatformWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, new StringToPlatformConverter().convert("arm:v7:linux"), null, null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?platform=arm:v7:linux").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void cannotListAppsByIncorrectPlatformWhenThereAreSomeAppsPresent() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?platform=::").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void canListAppsByCategoryWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, Category.fromValue("application"), null, null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?category=application").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void cannotListAppsByIncorrectCategoryWhenThereAreSomeAppsPresent() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?category=applicationFake").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void canListAppsByMaintainerNameWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, null, "Liberty Global", null, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?maintainerName=Liberty Global").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByOffsetWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, null, null, 1, null))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?offset=1").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canListAppsByLimitWhenThereAreSomeAppsPresent() throws Exception {
        // given
        given(appsService.listApplications(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(EMPTY_APPLICATIONS_LIST);
        given(appsService.listApplications(null, null, null, null, null, null, null, null, 1))
                .willReturn(FLUTTER_ONLY_APPLICATIONS_LIST);

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps?limit=1").accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationsList.write(FLUTTER_ONLY_APPLICATIONS_LIST).getJson());
    }

    @Test
    void canGetDetailsByJustApplicationIdOfAnExistingApplication() throws Exception {
        // given
        final String platformName = UUID.randomUUID().toString();
        final String firmwareVer = UUID.randomUUID().toString();
        given(appsService.getApplicationDetails(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());
        given(appsService.getApplicationDetails("com.libertyglobal.app.youi", platformName, firmwareVer))
                .willReturn(Optional.of(YOU_I_APPLICATION_DETAILS));

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi?platformName={platformName}&firmwareVer={firmwareVer}", platformName, firmwareVer))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationDetails.write(YOU_I_APPLICATION_DETAILS).getJson());
    }

    @Test
    void canGetDetailsByApplicationIdAndVersionOfAnExistingApplication() throws Exception {
        // given
        final String platformName = UUID.randomUUID().toString();
        final String firmwareVer = UUID.randomUUID().toString();
        given(appsService.getApplicationDetails(anyString(), anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());
        given(appsService.getApplicationDetails("com.libertyglobal.app.youi", "1.2.3", platformName, firmwareVer))
                .willReturn(Optional.of(YOU_I_APPLICATION_DETAILS));

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:1.2.3?platformName={platformName}&firmwareVer={firmwareVer}",
                        platformName, firmwareVer))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationDetails.write(YOU_I_APPLICATION_DETAILS).getJson());
    }

    @Test
    void canGetDetailsByApplicationIdAndLatestVersionOfAnExistingApplication() throws Exception {
        // given
        final String platformName = UUID.randomUUID().toString();
        final String firmwareVer = UUID.randomUUID().toString();
        given(appsService.getApplicationDetails(anyString(), anyString(), anyString()))
                .willReturn(Optional.empty());
        given(appsService.getApplicationDetails("com.libertyglobal.app.youi", platformName, firmwareVer))
                .willReturn(Optional.of(YOU_I_APPLICATION_DETAILS));

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:latest?platformName={platformName}&firmwareVer={firmwareVer}",
                        platformName, firmwareVer))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonApplicationDetails.write(YOU_I_APPLICATION_DETAILS).getJson());
    }

    @Test
    void cannotGetDetailsOfANonExistingApplication() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/nonExistingApp?platformName=doesntmatter&firmwareVer=doesntmatter"))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void cannotGetApplicationDetailsWithoutPlatformName() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/nonExistingApp?firmwareVer=doesntmatter"))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"platformName is mandatory for native apps\"}");
    }

    @Test
    void cannotGetApplicationDetailsWithoutFirmwareVer() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc
                .perform(get("/apps/nonExistingApp?platformName=doesntmatter"))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"firmwareVer is mandatory for native apps\"}");
    }

    @Test
    void cannotListNativeApplicationWhenFirmwareVerIsAbsent() throws Exception {
        // GIVEN
        final String platformName = UUID.randomUUID().toString();

        // WHEN
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:latest?platformName={platformName}", platformName))
                .andReturn()
                .getResponse();

        // THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"firmwareVer is mandatory for native apps\"}");
    }

    @Test
    void cannotListNativeApplicationWhenPlatformNameIsAbsent() throws Exception {
        // GIVEN
        final String firmwareVer = UUID.randomUUID().toString();

        // WHEN
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:latest?firmwareVer={firmwareVer}", firmwareVer))
                .andReturn()
                .getResponse();

        // THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"platformName is mandatory for native apps\"}");
    }

    @Test
    void canListWebApplication() throws Exception {
        // GIVEN
        doReturn(Optional.of(new AppIdWithType("web-app", ApplicationType.HTML5.toString())))
                .when(appsService).getApplicationType(Mockito.anyString());
        doReturn(Optional.of(YOU_I_APPLICATION_DETAILS))
                .when(appsService).getApplicationDetails(eq("com.libertyglobal.app.youi"), Mockito.any(), Mockito.any());

        // WHEN
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:latest"))
                .andReturn()
                .getResponse();

        // THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void cannotListApplicationWhenTypeIsUnsupported() throws Exception {
        // GIVEN
        doReturn(Optional.of(new AppIdWithType("web-app", null))).when(appsService).getApplicationType(Mockito.anyString());
        doReturn(Optional.of(YOU_I_APPLICATION_DETAILS))
                .when(appsService).getApplicationDetails(eq("com.libertyglobal.app.youi"), Mockito.any(), Mockito.any());

        // WHEN
        MockHttpServletResponse response = mvc
                .perform(get("/apps/com.libertyglobal.app.youi:latest"))
                .andReturn()
                .getResponse();

        // THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"Unsupported application type\"}");
    }

}