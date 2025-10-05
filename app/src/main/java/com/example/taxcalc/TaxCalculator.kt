package com.example.taxcalc

/**
 * Federal tax calculator (2024–2025).
 * - Pass **taxable** income to [compute].
 * - Or call [taxableFromGross] first to subtract the standard deduction.
 * - 2026 intentionally not included until IRS publishes the official tables.
 */
enum class FilingStatus { SINGLE, MARRIED_JOINT, HEAD_OF_HOUSEHOLD }

object TaxCalculator {

    /** A single tax "tier". If [upTo] is null it means "no upper cap" (top bracket). */
    private data class Bracket(val upTo: Double?, val rate: Double)

    /** Per-year schedules by filing status. */
    private data class Schedule(
        val single: List<Bracket>,
        val marriedJoint: List<Bracket>,
        val headOfHousehold: List<Bracket>
    )

    // --- Bracket tables (taxable income) ---
    // 2024: IRS tables (S, MFJ, HoH)
    // 2025: IRS inflation adjustments (S, MFJ, HoH)
    private val schedules: Map<Int, Schedule> = mapOf(
        2024 to Schedule(
            single = listOf(
                Bracket(11_600.0, 0.10),
                Bracket(47_150.0, 0.12),
                Bracket(100_525.0, 0.22),
                Bracket(191_950.0, 0.24),
                Bracket(243_725.0, 0.32),
                Bracket(609_350.0, 0.35),
                Bracket(null, 0.37)
            ),
            marriedJoint = listOf(
                Bracket(23_200.0, 0.10),
                Bracket(94_300.0, 0.12),
                Bracket(201_050.0, 0.22),
                Bracket(383_900.0, 0.24),
                Bracket(487_450.0, 0.32),
                Bracket(731_200.0, 0.35),
                Bracket(null, 0.37)
            ),
            headOfHousehold = listOf(                 // NEW
                Bracket(16_550.0, 0.10),
                Bracket(63_100.0, 0.12),
                Bracket(100_500.0, 0.22),
                Bracket(191_950.0, 0.24),
                Bracket(243_700.0, 0.32),
                Bracket(609_350.0, 0.35),
                Bracket(null, 0.37)
            )
        ),
        2025 to Schedule(
            single = listOf(
                Bracket(11_925.0, 0.10),
                Bracket(48_475.0, 0.12),
                Bracket(103_350.0, 0.22),
                Bracket(197_300.0, 0.24),
                Bracket(250_525.0, 0.32),
                Bracket(626_350.0, 0.35),
                Bracket(null, 0.37)
            ),
            marriedJoint = listOf(
                Bracket(23_850.0, 0.10),
                Bracket(96_950.0, 0.12),
                Bracket(206_700.0, 0.22),
                Bracket(394_600.0, 0.24),
                Bracket(501_050.0, 0.32),
                Bracket(751_600.0, 0.35),
                Bracket(null, 0.37)
            ),
            headOfHousehold = listOf(                 // NEW
                Bracket(17_000.0, 0.10),
                Bracket(64_850.0, 0.12),
                Bracket(103_350.0, 0.22),
                Bracket(197_300.0, 0.24),
                Bracket(250_500.0, 0.32),
                Bracket(626_350.0, 0.35),
                Bracket(null, 0.37)
            )
        ),
        // 2026 will be added when IRS publishes official tables.
    )

    /** Result bundle. */
    data class Result(
        val totalTax: Double,
        val effectiveRate: Double,
        val marginalRate: Double
    )

    /**
     * Compute federal tax on **taxable** income (after deductions).
     * @throws IllegalArgumentException if year not supported.
     */
    fun compute(
        taxYear: Int,
        filingStatus: FilingStatus,
        taxableIncome: Double
    ): Result {
        require(taxableIncome >= 0.0) { "Income must be non-negative." }

        if (taxYear == 2026) {
            throw IllegalArgumentException("2026 brackets not available yet. Update when IRS publishes.")
        }

        val schedule = schedules[taxYear]
            ?: throw IllegalArgumentException("Brackets for $taxYear not available.")

        val brackets = when (filingStatus) {
            FilingStatus.SINGLE -> schedule.single
            FilingStatus.MARRIED_JOINT -> schedule.marriedJoint
            FilingStatus.HEAD_OF_HOUSEHOLD -> schedule.headOfHousehold
        }

        var remaining = taxableIncome
        var prevCap = 0.0
        var totalTax = 0.0
        var marginalRate = 0.0

        for (b in brackets) {
            val slice = if (b.upTo == null) {
                remaining
            } else {
                val width = (b.upTo - prevCap).coerceAtLeast(0.0)
                width.coerceAtMost(remaining)
            }

            if (slice > 0.0) {
                totalTax += slice * b.rate
                remaining -= slice
                marginalRate = b.rate
            }

            if (b.upTo != null) prevCap = b.upTo
            if (remaining <= 0.0) break
        }

        val effectiveRate = if (taxableIncome > 0.0) totalTax / taxableIncome else 0.0
        return Result(totalTax, effectiveRate, marginalRate)
    }

    // ---------- Optional helpers ----------

    /**
     * Convert **gross** income to **taxable** income by subtracting the standard deduction
     * (or your provided itemized deduction if larger).
     */
    fun taxableFromGross(
        taxYear: Int,
        filingStatus: FilingStatus,
        grossIncome: Double,
        itemizedDeduction: Double? = null
    ): Double {
        require(grossIncome >= 0.0) { "Income must be non-negative." }
        val std = standardDeduction(taxYear, filingStatus)
        val deduction = maxOf(std, itemizedDeduction ?: std)
        return (grossIncome - deduction).coerceAtLeast(0.0)
    }

    /**
     * Standard deduction (basic amounts; extra amounts for age/blindness not modeled).
     */
    fun standardDeduction(taxYear: Int, filingStatus: FilingStatus): Double {
        return when (taxYear) {
            2024 -> when (filingStatus) {
                FilingStatus.SINGLE -> 14_600.0
                FilingStatus.MARRIED_JOINT -> 29_200.0
                FilingStatus.HEAD_OF_HOUSEHOLD -> 21_900.0   // NEW
            }
            2025 -> when (filingStatus) {
                FilingStatus.SINGLE -> 15_000.0
                FilingStatus.MARRIED_JOINT -> 30_000.0
                FilingStatus.HEAD_OF_HOUSEHOLD -> 22_500.0   // NEW
            }
            2026 -> throw IllegalArgumentException("2026 standard deduction not available yet.")
            else -> throw IllegalArgumentException("Standard deduction for $taxYear not available.")
        }
    }
}