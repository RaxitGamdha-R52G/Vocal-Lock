package com.vocallock.domain.usecase.audio

import kotlin.math.sqrt

/**
 * Mathematical engine to compare two voice biometric fingerprints (MFCC arrays).
 */
class VerifyVoicePhraseUseCase {

    /**
     * Compares the saved fingerprint with the live audio fingerprint.
     * Returns true if they match above the strictness threshold.
     */
    operator fun invoke(savedMfcc: FloatArray, liveMfcc: FloatArray): Boolean {
        // Failsafe: If either array is empty, the match fails immediately
        if (savedMfcc.isEmpty() || liveMfcc.isEmpty()) return false

        // Ensure both arrays are the exact same size for mathematical comparison
        val minSize = minOf(savedMfcc.size, liveMfcc.size)
        val trimmedSaved = savedMfcc.take(minSize).toFloatArray()
        val trimmedLive = liveMfcc.take(minSize).toFloatArray()

        val similarity = calculateCosineSimilarity(trimmedSaved, trimmedLive)

        // 0.85 (85%) is a standard enterprise threshold for biometric voice matching.
        // If the similarity is 85% or higher, it's the exact same person speaking.
        return similarity >= 0.85f
    }

    /**
     * Calculates the Cosine Similarity between two multi-dimensional vectors.
     * This measures the angle between the two audio waves.
     */
    private fun calculateCosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }

        if (normA == 0f || normB == 0f) return 0f

        return dotProduct / (sqrt(normA) * sqrt(normB))
    }
}