/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.util.StringUtil
import java.util.Calendar
import java.util.GregorianCalendar

object CardValidationUtils {

    // Luhn Check
    private const val RADIX = 10
    private const val FIVE_DIGIT = 5

    // Card Number
    private const val MINIMUM_CARD_NUMBER_LENGTH = 8
    const val MAXIMUM_CARD_NUMBER_LENGTH = 19
    const val GENERAL_CARD_NUMBER_LENGTH = 16
    const val AMEX_CARD_NUMBER_LENGTH = 15

    // Security Code
    private const val GENERAL_CARD_SECURITY_CODE_SIZE = 3
    private const val AMEX_SECURITY_CODE_SIZE = 4

    // Date
    private const val MONTHS_IN_YEAR = 12
    private const val MAXIMUM_YEARS_IN_FUTURE = 30
    private const val MAXIMUM_EXPIRED_MONTHS = 3

    /**
     * Validate card number.
     */
    fun validateCardNumber(number: String): FieldState<String> {
        val normalizedNumber = StringUtil.normalize(number)
        val length = normalizedNumber.length
        val validation = when {
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedNumber) -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            length > MAXIMUM_CARD_NUMBER_LENGTH -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            length < MINIMUM_CARD_NUMBER_LENGTH -> Validation.Partial
            isLuhnChecksumValid(normalizedNumber) -> Validation.Valid
            length == MAXIMUM_CARD_NUMBER_LENGTH -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            else -> Validation.Partial
        }

        return FieldState(number, validation)
    }

    @Suppress("MagicNumber")
    private fun isLuhnChecksumValid(normalizedNumber: String): Boolean {
        var s1 = 0
        var s2 = 0
        val reverse = StringBuffer(normalizedNumber).reverse().toString()
        for (i in reverse.indices) {
            val digit = Character.digit(reverse[i], RADIX)
            if (i % 2 == 0) {
                s1 += digit
            } else {
                s2 += 2 * digit
                if (digit >= FIVE_DIGIT) {
                    s2 -= 9
                }
            }
        }
        return (s1 + s2) % 10 == 0
    }

    /**
     * Validate Expiry Date.
     */
    fun validateExpiryDate(expiryDate: ExpiryDate): FieldState<ExpiryDate> {
        if (dateExists(expiryDate)) {
            val expiryDateCalendar = getExpiryCalendar(expiryDate)
            val maxFutureCalendar = GregorianCalendar.getInstance()
            maxFutureCalendar.add(Calendar.YEAR, MAXIMUM_YEARS_IN_FUTURE)
            val maxPastCalendar = GregorianCalendar.getInstance()
            maxPastCalendar.add(Calendar.MONTH, -MAXIMUM_EXPIRED_MONTHS)

            // higher than maxPast and lower than maxFuture
            if (expiryDateCalendar >= maxPastCalendar && expiryDateCalendar <= maxFutureCalendar) {
                return FieldState(expiryDate, Validation.Valid)
            }
        }
        return FieldState(expiryDate, Validation.Invalid(R.string.checkout_expiry_date_not_valid))
    }

    /**
     * Validate Security Code.
     * We always pass CardType null, but we can enforce size validation for Amex or otherwise if necessary.
     */
    fun validateSecurityCode(securityCode: String, cardType: CardType?): FieldState<String> {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length
        var validation: Validation = Validation.Invalid(R.string.checkout_security_code_not_valid)
        if (StringUtil.isDigitsAndSeparatorsOnly(normalizedSecurityCode)) {
            if (cardType == CardType.AMERICAN_EXPRESS && length == AMEX_SECURITY_CODE_SIZE) {
                validation = Validation.Valid
            } else if (length == GENERAL_CARD_SECURITY_CODE_SIZE && cardType != CardType.AMERICAN_EXPRESS) {
                validation = Validation.Valid
            }
        }
        return FieldState(normalizedSecurityCode, validation)
    }

    private fun dateExists(expiryDate: ExpiryDate): Boolean {
        return (
            expiryDate !== ExpiryDate.EMPTY_DATE &&
                expiryDate.expiryMonth != ExpiryDate.EMPTY_VALUE &&
                expiryDate.expiryYear != ExpiryDate.EMPTY_VALUE &&
                isValidMonth(expiryDate.expiryMonth) &&
                expiryDate.expiryYear > 0
            )
    }

    private fun isValidMonth(month: Int): Boolean {
        return month in 1..MONTHS_IN_YEAR
    }

    private fun getExpiryCalendar(expiryDate: ExpiryDate): Calendar {
        val expiryCalendar = GregorianCalendar.getInstance()
        expiryCalendar.clear()
        // First day of the expiry month. Calendar.MONTH is zero-based.
        expiryCalendar[expiryDate.expiryYear, expiryDate.expiryMonth - 1] = 1
        // Go to next month and remove 1 day to be on the last day of the expiry month.
        expiryCalendar.add(Calendar.MONTH, 1)
        expiryCalendar.add(Calendar.DAY_OF_MONTH, -1)
        return expiryCalendar
    }
}
