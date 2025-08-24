/* TODO: Fix compilation errors
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import java.nio.file.Path
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
    fun `Get directories list`() {
        val directory =
            this.javaClass.classLoader
                .getResource("")
                .path
        val baseDir = Path("$directory/..").normalize()
        assertThat(PathFinder().listDirectories("$directory/.."))
            .containsExactlyInAnyOrder(
                baseDir.resolve("main"),
                baseDir.resolve("test"),
            )
    }

    @Test
    fun `Get apksigner path latest`() {
        val pathFinder = spyk(PathFinder())
        every { pathFinder.listDirectories(any()) } returns
                listOf(
                    Path("/Users/name/Library/Android/sdk/build-tools/34.0.0"),
                    Path("/Users/name/Library/Android/sdk/build-tools/33.0.0"),
                )
        val path = pathFinder.getApkSignerPath("/Users/name/Library/Android/sdk")
        assertThat(path).isEqualTo("/Users/name/Library/Android/sdk/build-tools/34.0.0/apksigner")
    }
}

class PathFinder {
    fun getAndroidPath(adbPath: String): String = Path(adbPath).parent.parent.pathString

    fun getApkSignerPath(androidPath: String): String {
        val path = Path(androidPath, "build-tools").pathString
        val options =
            listDirectories(path)
                .map {
                    it.name
                }.first()
        return Path(path, options, "apksigner").pathString
    }

    fun listDirectories(path: String): List<Path> = Path(path).normalize().listDirectoryEntries()
}
*/
