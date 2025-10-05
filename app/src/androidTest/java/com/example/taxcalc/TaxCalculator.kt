package com.example.taxcalc

enum class FilingStatus { SINGLE, MARRIED_JOINT }

object TaxCalculator {

    data class Bracket(val upTo: Double?, val rate: Double)
    private data class Schedule(val single: List<Bracket>, val marriedJoint: List<Bracket>)

    private val schedules = mapOf(
        2024 to Schedule(
            single = listOf(
                Bracket(11_600.0, 0.10), Bracket(47_150.0, 0.12), Bracket(100_525.0, 0.22),
                Bracket(191_950.0, 0.24), Bracket(243_725.0, 0.32), Bracket(609_350.0, 0.35),
                Bracket(null, 0.37)
            ),
            marriedJoint = listOf(
                Bracket(23_200.0, 0.10), Bracket(94_300.0, 0.12), Bracket(201_050.0, 0.22),
                Bracket(383_900.0, 0.24), Bracket(487_450.0, 0.32), Bracket(731_200.0, 0.35),
                Bracket(null, 0.37)
            )
        ),
        2025 to Schedule(
            single = listOf(
                Bracket(11_925.0, 0.10), Bracket(48_475.0, 0.12), Bracket(103_350.0, 0.22),
                Bracket(197_300.0, 0.24), Bracket(250_525.0, 0.32), Bracket(626_350.0, 0.35),
                Bracket(null, 0.37)
            ),
            marriedJoint = listOf(
                Bracket(23_850.0, 0.10), Bracket(96_950.0, 0.12), Bracket(206_700.0, 0.22),
                Bracket(394_600.0, 0.24), Bracket(501_050.0, 0.32), Bracket(751_600.0, 0.35),
                Bracket(null, 0.37)
            )
        )
        // 2026: add when official IRS brackets publish.
    )

    data class Result(val totalTax: Double, val effectiveRate: Double, val marginalRate: Double)

    fun compute(taxYear: Int, filingStatus: FilingStatus, taxableIncome: Double): Result {
        require(taxableIncome >= 0) { "Income must be non-negative." }
        val schedule = schedules[taxYear]
            ?: throw IllegalArgumentException("Brackets for $taxYear not available.")

        val brackets = if (filingStatus == FilingStatus.SINGLE)
            schedule.single else schedule.marriedJoint

        var remaining = taxableIncome
        var lastCap = 0.0
        var total = 0.0
        var marginal = 0.0

        for (b in brackets) {
            val slice = if (b.upTo == null) remaining
            else (b.upTo - lastCap).coerceAtLeast(0.0).coerceAtMost(remaining)

            if (slice > 0) {
                total += slice * b.rate
                remaining -= slice
                marginal = b.rate
            }
            if (b.upTo != null) lastCap = b.upTo
            if (remaining <= 0) break
        }
        val eff = if (taxableIncome > 0) total / taxableIncome else 0.0
        return Result(totalTax = total, effectiveRate = eff, marginalRate = marginal)
    }
}
