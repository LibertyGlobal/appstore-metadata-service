/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 Liberty Global B.V.
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

create sequence maintainer_id_seq increment 1 start 1 minvalue 1 maxvalue 2147483647 cache 1;

create table maintainer (
    id integer primary key default nextval('maintainer_id_seq'::regclass),
    code text,
    "name" text,
    address text,
    homepage text,
    email text
);

create sequence developer_id_seq increment 1 start 1 minvalue 1 maxvalue 2147483647 cache 1;

create table developer (
    id integer primary key default nextval('developer_id_seq'::regclass),
    maintainer_id integer references maintainer (id),
    "name" text
);

create sequence application_id_seq increment 1 start 1 minvalue 1 maxvalue 2147483647 cache 1;

create table application (
    id integer primary key default nextval('application_id_seq'::regclass),
    maintainer_id integer references maintainer(id),
    id_rdomain text,
    "version" text,
    latest jsonb,
    visible boolean default false,
    "name" text,
    description text,
    url text,
    icon text,
    "type" text,
    category text,
    platform jsonb,
    hardware jsonb,
    features jsonb,
    dependencies jsonb,
    localizations jsonb,
    unique (id_rdomain, version)
);

create unique index application_id_rdomain_version_idx ON application (id_rdomain, version);
