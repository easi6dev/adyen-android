/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 25/8/2022.
 */

package com.adyen.checkout.issuerlist

import com.adyen.checkout.components.model.paymentmethods.InputDetail
import com.adyen.checkout.components.model.paymentmethods.Issuer

internal fun List<Issuer>.mapToModel(): List<IssuerModel> =
    this.mapNotNull { (id, name, isDisabled) ->
        if (!isDisabled && id != null && name != null) {
            IssuerModel(id, name)
        } else {
            null
        }
    }

internal fun List<InputDetail>?.getLegacyIssuers(): List<IssuerModel> =
    this.orEmpty()
        .flatMap { it.items.orEmpty() }
        .mapNotNull { (id, name) ->
            if (id != null && name != null) {
                IssuerModel(id, name)
            } else {
                null
            }
        }
