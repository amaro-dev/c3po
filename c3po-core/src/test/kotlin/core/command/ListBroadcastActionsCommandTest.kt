package core.command

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ListBroadcastActionsCommandTest {

    private val command = ListBroadcastActionsCommand()

    @ParameterizedTest
    @ValueSource(strings = ["api23"])  // Start with one real fixture
    fun `parses broadcast actions from real Android output`(apiDir: String) {
        // Load fixture from test resources
        val fixtureContent = loadFixture(apiDir)

        // Parse the content
        val result = command.parse(fixtureContent)

        // Basic assertions that should hold for real data
        assertTrue(result.isNotEmpty(), "Should find broadcast actions in $apiDir")

        // Verify we have some expected system actions
        val actions = result.map { it.action }
        assertTrue(actions.any { it.contains("BOOT_COMPLETED") }, "Should find BOOT_COMPLETED action")
        assertTrue(actions.any { it.contains("CONNECTIVITY_CHANGE") }, "Should find CONNECTIVITY_CHANGE action")

        // Verify we have expected packages
        val packages = result.map { it.packageName }.toSet()
        assertTrue(packages.contains("system"), "Should find system package")
        assertTrue(packages.any { it.contains("gms") }, "Should find GMS package")

        // Check permission handling
        val actionsWithPermissions = result.filter { it.requiredPermission != null }
        assertTrue(actionsWithPermissions.isNotEmpty(), "Should find some actions with required permissions")

        // Verify specific known permission from our fixture
        val gmsInternalAction = result.find {
            it.action == "com.google.android.gms.INSTANT_APP_STOPPED" &&
                    it.packageName == "com.google.android.gms.persistent"
        }
        assertNotNull(gmsInternalAction, "Should find GMS internal broadcast action")
        assertEquals("com.google.android.gms.permission.INTERNAL_BROADCAST", gmsInternalAction!!.requiredPermission)

        // Verify voice search permission
        val voiceSearchAction = result.find {
            it.action.contains("googlequicksearchbox") &&
                    it.packageName.contains("googlequicksearchbox")
        }
        if (voiceSearchAction != null) {
            assertEquals("android.permission.MANAGE_VOICE_KEYPHRASES", voiceSearchAction.requiredPermission)
        }

        println("Found ${result.size} broadcast actions from ${packages.size} packages in $apiDir")
        println("Actions with permissions: ${actionsWithPermissions.size}")
    }

    @Test
    fun `handles empty input gracefully`() {
        val result = command.parse("")
        assertTrue(result.isEmpty(), "Empty input should return empty list")
    }

    @Test
    fun `handles malformed input gracefully`() {
        val malformedInput = "This is not a valid dumpsys output\nRandom text here"
        val result = command.parse(malformedInput)
        assertTrue(result.isEmpty(), "Malformed input should return empty list")
    }

    @Test
    fun `handles input without registered receivers section`() {
        val inputWithoutRegistered = """
        ACTIVITY MANAGER BROADCAST STATE (dumpsys activity broadcasts)
        Historical broadcasts [foreground]:
        #0: Some broadcast record
        Sticky broadcasts for user 0:
        * Sticky action android.intent.action.BATTERY_CHANGED:
        """.trimIndent()

        val result = command.parse(inputWithoutRegistered)
        assertTrue(result.isEmpty(), "Should return empty list when no Registered Receivers section")
    }

    @Test
    fun `extracts package names correctly`() {
        val testInput = """
        ACTIVITY MANAGER BROADCAST STATE (dumpsys activity broadcasts)
          Registered Receivers:
          * ReceiverList{123 456 com.example.app/10001/u0 remote:789}
            app=456:com.example.app/u0a1001 pid=456 uid=10001 user=0
            Filter #0: BroadcastFilter{abc}
              Action: "android.intent.action.TEST_ACTION"
              AutoVerify=false
          * ReceiverList{234 567 system/1000/u0 local:890}
            app=567:system/1000 pid=567 uid=1000 user=0
            Filter #0: BroadcastFilter{def}
              Action: "android.intent.action.SYSTEM_ACTION"
              AutoVerify=false
        """.trimIndent()

        val result = command.parse(testInput)
        assertEquals(2, result.size)

        val appAction = result.find { it.packageName == "com.example.app" }
        assertNotNull(appAction)
        assertEquals("android.intent.action.TEST_ACTION", appAction!!.action)

        val systemAction = result.find { it.packageName == "system" }
        assertNotNull(systemAction)
        assertEquals("android.intent.action.SYSTEM_ACTION", systemAction!!.action)
    }

    @Test
    fun `handles multiple actions per filter`() {
        val testInput = """
        ACTIVITY MANAGER BROADCAST STATE (dumpsys activity broadcasts)
          Registered Receivers:
          * ReceiverList{123 456 com.example.app/10001/u0 remote:789}
            app=456:com.example.app/u0a1001 pid=456 uid=10001 user=0
            Filter #0: BroadcastFilter{abc}
              Action: "android.intent.action.ACTION_ONE"
              Action: "android.intent.action.ACTION_TWO"
              Action: "android.intent.action.ACTION_THREE"
              AutoVerify=false
        """.trimIndent()

        val result = command.parse(testInput)
        assertEquals(3, result.size)

        val packageNames = result.map { it.packageName }.toSet()
        assertEquals(setOf("com.example.app"), packageNames)

        val actions = result.map { it.action }.sorted()
        assertEquals(
            listOf(
                "android.intent.action.ACTION_ONE",
                "android.intent.action.ACTION_THREE",
                "android.intent.action.ACTION_TWO"
            ).sorted(),
            actions
        )
    }

    @Test
    fun `handles permissions per filter correctly`() {
        val testInput = """
        ACTIVITY MANAGER BROADCAST STATE (dumpsys activity broadcasts)
          Registered Receivers:
          * ReceiverList{123 456 com.example.app/10001/u0 remote:789}
            app=456:com.example.app/u0a1001 pid=456 uid=10001 user=0
            Filter #0: BroadcastFilter{abc}
              Action: "android.intent.action.ACTION_NO_PERM"
              AutoVerify=false
            Filter #1: BroadcastFilter{def}
              Action: "android.intent.action.ACTION_WITH_PERM"
              AutoVerify=false
              requiredPermission=android.permission.TEST_PERMISSION
        """.trimIndent()

        val result = command.parse(testInput)
        assertEquals(2, result.size)

        val noPerm = result.find { it.action == "android.intent.action.ACTION_NO_PERM" }
        assertNotNull(noPerm)
        assertNull(noPerm!!.requiredPermission)

        val withPerm = result.find { it.action == "android.intent.action.ACTION_WITH_PERM" }
        assertNotNull(withPerm)
        assertEquals("android.permission.TEST_PERMISSION", withPerm!!.requiredPermission)
    }

    private fun loadFixture(apiDir: String): String {
        val resourcePath = "/fixtures/broadcasts/$apiDir/sample.txt"
        val resource = this::class.java.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Fixture not found: $resourcePath")

        return resource.bufferedReader().use { it.readText() }
    }
}