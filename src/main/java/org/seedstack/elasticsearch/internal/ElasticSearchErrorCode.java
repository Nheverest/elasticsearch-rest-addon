/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.elasticsearch.internal;

import org.seedstack.shed.exception.ErrorCode;

enum ElasticSearchErrorCode implements ErrorCode {
    FORBIDDEN_CLIENT_CLOSE,
    INVALID_HOST,
    INVALID_PORT,
    CANNOT_CREATE_CLIENT, UNKNOWN_HOST
}
