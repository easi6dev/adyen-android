/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin

import android.content.Context
import android.os.Bundle
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.bacs.BacsDirectDebitConfiguration
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dropin.DropInConfiguration.Builder
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.onlinebankingcz.OnlineBankingCZConfiguration
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration
import com.adyen.checkout.onlinebankingsk.OnlineBankingSKConfiguration
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.sepa.SepaConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale
import kotlin.collections.set

/**
 * This is the base configuration for the Drop-In solution. You need to use the [Builder] to instantiate this class.
 * There you will find specific methods to add configurations for each specific PaymentComponent, to be able to customize their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
@Parcelize
@Suppress("LongParameterList")
class DropInConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    private val availablePaymentConfigs: HashMap<String, Configuration>,
    internal val availableActionConfigs: HashMap<Class<*>, Configuration>,
    val amount: Amount,
    val showPreselectedStoredPaymentMethod: Boolean,
    val skipListWhenSinglePaymentMethod: Boolean,
    val isRemovingStoredPaymentMethodsEnabled: Boolean,
    val additionalDataForDropInService: Bundle?,
) : Configuration {

    constructor(
        builder: Builder
    ) : this(
        builder.shopperLocale,
        builder.environment,
        builder.clientKey,
        builder.availablePaymentConfigs,
        builder.availableActionConfigs,
        builder.amount,
        builder.showPreselectedStoredPaymentMethod,
        builder.skipListWhenSinglePaymentMethod,
        builder.isRemovingStoredPaymentMethodsEnabled,
        builder.additionalDataForDropInService,
    )

    internal fun <T : Configuration> getConfigurationForPaymentMethod(paymentMethod: String): T? {
        if (availablePaymentConfigs.containsKey(paymentMethod)) {
            @Suppress("UNCHECKED_CAST")
            return availablePaymentConfigs[paymentMethod] as T
        }
        return null
    }

    @Suppress("unused")
    internal inline fun <reified T : Configuration> getConfigurationForAction(): T? {
        val actionClass = T::class.java
        if (availableActionConfigs.containsKey(actionClass)) {
            @Suppress("UNCHECKED_CAST")
            return availableActionConfigs[actionClass] as T
        }
        return null
    }

    /**
     * Builder for creating a [DropInConfiguration] where you can set specific Configurations for a Payment Method
     */
    @Suppress("unused", "TooManyFunctions")
    class Builder : BaseConfigurationBuilder<DropInConfiguration> {

        internal val availablePaymentConfigs = HashMap<String, Configuration>()
        internal val availableActionConfigs = HashMap<Class<*>, Configuration>()

        var amount: Amount = Amount.EMPTY
            private set
        var showPreselectedStoredPaymentMethod: Boolean = true
            private set
        var skipListWhenSinglePaymentMethod: Boolean = false
            private set
        var isRemovingStoredPaymentMethodsEnabled: Boolean = false
            private set
        var additionalDataForDropInService: Bundle? = null
            private set

        /**
         * Create a [DropInConfiguration]
         *
         * @param context   A context
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        /**
         * Constructor for Builder with default values.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey
        )

        /**
         * Create a Builder with the same values of an existing Configuration object.
         */
        constructor(dropInConfiguration: DropInConfiguration) : super(dropInConfiguration) {
            amount = dropInConfiguration.amount
            showPreselectedStoredPaymentMethod = dropInConfiguration.showPreselectedStoredPaymentMethod
            skipListWhenSinglePaymentMethod = dropInConfiguration.skipListWhenSinglePaymentMethod
            isRemovingStoredPaymentMethodsEnabled = dropInConfiguration.isRemovingStoredPaymentMethodsEnabled
            additionalDataForDropInService = dropInConfiguration.additionalDataForDropInService
        }

        override fun setShopperLocale(shopperLocale: Locale): Builder {
            return super.setShopperLocale(shopperLocale) as Builder
        }

        override fun setEnvironment(environment: Environment): Builder {
            return super.setEnvironment(environment) as Builder
        }

        fun setAmount(amount: Amount): Builder {
            if (!CheckoutCurrency.isSupported(amount.currency) || amount.value < 0) {
                throw CheckoutException("Currency is not valid.")
            }
            this.amount = amount
            return this
        }

        /**
         * When set to false, Drop-in will skip the preselected screen and go straight to the payment methods list.
         */
        fun setShowPreselectedStoredPaymentMethod(showStoredPaymentMethod: Boolean): Builder {
            this.showPreselectedStoredPaymentMethod = showStoredPaymentMethod
            return this
        }

        /**
         * When set to true, Drop-in will skip the payment methods list screen if there is only a single payment method available and no stored
         * payment methods.
         *
         * This only applies to payment methods that require a component (user input). Which means redirect payment methods, SDK payment methods,
         * etc will not be skipped even if this flag is set to true and a single payment method is present.
         */
        fun setSkipListWhenSinglePaymentMethod(skipListWhenSinglePaymentMethod: Boolean): Builder {
            this.skipListWhenSinglePaymentMethod = skipListWhenSinglePaymentMethod
            return this
        }

        /**
         * When set to true, users can remove their stored payment methods by swiping left on the corresponding row in the payment methods screen.
         *
         * You need to implement [DropInService.removeStoredPaymentMethod] to handle the removal.
         */
        fun setEnableRemovingStoredPaymentMethods(isEnabled: Boolean): Builder {
            this.isRemovingStoredPaymentMethodsEnabled = isEnabled
            return this
        }

        /**
         * Pass a custom Bundle to Drop-in. This Bundle will passed to the [DropInService] and can be read using [DropInService.getAdditionalData].
         */
        fun setAdditionalDataForDropInService(additionalDataForDropInService: Bundle): Builder {
            this.additionalDataForDropInService = additionalDataForDropInService
            return this
        }

        /**
         * Add configuration for Credit Card payment method.
         */
        fun addCardConfiguration(cardConfiguration: CardConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.SCHEME] = cardConfiguration
            return this
        }

        /**
         * Add configuration for iDeal payment method.
         */
        fun addIdealConfiguration(idealConfiguration: IdealConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.IDEAL] = idealConfiguration
            return this
        }

        /**
         * Add configuration for MolPay Thailand payment method.
         */
        fun addMolpayThailandConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.MOLPAY_THAILAND] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for MolPay Malasya payment method.
         */
        fun addMolpayMalasyaConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.MOLPAY_MALAYSIA] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for MolPay Vietnam payment method.
         */
        fun addMolpayVietnamConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.MOLPAY_VIETNAM] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for DotPay payment method.
         */
        fun addDotpayConfiguration(dotpayConfiguration: DotpayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.DOTPAY] = dotpayConfiguration
            return this
        }

        /**
         * Add configuration for Online Banking Czech Republic payment method.
         */
        fun addOnlineBankingCZConfiguration(onlineBankingCZConfiguration: OnlineBankingCZConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.ONLINE_BANKING_CZ] = onlineBankingCZConfiguration
            return this
        }

        /**
         * Add configuration for Online Banking Poland payment method.
         */
        fun addOnlineBankingPLConfiguration(onlineBankingPLConfiguration: OnlineBankingPLConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.ONLINE_BANKING_PL] = onlineBankingPLConfiguration
            return this
        }

        /**
         * Add configuration for Online Banking Slovakia payment method.
         */
        fun addOnlineBankingSKConfiguration(onlineBankingSKConfiguration: OnlineBankingSKConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.ONLINE_BANKING_SK] = onlineBankingSKConfiguration
            return this
        }

        /**
         * Add configuration for EPS payment method.
         */
        fun addEpsConfiguration(epsConfiguration: EPSConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.EPS] = epsConfiguration
            return this
        }

        /**
         * Add configuration for EnterCash payment method.
         */
        fun addEntercashConfiguration(entercashConfiguration: EntercashConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.ENTERCASH] = entercashConfiguration
            return this
        }

        /**
         * Add configuration for Open Banking payment method.
         */
        fun addOpenBankingConfiguration(openBankingConfiguration: OpenBankingConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.OPEN_BANKING] = openBankingConfiguration
            return this
        }

        /**
         * Add configuration for Google Pay payment method.
         */
        fun addGooglePayConfiguration(googlePayConfiguration: GooglePayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.GOOGLE_PAY] = googlePayConfiguration
            availablePaymentConfigs[PaymentMethodTypes.GOOGLE_PAY_LEGACY] = googlePayConfiguration
            return this
        }

        /**
         * Add configuration for Sepa payment method.
         */
        fun addSepaConfiguration(sepaConfiguration: SepaConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.SEPA] = sepaConfiguration
            return this
        }

        /**
         * Add configuration for BCMC payment method.
         */
        fun addBcmcConfiguration(bcmcConfiguration: BcmcConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.BCMC] = bcmcConfiguration
            return this
        }

        /**
         * Add configuration for MB WAY payment method.
         */
        fun addMBWayConfiguration(mbwayConfiguration: MBWayConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.MB_WAY] = mbwayConfiguration
            return this
        }

        /**
         * Add configuration for Blik payment method.
         */
        fun addBlikConfiguration(blikConfiguration: BlikConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.BLIK] = blikConfiguration
            return this
        }

        /**
         * Add configuration for BACS Direct Debit payment method.
         */
        fun addBacsDirectDebitConfiguration(bacsDirectDebitConfiguration: BacsDirectDebitConfiguration): Builder {
            availablePaymentConfigs[PaymentMethodTypes.BACS] = bacsDirectDebitConfiguration
            return this
        }

        /**
         * Add configuration for 3DS2 action.
         */
        fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Await action.
         */
        fun addAwaitActionConfiguration(configuration: AwaitConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for QR code action.
         */
        fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Redirect action.
         */
        fun addRedirectActionConfiguration(configuration: RedirectConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for WeChat Pay action.
         */
        fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        /**
         * Add configuration for Voucher action.
         */
        fun addVoucherActionConfiguration(configuration: VoucherConfiguration): Builder {
            availableActionConfigs[configuration::class.java] = configuration
            return this
        }

        override fun buildInternal(): DropInConfiguration {
            return DropInConfiguration(this)
        }
    }
}
