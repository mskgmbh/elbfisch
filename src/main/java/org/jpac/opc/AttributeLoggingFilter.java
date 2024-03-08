/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : AttributeLoggingFilter.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 * 
 * This module was implemented on basis of the pi-server example published
 * by Kevin Herron under the following license:
 * 
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.* 
 */

package org.jpac.opc;


import java.util.function.Predicate;

import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.GetAttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.SetAttributeContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeLoggingFilter implements AttributeFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Predicate<AttributeId> attributePredicate;

    public AttributeLoggingFilter() {
        this(attributeId -> true);
    }

    public AttributeLoggingFilter(Predicate<AttributeId> attributePredicate) {
        this.attributePredicate = attributePredicate;
    }

    @Override
    public Object getAttribute(GetAttributeContext ctx, AttributeId attributeId) {
        Object value = ctx.getAttribute(attributeId);

        // only log external reads
        if (attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
            logger.info(
                "get nodeId={} attributeId={} value={}",
                ctx.getNode().getNodeId(), attributeId, value
            );
        }

        return value;
    }

    @Override
    public void setAttribute(SetAttributeContext ctx, AttributeId attributeId, Object value) {
        // only log external writes
        if (attributePredicate.test(attributeId) && ctx.getSession().isPresent()) {
            logger.info(
                "set nodeId={} attributeId={} value={}",
                ctx.getNode().getNodeId(), attributeId, value
            );
        }

        ctx.setAttribute(attributeId, value);
    }

}
