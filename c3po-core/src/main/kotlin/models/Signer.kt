package models

import exceptions.IncompleteSignerInformationException

data class Signer(
    val commonName: String?,
    val organizationUnit: String?,
    val organization: String?,
    val locality: String?,
    val state: String?,
    val countryCode: String?
) {
    companion object {
        fun fromParams(vararg params: String): Signer {
            val fields = params.map { it.trim().split("=") }.associate { it[0] to it[1] }

            if (!fields.keys.hasAllAttrs() && !fields.hasDebugSignature())
                throw IncompleteSignerInformationException()

            return Signer(fields["CN"], fields["OU"], fields["O"], fields["L"], fields["ST"], fields["C"])
        }

        private fun Set<String>.hasAllAttrs() = containsAll(
            listOf("CN", "OU", "O", "L", "ST", "C")
        )

        private fun Map<String, String>.hasDebugSignature() =
            "CN" in keys && this["CN"] == "Android Debug"
    }

    val isDebug = commonName == "Android Debug"
}
