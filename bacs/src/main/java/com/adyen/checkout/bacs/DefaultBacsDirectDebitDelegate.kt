/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/7/2022.
 */

package com.adyen.checkout.bacs

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal class DefaultBacsDirectDebitDelegate(
    private val observerRepository: PaymentObserverRepository,
    override val configuration: BacsDirectDebitConfiguration,
    val paymentMethod: PaymentMethod,
) : BacsDirectDebitDelegate {

    private val inputData: BacsDirectDebitInputData = BacsDirectDebitInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BacsDirectDebitOutputData> = _outputDataFlow

    override val outputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<BacsDirectDebitComponentState> = _componentStateFlow

    @VisibleForTesting
    @Suppress("VariableNaming", "PropertyName")
    internal val _viewFlow = MutableStateFlow(BacsComponentViewType.INPUT)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<BacsDirectDebitComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun setMode(mode: BacsDirectDebitMode): Boolean {
        val currentMode = inputData.mode
        return when {
            mode == currentMode -> {
                Logger.e(TAG, "Current mode is already $mode")
                false
            }
            mode == BacsDirectDebitMode.CONFIRMATION && !outputData.isValid -> {
                Logger.e(TAG, "Cannot set confirmation view when input is not valid")
                false
            }
            else -> {
                Logger.d(TAG, "Setting mode to $mode")
                updateInputData { this.mode = mode }
                true
            }
        }
    }

    override fun updateInputData(update: BacsDirectDebitInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        updateViewType(inputData.mode)

        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun updateViewType(mode: BacsDirectDebitMode) {
        val viewType = when (mode) {
            BacsDirectDebitMode.INPUT -> BacsComponentViewType.INPUT
            BacsDirectDebitMode.CONFIRMATION -> BacsComponentViewType.CONFIRMATION
        }
        if (_viewFlow.value != viewType) {
            Logger.d(TAG, "Updating view flow to $viewType")
            _viewFlow.tryEmit(viewType)
        }
    }

    private fun createOutputData() = BacsDirectDebitOutputData(
        holderNameState = BacsDirectDebitValidationUtils.validateHolderName(inputData.holderName),
        bankAccountNumberState = BacsDirectDebitValidationUtils
            .validateBankAccountNumber(inputData.bankAccountNumber),
        sortCodeState = BacsDirectDebitValidationUtils.validateSortCode(inputData.sortCode),
        shopperEmailState = BacsDirectDebitValidationUtils.validateShopperEmail(inputData.shopperEmail),
        isAmountConsentChecked = inputData.isAmountConsentChecked,
        isAccountConsentChecked = inputData.isAccountConsentChecked,
        mode = inputData.mode,
    )

    @VisibleForTesting
    internal fun updateComponentState(outputData: BacsDirectDebitOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: BacsDirectDebitOutputData = this.outputData
    ): BacsDirectDebitComponentState {
        val bacsDirectDebitPaymentMethod = BacsDirectDebitPaymentMethod(
            type = BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
            holderName = outputData.holderNameState.value,
            bankAccountNumber = outputData.bankAccountNumberState.value,
            bankLocationId = outputData.sortCodeState.value,
        )

        val paymentComponentData = PaymentComponentData(
            shopperEmail = outputData.shopperEmailState.value,
            paymentMethod = bacsDirectDebitPaymentMethod,
        )

        return BacsDirectDebitComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
            mode = outputData.mode
        )
    }

    override fun onCleared() {
        removeObserver()
    }

    override fun getViewProvider(): ViewProvider = BacsViewProvider

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
