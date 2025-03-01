package di

import Settings
import commands.CommandExecutor
import di.Names.RESOURCES_PATH_DEPENDENCY
import facade.CompanionCommander
import facade.CompanionCommanderImpl
import facade.SettingsRepository
import facade.SettingsRepositoryImpl
import facade.SignatureExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import socket.SocketClient
import socket.SocketDriver
import socket.SocketResponseAggregator
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

val FacadeModule = module {

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

    single { SocketResponseAggregator() }

    single { SocketDriver(get(), get()) }

    single { SocketClient() }

    single { CommandExecutor() }

    factory { SignatureExtractor() }

    factory<SettingsRepository> {
        SettingsRepositoryImpl(get(named(RESOURCES_PATH_DEPENDENCY)), Settings.FILE_NAME)
    }

    factory<CompanionCommander> { CompanionCommanderImpl(get(), get(named(RESOURCES_PATH_DEPENDENCY))) }

    factory<Clipboard> { Toolkit.getDefaultToolkit().systemClipboard }
}
