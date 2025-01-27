/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/9/2022.
 */

package com.adyen.checkout.components.base

interface ComponentDelegate {
    val configuration: Configuration

    fun onCleared()
}
