/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway

import android.content.Context
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class MBWayConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
) : Configuration {

    private constructor(builder: Builder) : this(
        builder.shopperLocale,
        builder.environment,
        builder.clientKey
    )

    /**
     * Builder to create a [MBWayConfiguration].
     */
    class Builder : BaseConfigurationBuilder<MBWayConfiguration> {

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
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey
        )

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: MBWayConfiguration) : super(configuration)

        override fun buildInternal(): MBWayConfiguration {
            return MBWayConfiguration(this)
        }
    }
}
