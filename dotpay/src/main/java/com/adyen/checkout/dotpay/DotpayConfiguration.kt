/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.dotpay

import android.content.Context
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import com.adyen.checkout.issuerlist.IssuerListViewType
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class DotpayConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val viewType: IssuerListViewType,
    override val hideIssuerLogos: Boolean,
) : IssuerListConfiguration() {

    /**
     * Builder to create a [DotpayConfiguration].
     */
    class Builder : IssuerListBuilder<DotpayConfiguration> {

        /**
         * Constructor for Builder with default values.
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
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: DotpayConfiguration) : super(configuration) {
            viewType = configuration.viewType
            hideIssuerLogos = configuration.hideIssuerLogos
        }

        override fun setShopperLocale(shopperLocale: Locale): Builder {
            return super.setShopperLocale(shopperLocale) as Builder
        }

        override fun setEnvironment(environment: Environment): Builder {
            return super.setEnvironment(environment) as Builder
        }

        override fun buildInternal(): DotpayConfiguration {
            return DotpayConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
            )
        }
    }
}
