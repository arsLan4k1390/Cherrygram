/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.chats.gemini

data object GeminiDTO {

    data class Response(
        val candidates: List<Candidate>
    )

    data class Candidate(
        val content: Content
    )

    data class Content(
        val parts: List<Part>
    )

    data class Part(
        val text: String
    )

}