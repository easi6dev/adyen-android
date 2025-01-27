/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.qrcode

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.lifecycle.get
import com.adyen.checkout.components.base.lifecycle.viewModelFactory
import com.adyen.checkout.components.handler.DefaultRedirectHandler
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.components.status.DefaultStatusRepository
import com.adyen.checkout.components.status.api.StatusService
import com.adyen.checkout.components.util.PaymentMethodTypes

private val VIEWABLE_PAYMENT_METHODS = listOf(PaymentMethodTypes.PIX)

class QRCodeComponentProvider : ActionComponentProvider<QRCodeComponent, QRCodeConfiguration, QRCodeDelegate> {

    override fun <T> get(
        owner: T,
        application: Application,
        configuration: QRCodeConfiguration,
        key: String?,
    ): QRCodeComponent where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
        return get(owner, owner, application, configuration, null, key)
    }

    override fun get(
        savedStateRegistryOwner: SavedStateRegistryOwner,
        viewModelStoreOwner: ViewModelStoreOwner,
        application: Application,
        configuration: QRCodeConfiguration,
        defaultArgs: Bundle?,
        key: String?,
    ): QRCodeComponent {
        val qrCodeFactory = viewModelFactory(savedStateRegistryOwner, defaultArgs) { savedStateHandle ->
            val qrCodeDelegate = getDelegate(configuration, savedStateHandle, application)
            QRCodeComponent(
                configuration = configuration,
                delegate = qrCodeDelegate,
            )
        }
        return ViewModelProvider(viewModelStoreOwner, qrCodeFactory)[key, QRCodeComponent::class.java]
    }

    override fun getDelegate(
        configuration: QRCodeConfiguration,
        savedStateHandle: SavedStateHandle,
        application: Application,
    ): QRCodeDelegate {
        val statusService = StatusService(configuration.environment.baseUrl)
        val statusRepository = DefaultStatusRepository(statusService, configuration.clientKey)
        val countDownTimer = QRCodeCountDownTimer()
        val redirectHandler = DefaultRedirectHandler()
        val paymentDataRepository = PaymentDataRepository(savedStateHandle)

        return DefaultQRCodeDelegate(
            observerRepository = ActionObserverRepository(),
            configuration = configuration,
            statusRepository = statusRepository,
            statusCountDownTimer = countDownTimer,
            redirectHandler = redirectHandler,
            paymentDataRepository = paymentDataRepository,
        )
    }

    override val supportedActionTypes: List<String>
        get() = listOf(QrCodeAction.ACTION_TYPE)

    override fun canHandleAction(action: Action): Boolean {
        return when {
            !supportedActionTypes.contains(action.type) -> false
            // viewable action, can be handled
            requiresView(action) -> true
            // QR code actions that contain a url are handled as a redirect action
            !(action as? QrCodeAction)?.url.isNullOrEmpty() -> true
            else -> false
        }
    }

    // TODO remove this method when we create a generic Action handling Component
    override fun requiresView(action: Action): Boolean {
        return VIEWABLE_PAYMENT_METHODS.contains(action.paymentMethodType)
    }

    override fun providesDetails(action: Action): Boolean {
        return true
    }
}
