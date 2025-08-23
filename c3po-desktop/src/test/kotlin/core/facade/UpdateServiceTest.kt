import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import core.facade.UpdateService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.net.http.HttpResponse

class UpdateServiceTest {

    private val httpClient: HttpClient = mockk(relaxed = true)
    private val updateService = UpdateService(httpClient)

    @Test
    fun `checkForUpdates should return UpdateInfo when newer version available`() = runTest {
        val mockResponse: HttpResponse<String> = mockk()
        coEvery { mockResponse.statusCode() } returns 200
        coEvery { mockResponse.body() } returns """
            {
              "tag_name": "v2.1.0",
              "assets": [
                {
                  "name": "c3po-2.1.0.dmg",
                  "browser_download_url": "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg"
                }
              ]
            }
        """.trimIndent()

        coEvery { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse

        val result = updateService.checkForUpdates("2.0.1")

        assertThat(result?.version).isEqualTo("2.1.0")
        assertThat(result?.downloadUrl).isEqualTo("https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg")
    }

    @Test
    fun `checkForUpdates should return null when up to date`() = runTest {
        val mockResponse: HttpResponse<String> = mockk()
        coEvery { mockResponse.statusCode() } returns 200
        coEvery { mockResponse.body() } returns """
            {
              "tag_name": "v2.0.1",
              "assets": [
                {
                  "name": "c3po-2.0.1.dmg",
                  "browser_download_url": "https://github.com/amaro-dev/c3po/releases/download/v2.0.1/c3po-2.0.1.dmg"
                }
              ]
            }
        """.trimIndent()

        coEvery { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse

        val result = updateService.checkForUpdates("2.0.1")

        assertThat(result).isNull()
    }

    @Test
    fun `should compare versions correctly`() = runTest {
        val mockResponse: HttpResponse<String> = mockk()
        coEvery { mockResponse.statusCode() } returns 200
        coEvery { mockResponse.body() } returns """
            {
              "tag_name": "v2.1.0",
              "assets": [
                {
                  "name": "c3po-2.1.0.dmg", 
                  "browser_download_url": "https://github.com/amaro-dev/c3po/releases/download/v2.1.0/c3po-2.1.0.dmg"
                }
              ]
            }
        """.trimIndent()

        coEvery { httpClient.send(any(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse

        // Test newer version available
        val newerResult = updateService.checkForUpdates("2.0.1")
        assertThat(newerResult?.version).isEqualTo("2.1.0")

        // Test same version
        val sameResult = updateService.checkForUpdates("2.1.0")
        assertThat(sameResult).isNull()
    }

    @Test
    fun `should handle network errors gracefully`() = runTest {
        coEvery {
            httpClient.send(
                any(),
                any<HttpResponse.BodyHandler<String>>()
            )
        } throws RuntimeException("Network error")

        val result = updateService.checkForUpdates("2.0.1")

        assertThat(result).isNull()
    }
}