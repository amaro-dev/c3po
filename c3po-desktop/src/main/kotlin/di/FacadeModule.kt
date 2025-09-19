package di

import Settings
import core.command.CommandExecutor
import core.command.SystemCommandExecutor
import core.facade.AdbFinder
import core.facade.ApkSignatureExtractor
import core.facade.SettingsRepository
import core.facade.SettingsRepositoryImpl
import core.facade.SignatureExtractor
import di.Names.RESOURCES_PATH_DEPENDENCY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

val FacadeModule =
    module {

        single {
            CoroutineScope(Dispatchers.IO)
        }

        single<File>(named(RESOURCES_PATH_DEPENDENCY)) {
            if (Settings.isDebug()) {
                File(Paths.get("build/resources/main").absolutePathString())
            } else {
                File(Settings.productionSettingsFolder()).apply {
                    if (!exists()) Files.createDirectory(toPath())
                }
            }
        }


        single { CommandExecutor() }

        single { SystemCommandExecutor() }

        single { AdbFinder(get()) }

        factory { SignatureExtractor() }

        factory { ApkSignatureExtractor(get(), get()) }

        factory<SettingsRepository> {
            SettingsRepositoryImpl(get(named(RESOURCES_PATH_DEPENDENCY)), Settings.FILE_NAME)
        }


        factory<Clipboard> { Toolkit.getDefaultToolkit().systemClipboard }
    }
