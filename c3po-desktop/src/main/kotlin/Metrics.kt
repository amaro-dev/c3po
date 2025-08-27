import io.sentry.Sentry

class Metrics {
    fun start() {
        Sentry.init { options ->
            options.dsn =
                "https://838cce296a9f651e6588678a9604107e@o4508842340843520.ingest.us.sentry.io/4508842344972288"
            options.tracesSampleRate = 1.0
            options.isDebug = true
            options.release = AppVersion.getAppVersion()
        }
    }
}
