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

package com.lgi.appstore.metadata.api.maintainer;

import static com.lgi.appstore.metadata.jooq.model.Tables.APPLICATION;
import static com.lgi.appstore.metadata.jooq.model.Tables.MAINTAINER;
import static org.jooq.impl.DSL.count;

import com.lgi.appstore.metadata.api.error.ApplicationAlreadyExistsException;
import com.lgi.appstore.metadata.api.error.MaintainerAlreadyExistsException;
import com.lgi.appstore.metadata.api.error.MaintainerNotFoundException;
import com.lgi.appstore.metadata.model.Maintainer;
import com.lgi.appstore.metadata.model.MaintainerForUpdate;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service("PersistentMaintainersService")
public class PersistentMaintainersService implements MaintainersService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentMaintainersService.class);

    private final DSLContext dslContext;

    @Autowired
    public PersistentMaintainersService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Maintainer getMaintainer(final String maintainerCode) {
        return dslContext
                .select()
                .from(MAINTAINER)
                .where(MAINTAINER.CODE.eq(maintainerCode))
                .fetchOptional()
                .map(record -> new Maintainer()
                        .name(record.get(MAINTAINER.NAME))
                        .address(record.get(MAINTAINER.ADDRESS))
                        .homepage(record.get(MAINTAINER.HOMEPAGE))
                        .email(record.get(MAINTAINER.EMAIL))
                        .code(record.get(MAINTAINER.CODE)))
                .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));
    }

    @Override
    public void createMaintainer(final Maintainer maintainer) {
        dslContext.transaction(configuration -> {
            final DSLContext localDslContext = DSL.using(configuration);

            localDslContext
                    .select(MAINTAINER.ID)
                    .from(MAINTAINER)
                    .where(MAINTAINER.CODE.eq(maintainer.getCode()))
                    .fetchOptional()
                    .ifPresent((record) -> {
                        throw new MaintainerAlreadyExistsException(maintainer.getCode());
                    });

            localDslContext.insertInto(MAINTAINER,
                    MAINTAINER.CODE,
                    MAINTAINER.ADDRESS,
                    MAINTAINER.HOMEPAGE,
                    MAINTAINER.EMAIL,
                    MAINTAINER.NAME
            )
                    .values(
                            maintainer.getCode(),
                            maintainer.getAddress(),
                            maintainer.getHomepage(),
                            maintainer.getEmail(),
                            maintainer.getName()
                    )
                    .execute();
        });
    }

    @Override
    public boolean updateMaintainer(final String maintainerCode, final MaintainerForUpdate maintainerForUpdate) {
        return dslContext.transactionResult(configuration -> {
            final DSLContext localDslContext = DSL.using(configuration);

            final Integer maintainerId = localDslContext.select(MAINTAINER.ID)
                    .from(MAINTAINER)
                    .where(MAINTAINER.CODE.eq(maintainerCode))
                    .fetchOptional()
                    .map(integerRecord -> integerRecord.get(MAINTAINER.ID))
                    .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

            final int affectedRows = localDslContext.update(MAINTAINER)
                    .set(MAINTAINER.ADDRESS, maintainerForUpdate.getAddress())
                    .set(MAINTAINER.EMAIL, maintainerForUpdate.getEmail())
                    .set(MAINTAINER.HOMEPAGE, maintainerForUpdate.getHomepage())
                    .set(MAINTAINER.NAME, maintainerForUpdate.getName())
                    .where(MAINTAINER.ID.eq(maintainerId))
                    .execute();

            return affectedRows > 0;
        });
    }

    @Override
    public boolean deleteMaintainer(final String maintainerCode) {
        return dslContext.transactionResult(configuration -> {
            final DSLContext localDslContext = DSL.using(configuration);

            final Pair<Integer, Integer> maintainerIdWithApplicationCount = localDslContext.select(MAINTAINER.ID, count(APPLICATION.ID))
                    .from(MAINTAINER)
                    .leftJoin(APPLICATION)
                    .on(APPLICATION.MAINTAINER_ID.eq(MAINTAINER.ID))
                    .where(MAINTAINER.CODE.eq(maintainerCode))
                    .groupBy(MAINTAINER.ID)
                    .fetchOptional()
                    .map(record -> Pair.of(record.get(0, Integer.class), record.get(1, Integer.class)))
                    .orElseThrow(() -> new MaintainerNotFoundException(maintainerCode));

            if (maintainerIdWithApplicationCount.getRight() != null && maintainerIdWithApplicationCount.getRight() > 0) {
                throw new ApplicationAlreadyExistsException(String.format("Can't remove maintainer '%s' with application(s) ", maintainerCode));
            }

            final int affectedRows = localDslContext.deleteFrom(MAINTAINER)
                    .where(MAINTAINER.ID.eq(maintainerIdWithApplicationCount.getLeft()))
                    .execute();

            return affectedRows > 0;
        });
    }
}
