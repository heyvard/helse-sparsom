package no.nav.helse.sparsom

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparsom.db.AktivitetDao
import no.nav.helse.sparsom.db.DataSourceBuilder
import no.nav.helse.sparsom.db.HendelseDao

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSourceBuilder = DataSourceBuilder(env)
    dataSourceBuilder.migrate()
    val dataSource = dataSourceBuilder.getDataSource()

    return RapidApplication.create(env).apply {
        val aktivitetFactory = AktivitetFactory(AktivitetDao(dataSource))
        AktivitetRiver(this, HendelseDao(dataSource), aktivitetFactory)
        register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                dataSource.close()
            }
        })
    }
}