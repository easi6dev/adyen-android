/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultOnlineBankingCZDelegate(
    private val paymentMethod: PaymentMethod,
    private val paymentMethodFactory: () -> OnlineBankingCZPaymentMethod
) : OnlineBankingDelegate<OnlineBankingCZPaymentMethod> {

    private val _outputDataFlow = MutableStateFlow<OnlineBankingOutputData?>(null)
    override val outputDataFlow: Flow<OnlineBankingOutputData?> get() = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<OnlineBankingCZPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<OnlineBankingCZPaymentMethod>?> = _componentStateFlow

    override fun getIssuers(): List<OnlineBankingModel> {
        return paymentMethod.issuers?.let {
            getIssuers(it)
        } ?: getLegacyIssuers(paymentMethod.details)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onInputDataChanged(inputData: OnlineBankingInputData) {
        val outputData = OnlineBankingOutputData(inputData.selectedIssuer)

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    override fun createComponentState(outputData: OnlineBankingOutputData) {
        val issuerListPaymentMethod = paymentMethodFactory()
        issuerListPaymentMethod.type = getPaymentMethodType()
        issuerListPaymentMethod.issuer = outputData.selectedIssuer?.id ?: ""

        val paymentComponentData = PaymentComponentData(paymentMethod = issuerListPaymentMethod)

        val state = PaymentComponentState(paymentComponentData, outputData.isValid, true)
        _componentStateFlow.tryEmit(state)
    }

    override fun getTermsAndConditionsUrl(): String = TERMS_CONDITIONS_URL

    companion object {
        private const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_cs.pdf"
    }
}