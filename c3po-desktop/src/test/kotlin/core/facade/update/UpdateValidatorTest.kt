package core.facade.update

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UpdateValidatorTest {

    private val validator = UpdateValidator()

    @Test
    fun `compareVersions - newer version returns true`() {
        assertThat(validator.compareVersions("2.1.0", "2.0.1")).isTrue()
        assertThat(validator.compareVersions("3.0.0", "2.9.9")).isTrue()
        assertThat(validator.compareVersions("1.0.1", "1.0.0")).isTrue()
    }

    @Test
    fun `compareVersions - older version returns false`() {
        assertThat(validator.compareVersions("2.0.1", "2.1.0")).isFalse()
        assertThat(validator.compareVersions("1.9.9", "2.0.0")).isFalse()
        assertThat(validator.compareVersions("1.0.0", "1.0.1")).isFalse()
    }

    @Test
    fun `compareVersions - same version returns false`() {
        assertThat(validator.compareVersions("2.0.1", "2.0.1")).isFalse()
        assertThat(validator.compareVersions("1.0.0", "1.0.0")).isFalse()
        assertThat(validator.compareVersions("3.2.1", "3.2.1")).isFalse()
    }

    @Test
    fun `compareVersions - version with v prefix handled correctly`() {
        assertThat(validator.compareVersions("v2.1.0", "2.0.1")).isTrue()
        assertThat(validator.compareVersions("2.1.0", "v2.0.1")).isTrue()
        assertThat(validator.compareVersions("v2.1.0", "v2.0.1")).isTrue()
    }

    @Test
    fun `compareVersions - semantic versioning edge cases`() {
        assertThat(validator.compareVersions("2.0", "1.9.9")).isTrue()
        assertThat(validator.compareVersions("2.0.0", "1.9")).isTrue()
        assertThat(validator.compareVersions("1.9", "2.0.0")).isFalse()
    }

    @Test
    fun `compareVersions - invalid version throws exception`() {
        assertThrows<IllegalArgumentException> {
            validator.compareVersions("invalid", "2.0.1")
        }

        assertThrows<IllegalArgumentException> {
            validator.compareVersions("2.0.1", "not.a.version")
        }

        assertThrows<IllegalArgumentException> {
            validator.compareVersions("2.a.1", "2.0.1")
        }
    }

    @Test
    fun `calculateChecksum - valid file returns correct hash`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Hello World")

        val checksum = validator.calculateChecksum(testFile)

        // SHA-256 of "Hello World"
        assertThat(checksum).isEqualTo("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e")
    }

    @Test
    fun `calculateChecksum - non-existent file throws exception`(@TempDir tempDir: File) {
        val nonExistentFile = File(tempDir, "does-not-exist.txt")

        assertThrows<IllegalArgumentException> {
            validator.calculateChecksum(nonExistentFile)
        }
    }

    @Test
    fun `calculateChecksum - empty file throws exception`(@TempDir tempDir: File) {
        val emptyFile = File(tempDir, "empty.txt")
        emptyFile.createNewFile()

        assertThrows<IllegalArgumentException> {
            validator.calculateChecksum(emptyFile)
        }
    }

    @Test
    fun `validateChecksum - matching checksums return true`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Hello World")

        val expectedChecksum = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"

        assertThat(validator.validateChecksum(testFile, expectedChecksum)).isTrue()
        assertThat(validator.validateChecksum(testFile, expectedChecksum.uppercase())).isTrue()
    }

    @Test
    fun `validateChecksum - mismatched checksums return false`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Hello World")

        val wrongChecksum = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"

        assertThat(validator.validateChecksum(testFile, wrongChecksum)).isFalse()
    }

    @Test
    fun `validateChecksum - null checksum returns true`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Hello World")

        assertThat(validator.validateChecksum(testFile, null)).isTrue()
        assertThat(validator.validateChecksum(testFile, "")).isTrue()
        assertThat(validator.validateChecksum(testFile, "   ")).isTrue()
    }
}