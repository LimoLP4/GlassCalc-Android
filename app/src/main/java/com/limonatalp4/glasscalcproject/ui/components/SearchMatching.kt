package com.limonatalp4.glasscalcproject.ui.components

import java.text.Normalizer
import java.util.Locale

private val WHITESPACE_SPLIT_REGEX = Regex("\\s+")
private val WORD_BOUNDARY_SPLIT_REGEX = Regex("[\\s\\-_/(),:;]+")
private val KEYWORD_SPLIT_CHARS = charArrayOf(',', ';', '、', '，')

/**
 * Normalisiert Suchtexte sprachunabhängig und fehlertolerant:
 * - Führende/nachfolgende sowie CJK-Vollbreiten-Leerzeichen (U+3000) entfernen
 * - Case-insensitiv über Locale.ROOT (sicher für Kyrillisch, Klingonisch, Deutsch, etc.)
 * - Kyrillisches 'ё' auf 'е' abbilden
 */
fun normalizeSearchRaw(input: String): String {
    if (input.isEmpty()) return ""
    return input
        .replace('\u3000', ' ')
        .replace('\u00A0', ' ')
        .trim()
        .lowercase(Locale.ROOT)
        .replace('ё', 'е')
}

/**
 * Entfernt diakritische Zeichen ausschließlich nach lateinischen Basisbuchstaben (a-z)
 * und faltet 'ß' -> "ss", ohne nicht-lateinische Schriften (z. B. Devanagari/Hindi,
 * Japanisch Dakuten oder Kyrillisch й) zu verfälschen.
 */
fun foldLatinForSearch(normalizedLower: String): String {
    if (normalizedLower.isEmpty()) return ""
    val eszettExpanded = normalizedLower.replace("ß", "ss")
    val decomposed = Normalizer.normalize(eszettExpanded, Normalizer.Form.NFD)
    val sb = StringBuilder(decomposed.length)
    var prevWasAsciiLatin = false
    for (ch in decomposed) {
        val type = Character.getType(ch)
        if (type == Character.NON_SPACING_MARK.toInt() && prevWasAsciiLatin) {
            continue
        }
        sb.append(ch)
        prevWasAsciiLatin = ch in 'a'..'z'
    }
    return sb.toString()
}

fun tokenizeSearchQuery(query: String): List<String> {
    val normalized = normalizeSearchRaw(query)
    if (normalized.isEmpty()) return emptyList()
    return normalized
        .split(WHITESPACE_SPLIT_REGEX)
        .filter { it.isNotEmpty() }
}

fun splitSearchKeywords(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    return raw
        .split(*KEYWORD_SPLIT_CHARS)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

private data class PreparedCandidate(
    val raw: String,
    val folded: String,
    val rawWords: List<String>,
    val foldedWords: List<String>
)

private fun prepareCandidate(text: String): PreparedCandidate? {
    val raw = normalizeSearchRaw(text)
    if (raw.isEmpty()) return null
    val folded = foldLatinForSearch(raw)
    val rawWords = raw.split(WORD_BOUNDARY_SPLIT_REGEX).filter { it.isNotEmpty() }
    val foldedWords = folded.split(WORD_BOUNDARY_SPLIT_REGEX).filter { it.isNotEmpty() }
    return PreparedCandidate(
        raw = raw,
        folded = folded,
        rawWords = rawWords,
        foldedWords = foldedWords
    )
}

/**
 * Bewertet die Relevanz eines Eintrags für die gegebenen Such-Tokens.
 * Rückgabe: Score >= 0 (kleiner = relevanter) oder null, falls mindestens ein Token nicht passt.
 *
 * Prioritäten:
 * - Exakter Treffer auf Primärtitel / Einheitensymbol: 0..1
 * - Primärtitel / Symbol beginnt mit Suchbegriff: 10..11
 * - Wortanfang im Primärtitel beginnt mit Suchbegriff: 20..21
 * - Exakter Keyword-Treffer: 25..26
 * - Keyword oder Wort im Keyword beginnt mit Suchbegriff: 30..35
 * - Wortanfang in Sekundärtext (Beschreibung/Formel) beginnt mit Suchbegriff: 36..37
 * - Teilwort (Substring) irgendwo in Primärtitel / Symbol: 40+
 * - Teilwort (Substring) irgendwo in Keywords: 55+
 * - Teilwort (Substring) irgendwo in Sekundärtext: 65+
 */
fun computeMatchScore(
    tokens: List<String>,
    primaryNames: List<String>,
    secondaryNames: List<String> = emptyList(),
    keywords: List<String> = emptyList()
): Int? {
    if (tokens.isEmpty()) return 0

    val primary = primaryNames.mapNotNull(::prepareCandidate)
    val keywordList = keywords.mapNotNull(::prepareCandidate)
    val secondary = secondaryNames.mapNotNull(::prepareCandidate)
    if (primary.isEmpty() && keywordList.isEmpty() && secondary.isEmpty()) return null

    var totalScore = 0
    for (token in tokens) {
        val tRaw = normalizeSearchRaw(token)
        val tFold = foldLatinForSearch(tRaw)
        if (tRaw.isEmpty()) continue

        var bestTokenScore = Int.MAX_VALUE

        for (cand in primary) {
            val score = scorePrimaryCandidate(cand, tRaw, tFold)
            if (score != null && score < bestTokenScore) {
                bestTokenScore = score
            }
        }
        for (cand in keywordList) {
            val score = scoreKeywordCandidate(cand, tRaw, tFold)
            if (score != null && score < bestTokenScore) {
                bestTokenScore = score
            }
        }
        for (cand in secondary) {
            val score = scoreSecondaryCandidate(cand, tRaw, tFold)
            if (score != null && score < bestTokenScore) {
                bestTokenScore = score
            }
        }

        if (bestTokenScore == Int.MAX_VALUE) {
            return null
        }
        totalScore += bestTokenScore
    }
    return totalScore
}

private fun scorePrimaryCandidate(
    cand: PreparedCandidate,
    tRaw: String,
    tFold: String
): Int? {
    if (cand.raw == tRaw) return 0
    if (cand.folded == tFold) return 1
    if (cand.raw.startsWith(tRaw)) return 10
    if (cand.folded.startsWith(tFold)) return 11
    if (cand.rawWords.any { it.startsWith(tRaw) }) return 20
    if (cand.foldedWords.any { it.startsWith(tFold) }) return 21

    val rawIdx = cand.raw.indexOf(tRaw)
    if (rawIdx >= 0) return 40 + rawIdx.coerceAtMost(14)

    val foldIdx = cand.folded.indexOf(tFold)
    if (foldIdx >= 0) return 45 + foldIdx.coerceAtMost(14)

    return null
}

private fun scoreKeywordCandidate(
    cand: PreparedCandidate,
    tRaw: String,
    tFold: String
): Int? {
    if (cand.raw == tRaw) return 25
    if (cand.folded == tFold) return 26
    if (cand.raw.startsWith(tRaw)) return 30
    if (cand.folded.startsWith(tFold)) return 31
    if (cand.rawWords.any { it.startsWith(tRaw) }) return 34
    if (cand.foldedWords.any { it.startsWith(tFold) }) return 35

    val rawIdx = cand.raw.indexOf(tRaw)
    if (rawIdx >= 0) return 55 + rawIdx.coerceAtMost(9)

    val foldIdx = cand.folded.indexOf(tFold)
    if (foldIdx >= 0) return 58 + foldIdx.coerceAtMost(9)

    return null
}

private fun scoreSecondaryCandidate(
    cand: PreparedCandidate,
    tRaw: String,
    tFold: String
): Int? {
    if (cand.rawWords.any { it.startsWith(tRaw) }) return 36
    if (cand.foldedWords.any { it.startsWith(tFold) }) return 37

    val rawIdx = cand.raw.indexOf(tRaw)
    if (rawIdx >= 0) return 65 + rawIdx.coerceAtMost(19)

    val foldIdx = cand.folded.indexOf(tFold)
    if (foldIdx >= 0) return 68 + foldIdx.coerceAtMost(19)

    return null
}
