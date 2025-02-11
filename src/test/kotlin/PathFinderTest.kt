import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

class PathFinderTest {

    @Test
    fun `Get ANDROID_HOME from adb path`() {
        val adbPath = "/Users/name/Library/Android/sdk/platform-tools/adb"
        assertThat(PathFinder().getAndroidPath(adbPath))
            .isEqualTo("/Users/name/Library/Android/sdk")
    }

    @Test
    fun `Get apksigner path latest`() {
        val adbPath = "/Users/name/Library/Android/sdk"
        assertThat(PathFinder().getApkSignerPath(adbPath))
            .isEqualTo("/Users/name/Library/Android/sdk/build-tools/34.0.0/apksigner")
    }

}

class PathFinder {

    fun getAndroidPath(adbPath: String): String {
        return Path(adbPath).parent.parent.pathString
    }

    fun getApkSignerPath(androidPath: String): String {
        val path = Path(androidPath, "build-tools")
        val options = path.listDirectoryEntries().map {
            it.name
        }.first()
        return Path(path.pathString, options, "apksigner").pathString
    }
}
