package br.unb.sds.gds2ephem.ephemv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Troca os nomes de estado e municipio do "geo" pelos ids das listas de lugares do ePHEM
 * (GET /states e /districts), como o guia da API recomenda.
 * <p>
 * O ePHEM compara nomes exatos (so ignora maiusculas), mas as listas de cada servidor tem grafias
 * proprias: em Cabo Verde "SANTO ANTAO" (sem til), "S.L.ÓRGÃOS", "P.NOVO", "RIBERIA GRANDE".
 * Aqui a comparacao ignora acentos, pontuacao e preposicoes, aceita iniciais ("S." = Sao/Santa/Santo)
 * e tolera um erro de digitacao por palavra longa. So usamos o id quando ha um unico candidato.
 * <p>
 * As listas ficam em cache por pais. Se nao for possivel carrega-las, o geo segue com os nomes e o
 * {@link EphemV2SignalPublisher} cuida do fallback.
 */
@Slf4j
public class EphemV2PlaceResolver {
    static final Set<String> STOPWORDS = Set.of("DE", "DO", "DOS", "DA", "DAS", "E");
    /**
     * Palavras a partir deste tamanho aceitam 1 erro (troca, falta, sobra ou inversao de letras).
     */
    static final int FUZZY_MIN_LENGTH = 5;

    private final EphemV2Client client;
    private final Duration cacheTtl;
    private final Clock clock;
    private final Map<String, Catalogo> cache = new ConcurrentHashMap<>();

    public EphemV2PlaceResolver(EphemV2Client client, Duration cacheTtl, Clock clock) {
        this.client = client;
        this.cacheTtl = cacheTtl;
        this.clock = clock;
    }

    /**
     * Altera o geo no lugar: estado e municipio encontrados viram ids; nomes sem correspondencia
     * sao removidos (evita um 422 certo), sempre preservando os niveis acima.
     */
    public void resolver(ObjectNode geo, long eventoId) {
        final var country = geo.path("country").asText("");
        final var stateName = textual(geo, "state");
        final var districtName = textual(geo, "district");
        if (country.length() != 2 || (stateName.isEmpty() && districtName.isEmpty())) {
            return;
        }
        final Catalogo catalogo;
        try {
            catalogo = catalogo(country);
        } catch (RuntimeException e) {
            log.warn("evento {}: nao foi possivel carregar os lugares de {} na ePHEM API v2 ({}); enviando nomes", eventoId, country, e.getMessage());
            return;
        }

        Long stateId = null;
        if (!stateName.isEmpty()) {
            final var state = unico(catalogo.states, stateName);
            if (state.isPresent()) {
                stateId = state.get().path("id").asLong();
                geo.put("state", stateId);
            } else {
                log.warn("evento {}: estado '{}' nao encontrado nos lugares de {}; sera inferido pelo municipio, se houver", eventoId, stateName, country);
                geo.remove("state");
            }
        }
        if (!districtName.isEmpty()) {
            final var filtroEstado = stateId;
            final var candidatos = catalogo.districts.stream()
                    .filter(d -> filtroEstado == null || d.path("state_id").asLong() == filtroEstado)
                    .collect(Collectors.toList());
            final var district = unico(candidatos, districtName);
            if (district.isPresent()) {
                geo.put("district", district.get().path("id").asLong());
                if (!geo.has("state")) {
                    geo.put("state", district.get().path("state_id").asLong());
                }
            } else {
                log.warn("evento {}: municipio '{}' nao encontrado nos lugares de {}; signal sem municipio", eventoId, districtName, country);
                geo.remove("district");
            }
        }
    }

    /**
     * Esquece as listas em cache (ex.: o ePHEM respondeu 422 para um id que ja nao existe).
     */
    public void invalidar(String country) {
        cache.remove(country);
    }

    private Catalogo catalogo(String country) {
        final var agora = clock.instant();
        final var atual = cache.get(country);
        if (atual != null && atual.expiraEm.isAfter(agora)) {
            return atual;
        }
        final var novo = new Catalogo(client.listarEstados(country), client.listarDistritos(country), agora.plus(cacheTtl));
        log.info("lugares de {} carregados da ePHEM API v2: {} estados, {} municipios", country, novo.states.size(), novo.districts.size());
        cache.put(country, novo);
        return novo;
    }

    /**
     * Correspondencia exata (normalizada) ganha; senao, aceita a aproximada se for unica.
     */
    static Optional<JsonNode> unico(List<JsonNode> linhas, String nome) {
        final var alvo = tokens(nome);
        if (alvo.isEmpty()) {
            return Optional.empty();
        }
        final var exatos = linhas.stream().filter(l -> tokens(l.path("name").asText()).equals(alvo)).collect(Collectors.toList());
        if (exatos.size() == 1) {
            return Optional.of(exatos.get(0));
        }
        if (exatos.size() > 1) {
            return Optional.empty();
        }
        final var aproximados = linhas.stream().filter(l -> parecidos(tokens(l.path("name").asText()), alvo)).collect(Collectors.toList());
        return aproximados.size() == 1 ? Optional.of(aproximados.get(0)) : Optional.empty();
    }

    static List<String> tokens(String nome) {
        final var semAcento = Normalizer.normalize(Optional.ofNullable(nome).orElse(""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", " ")
                .trim();
        if (semAcento.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(semAcento.split(" ")).filter(t -> !STOPWORDS.contains(t)).collect(Collectors.toList());
    }

    static boolean parecidos(List<String> a, List<String> b) {
        if (a.size() != b.size() || a.isEmpty()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!palavrasParecidas(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean palavrasParecidas(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        // inicial: "S" casa com SAO/SANTA/SANTO, "P" com PORTO...
        if (a.length() == 1 || b.length() == 1) {
            return a.charAt(0) == b.charAt(0);
        }
        return Math.min(a.length(), b.length()) >= FUZZY_MIN_LENGTH && distancia(a, b) <= 1;
    }

    /**
     * Distancia de Damerau-Levenshtein restrita (conta a inversao de duas letras vizinhas como 1).
     */
    static int distancia(String a, String b) {
        final var d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                final var custo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + custo);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }

    private static String textual(ObjectNode geo, String campo) {
        final var valor = geo.get(campo);
        return valor != null && valor.isTextual() ? valor.asText().trim() : "";
    }

    private static final class Catalogo {
        private final List<JsonNode> states;
        private final List<JsonNode> districts;
        private final Instant expiraEm;

        private Catalogo(List<JsonNode> states, List<JsonNode> districts, Instant expiraEm) {
            this.states = states;
            this.districts = districts;
            this.expiraEm = expiraEm;
        }
    }
}
