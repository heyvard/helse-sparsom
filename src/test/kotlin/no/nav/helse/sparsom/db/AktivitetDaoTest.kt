package no.nav.helse.sparsom.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparsom.Nivå.INFO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDateTime
import java.util.*

@TestInstance(PER_CLASS)
internal class AktivitetDaoTest: AbstractDatabaseTest() {

    private lateinit var aktivitetDao: AktivitetDao
    private lateinit var hendelseDao: HendelseDao

    @BeforeAll
    fun beforeAll() {
        aktivitetDao = AktivitetDao { dataSource }
        hendelseDao = HendelseDao { dataSource }
    }

    @Test
    fun lagre() {
        val hendelseId = hendelseDao.lagre("12345678910", UUID.randomUUID(), "{}", LocalDateTime.now())
        aktivitetDao.lagre(INFO, "en melding", LocalDateTime.now(), hendelseId, listOf(Triple("Person", "fødselsnummer", "12345678910")))
        assertAntallRader(1, 1, 1)
    }

    @Test
    fun `lagre med flere kontekster`() {
        val hendelseId = hendelseDao.lagre("12345678910", UUID.randomUUID(), "{}", LocalDateTime.now())
        aktivitetDao.lagre(
            INFO,
            "en melding",
            LocalDateTime.now(),
            hendelseId,
            listOf(
                Triple("Person", "fødselsnummer", "12345678910"),
                Triple("Vedtaksperiode", "vedtaksperiodeId", "${UUID.randomUUID()}")
            )
        )
        assertAntallRader(1, 2, 2)
    }

    @Test
    fun duplikatkontroll() {
        val hendelseId = hendelseDao.lagre("12345678910", UUID.randomUUID(), "{}", LocalDateTime.now())
        val tidsstempel = LocalDateTime.now()
        val vedtaksperiodeid = UUID.randomUUID()
        aktivitetDao.lagre(INFO, "en melding", tidsstempel, hendelseId, listOf(
                Triple("Person", "fødselsnummer", "12345678910"),
                Triple("Vedtaksperiode", "vedtaksperiodeId", "$vedtaksperiodeid")
            )
        )
        aktivitetDao.lagre(INFO, "en melding", tidsstempel, hendelseId, listOf(
                Triple("Person", "fødselsnummer", "12345678910"),
                Triple("Vedtaksperiode", "vedtaksperiodeId", "$vedtaksperiodeid")
            )
        )
        assertAntallRader(
            forventetAntallAktiviteter = 1,
            forventetAntallKontekster = 2,
            forventetAntallKoblinger = 2
        )
    }

    @Test
    fun `ulike hendelser med samme kontekst`() {
        val hendelseId1 = hendelseDao.lagre("12345678910", UUID.randomUUID(), "{}", LocalDateTime.now())
        val hendelseId2 = hendelseDao.lagre("12345678910", UUID.randomUUID(), "{}", LocalDateTime.now())
        aktivitetDao.lagre(INFO, "en melding", LocalDateTime.now(), hendelseId1, listOf(Triple("Person", "fødselsnummer", "12345678910")))
        aktivitetDao.lagre(INFO, "en annen melding", LocalDateTime.now(), hendelseId2, listOf(Triple("Person", "fødselsnummer", "12345678910")))
        assertAntallRader(2, 1, 2)
    }

    private fun assertAntallRader(forventetAntallAktiviteter: Int, forventetAntallKontekster: Int, forventetAntallKoblinger: Int) {
        val faktiskAntallAktiviteter = antallAktiviteter()
        val faktiskAntallKontekster = antallKontekster()
        val faktiskAntallKoblinger = antallKoblinger()
        assertEquals(forventetAntallAktiviteter, faktiskAntallAktiviteter) { "forventet antall aktiviteter: $forventetAntallAktiviteter, faktisk: $faktiskAntallAktiviteter" }
        assertEquals(forventetAntallKontekster, faktiskAntallKontekster) { "forventet antall kontekster: $forventetAntallKontekster, faktisk: $faktiskAntallKontekster" }
        assertEquals(forventetAntallKoblinger, faktiskAntallKoblinger) { "forventet antall koblinger: $forventetAntallKoblinger, faktisk: $faktiskAntallKoblinger" }
    }

    private fun antallAktiviteter() = sessionOf((dataSource)).use {
        requireNotNull(it.run(queryOf("SELECT COUNT(1) FROM aktivitet").map { it.int(1) }.asSingle))
    }

    private fun antallKontekster() = sessionOf((dataSource)).use {
        requireNotNull(it.run(queryOf("SELECT COUNT(1) FROM kontekst").map { it.int(1) }.asSingle))
    }

    private fun antallKoblinger() = sessionOf((dataSource)).use {
        requireNotNull(it.run(queryOf("SELECT COUNT(1) FROM aktivitet_kontekst").map { it.int(1) }.asSingle))
    }
}