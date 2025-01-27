/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.issuerlist.IssuerModel
import com.adyen.checkout.paybybank.databinding.PayByBankViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PayByBankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: PayByBankViewBinding = PayByBankViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: PayByBankDelegate

    private var payByBankRecyclerAdapter: PayByBankRecyclerAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(0, padding, 0, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is PayByBankDelegate) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        initSearchQueryInput()
        initIssuersRecyclerView()
    }

    private fun observeDelegate(delegate: PayByBankDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { onOutputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun onOutputDataChanged(outputData: PayByBankOutputData) {
        payByBankRecyclerAdapter?.submitList(outputData.issuers)
        binding.textViewNoMatchingIssuers.isVisible = outputData.issuers.isEmpty()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        // TODO strings
    }

    private fun onItemClicked(issuerModel: IssuerModel) {
        Logger.d(TAG, "onItemClicked - ${issuerModel.name}")
        delegate.updateInputData { selectedIssuer = issuerModel }
    }

    private fun initSearchQueryInput() {
        binding.editTextSearchQuery.setOnChangeListener {
            delegate.updateInputData { query = it.toString() }
        }
    }

    private fun initIssuersRecyclerView() {
        payByBankRecyclerAdapter = PayByBankRecyclerAdapter(
            imageLoader = ImageLoader.getInstance(context, delegate.configuration.environment),
            paymentMethod = delegate.getPaymentMethodType(),
            onItemClicked = ::onItemClicked
        ).apply {
            submitList(delegate.getIssuers())
        }
        binding.recyclerIssuers.adapter = payByBankRecyclerAdapter
    }

    override val isConfirmationRequired: Boolean = false

    override fun highlightValidationErrors() {
        // no validation
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
