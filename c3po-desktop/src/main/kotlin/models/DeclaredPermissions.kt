package models

import commands.PermissionFlag

data class DeclaredPermissions(
    val ownerApp: String,
    val permissions: Map<String, List<PermissionFlag>>,
)
