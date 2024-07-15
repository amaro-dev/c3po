package ui.plugins.intents.pending

enum class IntentFlags(val code: Long) {
    FLAG_ACTIVITY_BROUGHT_TO_FRONT(0x00400000),
    FLAG_ACTIVITY_CLEAR_TASK(0x00008000),
    FLAG_ACTIVITY_CLEAR_TOP(0x04000000),
    FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET(0x00080000),
    FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(0x00800000),
    FLAG_ACTIVITY_FORWARD_RESULT(0x02000000),
    FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY(0x00100000),
    FLAG_ACTIVITY_LAUNCH_ADJACENT(0x00001000),
    FLAG_ACTIVITY_MATCH_EXTERNAL(0x00000800),
    FLAG_ACTIVITY_MULTIPLE_TASK(0x08000000),
    FLAG_ACTIVITY_NEW_DOCUMENT(0x00080000),
    FLAG_ACTIVITY_NEW_TASK(0x10000000),
    FLAG_ACTIVITY_NO_ANIMATION(0x00010000),
    FLAG_ACTIVITY_NO_HISTORY(0x40000000),
    FLAG_ACTIVITY_NO_USER_ACTION(0x00040000),
    FLAG_ACTIVITY_PREVIOUS_IS_TOP(0x01000000),
    FLAG_ACTIVITY_REORDER_TO_FRONT(0x00020000),
    FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(0x00200000),
    FLAG_ACTIVITY_RETAIN_IN_RECENTS(0x00002000),
    FLAG_ACTIVITY_REQUIRE_DEFAULT(0x00000200),
    FLAG_ACTIVITY_REQUIRE_NON_BROWSER(0x00000400),
    FLAG_ACTIVITY_SINGLE_TOP(0x20000000),
    FLAG_ACTIVITY_TASK_ON_HOME(0x00004000),
    FLAG_DEBUG_LOG_RESOLUTION(0x00000008),
    FLAG_DIRECT_BOOT_AUTO(0x00000100),
    FLAG_EXCLUDE_STOPPED_PACKAGES(0x00000010),
    FLAG_FROM_BACKGROUND(0x00000004),
    FLAG_GRANT_PERSISTABLE_URI_PERMISSION(0x00000040),
    FLAG_GRANT_PREFIX_URI_PERMISSION(0x00000080),
    FLAG_GRANT_READ_URI_PERMISSION(0x00000001),
    FLAG_GRANT_WRITE_URI_PERMISSION(0x00000002),
    FLAG_INCLUDE_STOPPED_PACKAGES(0x00000020),
    FLAG_RECEIVER_FOREGROUND(0x10000000),
    FLAG_RECEIVER_NO_ABORT(0x08000000),
    FLAG_RECEIVER_REGISTERED_ONLY(0x40000000),
    FLAG_RECEIVER_REPLACE_PENDING(0x20000000),
    FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS(0x00200000);
}
