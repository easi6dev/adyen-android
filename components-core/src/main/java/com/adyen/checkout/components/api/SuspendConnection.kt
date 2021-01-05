/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/1/2021.
 */

package com.adyen.checkout.components.api

import com.adyen.checkout.core.api.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

abstract class SuspendConnection<T>(url: String) : Connection<T>(url) {

    suspend fun suspendedCall(): T {
        return supervisorScope {
            withContext(Dispatchers.IO) {
                call()
            }
        }
    }
}
