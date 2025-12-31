/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.gemini

import com.google.gson.annotations.SerializedName

class GeminiErrorDTO {

    data class ErrorResponse(
        val error: ErrorDetails
    )

    data class ErrorDetails(
        val code: Int,
        val message: String,
        val status: String,
        val details: List<ErrorDetail>? = null
    )

    data class ErrorDetail(
        @SerializedName("@type") val type: String,
        val reason: String? = null,
        val domain: String? = null,
        val metadata: Metadata? = null,
        val locale: String? = null,
        val localizedMessage: String? = null
    )

    data class Metadata(
        val service: String
    )

}