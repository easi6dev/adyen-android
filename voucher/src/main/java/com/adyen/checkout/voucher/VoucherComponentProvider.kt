/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.util.PaymentMethodTypes

private val PAYMENT_METHODS = listOf(PaymentMethodTypes.BACS)

class VoucherComponentProvider : ActionComponentProvider<VoucherComponent, VoucherConfiguration, VoucherDelegate> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: VoucherConfiguration,
        key: String?,
    ): VoucherComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null, key)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: VoucherConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): VoucherComponent {
        val voucherFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val voucherDelegate = getDelegate(configuration, savedStateHandle, application)
            VoucherComponent(
                configuration,
                voucherDelegate,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, voucherFactory)[key, VoucherComponent::class.java]
    }

    override fun getDelegate(
        configuration: VoucherConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): VoucherDelegate {
        return DefaultVoucherDelegate(ActionObserverRepository(), configuration)
    }

    override val supportedActionTypes: List<String>
        get() = listOf(VoucherAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return supportedActionTypes.contains(action.type) && PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun requiresView(action: Action): Boolean {
        return true
    }

    override fun providesDetails(action: Action): Boolean {
        return false
    }
}
