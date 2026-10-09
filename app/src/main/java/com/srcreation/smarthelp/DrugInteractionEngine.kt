package com.srcreation.smarthelp

import com.srcreation.smarthelp.db.MedicineEntity

data class DrugConflictInfo(
    val conflictType: String,
    val existingMedName: String,
    val newMedName: String,
    val messageResId: Int
)

object DrugInteractionEngine {

    private val paracetamolBrands = setOf(
        "napa", "ace", "renova", "reset", "fast", "pyrex", "tamipen", 
        "fever", "paracetamol", "pamol", "xpa", "aceta", "paracod"
    )

    private val nsaidBrands = setOf(
        "naprosyn", "clofenac", "fenac", "tuplen", "flexi", "voltalin", 
        "ibuprofen", "diclofenac", "ketorolac", "indomet", "etoricoxib", "naproxen"
    )

    fun checkConflict(newMedName: String, existingMeds: List<MedicineEntity>): DrugConflictInfo? {
        val cleanNewName = newMedName.trim().lowercase()
        if (cleanNewName.isBlank()) return null

        val isNewParacetamol = paracetamolBrands.any { cleanNewName.contains(it) }
        val isNewNsaid = nsaidBrands.any { cleanNewName.contains(it) }

        existingMeds.filter { it.isActive }.forEach { existingMed ->
            val cleanExisting = existingMed.name.trim().lowercase()

            // Check Paracetamol Overdose Conflict
            if (isNewParacetamol && paracetamolBrands.any { cleanExisting.contains(it) }) {
                return DrugConflictInfo(
                    conflictType = "OVERDOSE_PARACETAMOL",
                    existingMedName = existingMed.name,
                    newMedName = newMedName,
                    messageResId = R.string.drug_conflict_paracetamol
                )
            }

            // Check Multiple NSAIDs Conflict
            if (isNewNsaid && nsaidBrands.any { cleanExisting.contains(it) }) {
                return DrugConflictInfo(
                    conflictType = "MULTIPLE_NSAID",
                    existingMedName = existingMed.name,
                    newMedName = newMedName,
                    messageResId = R.string.drug_conflict_nsaid
                )
            }
        }

        return null
    }
}
