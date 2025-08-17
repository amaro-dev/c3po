package core.model

import core.command.PermissionFlag

data class DeclaredPermissions(
    val ownerApp: String,
    val permissions: Map<String, List<PermissionFlag>>,
)
