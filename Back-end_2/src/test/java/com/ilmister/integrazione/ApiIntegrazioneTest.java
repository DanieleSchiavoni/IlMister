package com.ilmister.integrazione;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmister.entity.enumerazioni.RuoloUtente;
import com.ilmister.repository.UtenteRepository;

/** L'applicazione intera, provata come la userebbe il frontend: richieste HTTP vere, sicurezza compresa */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrazioneTest {

    private static final String PASSWORD = "password-sicura";
    private static final AtomicInteger CONTATORE = new AtomicInteger();

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UtenteRepository utenteRepository;

    /** Una sessione di prova: i due token di un utente appena registrato */
    private record Sessione(String username, String accessToken, String refreshToken) {
        String intestazione() {
            return "Bearer " + accessToken;
        }
    }

    private ResultActions inviaJson(String metodo, String percorso, Object corpo, String token) throws Exception {
        var richiesta = switch (metodo) {
            case "POST" -> post(percorso);
            case "PATCH" -> patch(percorso);
            default -> throw new IllegalArgumentException(metodo);
        };
        richiesta.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo));
        if (token != null) {
            richiesta.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(richiesta);
    }

    private JsonNode corpo(ResultActions risultato) throws Exception {
        return json.readTree(risultato.andReturn().getResponse().getContentAsString());
    }

    private Sessione registraEAccedi() throws Exception {
        String username = "utente" + CONTATORE.incrementAndGet() + "x" + System.nanoTime() % 100000;
        inviaJson("POST", "/api/auth/register",
                new java.util.LinkedHashMap<>(java.util.Map.of("username", username, "email", username + "@test.it",
                        "password", PASSWORD)), null).andExpect(status().isCreated());
        return accedi(username);
    }

    private Sessione accedi(String username) throws Exception {
        JsonNode risposta = corpo(inviaJson("POST", "/api/auth/login",
                java.util.Map.of("identificativo", username, "password", PASSWORD), null).andExpect(status().isOk()));
        return new Sessione(username, risposta.get("accessToken").asText(), risposta.get("refreshToken").asText());
    }

    private Sessione registraAdmin() throws Exception {
        Sessione normale = registraEAccedi();
        var utente = utenteRepository.findByUsernameIgnoreCase(normale.username()).orElseThrow();
        utente.setRuolo(RuoloUtente.ADMIN);
        utenteRepository.saveAndFlush(utente);
        // Il ruolo sta nel token: serve un nuovo accesso
        return accedi(normale.username());
    }

    private long creaCarriera(Sessione sessione) throws Exception {
        JsonNode anteprima = corpo(mvc.perform(get("/api/anteprima/campionato").param("seme", "7")
                .header("Authorization", sessione.intestazione())).andExpect(status().isOk()));
        String squadra = anteprima.get("squadre").get(0).get("nome").asText();
        ResultActions creata = inviaJson("POST", "/api/carriere", java.util.Map.of("nome", "La mia carriera",
                "nomeAllenatore", "Mister Prova", "nomeSquadra", squadra, "seme", 7), sessione.accessToken())
                .andExpect(status().isCreated()).andExpect(header().exists("Location"));
        return corpo(creata).get("id").asLong();
    }

    // ---------- accesso e registrazione ----------

    @Test
    void senzaTokenLeRotteProtetteRispondono401ConUnErroreJson() throws Exception {
        mvc.perform(get("/api/carriere")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.errore").value("NON_AUTENTICATO"));
    }

    @Test
    void unTokenInventatoRispondeComeSenzaToken() throws Exception {
        mvc.perform(get("/api/carriere").header("Authorization", "Bearer questo.non.vale"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void leRotteLibereNonVogliononToken() throws Exception {
        mvc.perform(get("/api/ping")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void laRegistrazioneRispondeSenzaPasswordEDuplicatiDanno409() throws Exception {
        String username = "duplicato" + System.nanoTime() % 100000;
        var dati = java.util.Map.of("username", username, "email", username + "@test.it", "password", PASSWORD);

        inviaJson("POST", "/api/auth/register", dati, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username)).andExpect(jsonPath("$.ruolo").value("UTENTE"))
                .andExpect(jsonPath("$.password").doesNotExist());
        inviaJson("POST", "/api/auth/register", dati, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errore").value("CONFLITTO"));
    }

    @Test
    void unaRegistrazioneNonValidaDa400ConIlElencoDeiCampi() throws Exception {
        inviaJson("POST", "/api/auth/register", java.util.Map.of("username", "a", "email", "non-una-email",
                "password", "corta"), null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campi", hasSize(3)));
    }

    @Test
    void ilLoginSbagliatoDa401() throws Exception {
        Sessione sessione = registraEAccedi();

        inviaJson("POST", "/api/auth/login", java.util.Map.of("identificativo", sessione.username(),
                "password", "sbagliata-per-forza"), null).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errore").value("NON_AUTENTICATO"));
    }

    @Test
    void conIlTokenSiLeggeIlProprioProfilo() throws Exception {
        Sessione sessione = registraEAccedi();

        mvc.perform(get("/api/utenti/me").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(sessione.username()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // ---------- refresh e logout ----------

    @Test
    void ilRefreshDaTokenNuoviEIlVecchioNonVale() throws Exception {
        Sessione sessione = registraEAccedi();

        JsonNode nuovi = corpo(inviaJson("POST", "/api/auth/refresh",
                java.util.Map.of("refreshToken", sessione.refreshToken()), null).andExpect(status().isOk()));
        mvc.perform(get("/api/utenti/me").header("Authorization", "Bearer " + nuovi.get("accessToken").asText()))
                .andExpect(status().isOk());

        // Riusare il vecchio è sospetto: 401 e si chiude anche la sessione nuova
        inviaJson("POST", "/api/auth/refresh", java.util.Map.of("refreshToken", sessione.refreshToken()), null)
                .andExpect(status().isUnauthorized());
        inviaJson("POST", "/api/auth/refresh", java.util.Map.of("refreshToken", nuovi.get("refreshToken").asText()),
                null).andExpect(status().isUnauthorized());
    }

    @Test
    void ilLogoutRevocaIlRefreshToken() throws Exception {
        Sessione sessione = registraEAccedi();

        inviaJson("POST", "/api/auth/logout", java.util.Map.of("refreshToken", sessione.refreshToken()), null)
                .andExpect(status().isNoContent());
        inviaJson("POST", "/api/auth/refresh", java.util.Map.of("refreshToken", sessione.refreshToken()), null)
                .andExpect(status().isUnauthorized());
    }

    // ---------- ruoli ----------

    @Test
    void lAreaAdminEVietataAgliUtentiNormali() throws Exception {
        Sessione normale = registraEAccedi();

        mvc.perform(get("/api/admin/utenti").header("Authorization", normale.intestazione()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.errore").value("ACCESSO_NEGATO"));
    }

    @Test
    void lAdminVedeGliUtentiELiPuoBloccare() throws Exception {
        Sessione admin = registraAdmin();
        Sessione vittima = registraEAccedi();
        long vittimaId = corpo(mvc.perform(get("/api/utenti/me").header("Authorization", vittima.intestazione())))
                .get("id").asLong();

        mvc.perform(get("/api/admin/utenti").header("Authorization", admin.intestazione()))
                .andExpect(status().isOk());
        inviaJson("PATCH", "/api/admin/utenti/" + vittimaId + "/abilitato", java.util.Map.of("abilitato", false),
                admin.accessToken()).andExpect(status().isOk()).andExpect(jsonPath("$.abilitato").value(false));

        // Bloccato: il login e il rinnovo non funzionano più
        inviaJson("POST", "/api/auth/login", java.util.Map.of("identificativo", vittima.username(),
                "password", PASSWORD), null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errore").value("ACCOUNT_BLOCCATO"));
        inviaJson("POST", "/api/auth/refresh", java.util.Map.of("refreshToken", vittima.refreshToken()), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lAdminNonPuoBloccareSeStesso() throws Exception {
        Sessione admin = registraAdmin();
        long id = corpo(mvc.perform(get("/api/utenti/me").header("Authorization", admin.intestazione())))
                .get("id").asLong();

        inviaJson("PATCH", "/api/admin/utenti/" + id + "/abilitato", java.util.Map.of("abilitato", false),
                admin.accessToken()).andExpect(status().isBadRequest());
    }

    // ---------- carriere e ownership ----------

    @Test
    void unaCarrieraSiCreaESiVedeNelleProprieCarriere() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);

        mvc.perform(get("/api/carriere").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id)).andExpect(jsonPath("$[0].giornateTotali").value(18));
    }

    @Test
    void lAltroUtenteNonVedeNeToccaLaCarrieraDiUnAltro() throws Exception {
        Sessione proprietario = registraEAccedi();
        Sessione intruso = registraEAccedi();
        long id = creaCarriera(proprietario);

        mvc.perform(get("/api/carriere/" + id).header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/carriere/" + id + "/classifica").header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/carriere/" + id + "/giornata/avanza").header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/carriere/" + id).header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/carriere").header("Authorization", intruso.intestazione()))
                .andExpect(jsonPath("$", hasSize(0)));
        // E al proprietario la carriera è rimasta
        mvc.perform(get("/api/carriere/" + id).header("Authorization", proprietario.intestazione()))
                .andExpect(status().isOk());
    }

    @Test
    void unaCarrieraNonValidaDa400() throws Exception {
        Sessione sessione = registraEAccedi();

        inviaJson("POST", "/api/carriere", java.util.Map.of("nome", "", "nomeAllenatore", "X", "nomeSquadra", "Y"),
                sessione.accessToken()).andExpect(status().isBadRequest());
    }

    // ---------- il gioco, dall'inizio alla fine ----------

    @Test
    void siGiocaUnaGiornataEPoiLaClassificaSiMuove() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String base = "/api/carriere/" + id;

        mvc.perform(get(base + "/calendario").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(18)))
                .andExpect(jsonPath("$[0].partite", hasSize(5)));

        JsonNode giocata = corpo(mvc.perform(post(base + "/giornata/avanza")
                .header("Authorization", sessione.intestazione())).andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(1)).andExpect(jsonPath("$.risultati", hasSize(5))));
        long partitaId = giocata.get("risultati").get(0).get("partitaId").asLong();

        mvc.perform(get(base + "/classifica").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].giocate").value(1));
        mvc.perform(get(base + "/partite/" + partitaId).header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk());
        mvc.perform(get(base).header("Authorization", sessione.intestazione()))
                .andExpect(jsonPath("$.prossimaGiornata").value(2));
    }

    @Test
    void ilPrimoTempoEPoiLaConclusioneSenzaDecisioni() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String base = "/api/carriere/" + id;

        mvc.perform(post(base + "/partita/primo-tempo").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk());
        // Il corpo è facoltativo: senza decisioni si gioca il secondo tempo com'era
        mvc.perform(post(base + "/partita/conclusione").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numero").value(1));
    }

    @Test
    void unaCarrieraEliminataNonCeNePiu() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);

        mvc.perform(delete("/api/carriere/" + id).header("Authorization", sessione.intestazione()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/carriere/" + id).header("Authorization", sessione.intestazione()))
                .andExpect(status().isNotFound());
    }

    // ---------- mercato, finanze, allenamento ----------

    @Test
    void ilMercatoSenzaTokenDa401() throws Exception {
        mvc.perform(get("/api/carriere/1/mercato/giocatori")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/carriere/1/finanze")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/carriere/1/allenamento")).andExpect(status().isUnauthorized());
    }

    @Test
    void siCompraUnGiocatoreEILeFinanzeLoRegistrano() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String base = "/api/carriere/" + id;

        JsonNode elenco = corpo(mvc.perform(get(base + "/mercato/giocatori")
                .header("Authorization", sessione.intestazione())).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(60))));
        JsonNode scelto = elenco.get(elenco.size() - 1);
        double offerta = scelto.get("valoreMercato").asDouble() * 1.5;

        mvc.perform(get(base + "/finanze").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.movimenti", hasSize(0)))
                .andExpect(jsonPath("$.giocatori").value(26));

        inviaJson("POST", base + "/mercato/offerte", java.util.Map.of("giocatoreId", scelto.get("id").asLong(),
                "importo", offerta), sessione.accessToken())
                .andExpect(status().isOk()).andExpect(jsonPath("$.conclusa").value(true));

        mvc.perform(get(base + "/finanze").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.giocatori").value(27))
                .andExpect(jsonPath("$.movimenti", hasSize(1))).andExpect(jsonPath("$.movimenti[0].tipo").value("ACQUISTO"));
    }

    @Test
    void unOffertaRifiutataNonESuErrore() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String base = "/api/carriere/" + id;
        JsonNode elenco = corpo(mvc.perform(get(base + "/mercato/giocatori")
                .header("Authorization", sessione.intestazione())));
        JsonNode scelto = elenco.get(0);

        inviaJson("POST", base + "/mercato/offerte", java.util.Map.of("giocatoreId", scelto.get("id").asLong(),
                "importo", 1), sessione.accessToken())
                .andExpect(status().isOk()).andExpect(jsonPath("$.conclusa").value(false));
    }

    @Test
    void unOffertaNonValidaDa400() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);

        inviaJson("POST", "/api/carriere/" + id + "/mercato/offerte", java.util.Map.of("giocatoreId", 1,
                "importo", -5), sessione.accessToken()).andExpect(status().isBadRequest());
    }

    @Test
    void ilMercatoDiUnAltraCarrieraDa404() throws Exception {
        Sessione proprietario = registraEAccedi();
        Sessione intruso = registraEAccedi();
        long id = creaCarriera(proprietario);

        mvc.perform(get("/api/carriere/" + id + "/mercato/giocatori").header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/carriere/" + id + "/finanze").header("Authorization", intruso.intestazione()))
                .andExpect(status().isNotFound());
    }

    @Test
    void ilFocusDiAllenamentoSiLeggeESiCambia() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String percorso = "/api/carriere/" + id + "/allenamento";

        mvc.perform(get(percorso).header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.focus").value("EQUILIBRATO"))
                .andExpect(jsonPath("$.opzioni", hasSize(5)));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(percorso)
                .header("Authorization", sessione.intestazione()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"focus\":\"FISICO\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.focus").value("FISICO"));
        mvc.perform(get(percorso).header("Authorization", sessione.intestazione()))
                .andExpect(jsonPath("$.focus").value("FISICO"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(percorso)
                .header("Authorization", sessione.intestazione()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"focus\":\"INESISTENTE\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void dopoUnaGiornataLeFinanzeHannoStipendiEBiglietti() throws Exception {
        Sessione sessione = registraEAccedi();
        long id = creaCarriera(sessione);
        String base = "/api/carriere/" + id;

        mvc.perform(post(base + "/giornata/avanza").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk());

        mvc.perform(get(base + "/finanze").header("Authorization", sessione.intestazione()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.movimenti[?(@.tipo=='STIPENDI')]", hasSize(1)))
                .andExpect(jsonPath("$.usciteTotali").isNumber());
    }
}
