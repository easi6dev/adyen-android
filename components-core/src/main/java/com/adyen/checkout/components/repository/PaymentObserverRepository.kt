/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/11/2022.
 */

package com.adyen.checkout.components.repository

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PaymentObserverRepository(
    private val observerContainer: ObserverContainer = ObserverContainer()
) {

    fun <T : PaymentComponentState<out PaymentMethodDetails>> addObservers(
        stateFlow: Flow<T>,
        exceptionFlow: Flow<CheckoutException>?,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<T>) -> Unit,
    ) {
        with(observerContainer) {
            removeObservers()

            stateFlow.observe(lifecycleOwner, coroutineScope) {
                callback(PaymentComponentEvent.StateChanged(it))
            }

            exceptionFlow?.observe(lifecycleOwner, coroutineScope) {
                callback(PaymentComponentEvent.Error(ComponentError(it)))
            }
        }
    }

    fun removeObservers() {
        observerContainer.removeObservers()
    }
}
