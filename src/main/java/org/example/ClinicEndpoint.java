package org.example;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import org.hibernate.search.mapper.pojo.standalone.mapping.SearchMapping;
import org.hibernate.search.mapper.pojo.standalone.session.SearchSession;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/clinic")
public class ClinicEndpoint {

    @Inject
    SearchMapping searchMapping;

    void onStart(@Observes StartupEvent ev) {
        try (var searchSession = searchMapping.createSession()) {
            if (0 < searchSession.search(Clinic.class)
                    .where(f -> f.matchAll())
                    .fetchTotalHitCount()) {
                return;
            }
            for (Clinic clinic : initialDataSet()) {
                searchSession.indexingPlan().add(clinic);
            }
        }
    }

    private List<Clinic> initialDataSet() {
        return List.of(
                new Clinic(UUID.randomUUID(), "Klinika Starogardzka", "Leczenie chorób psychicznych", "08:00:00", "20:00:00", List.of("BLIK", "CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Zdrowia", "Terapia rodzin i dzieci", "09:00:00", "18:00:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Słoneczna", "Rehabilitacja neurologiczna", "07:00:00", "19:00:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Medica", "Leczenie uzależnień", "08:30:00", "21:00:00", List.of("BLIK", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Nova", "Psychoterapia indywidualna", "08:00:00", "20:00:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Harmonia", "Terapia par", "09:00:00", "17:00:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Vita", "Leczenie depresji", "08:00:00", "19:00:00", List.of("BLIK", "CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Centrum", "Terapia zajęciowa", "07:30:00", "20:00:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Zdrowie Plus", "Wsparcie psychiczne dzieci", "08:00:00", "18:30:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Alpha", "Leczenie zaburzeń snu", "09:00:00", "20:00:00", List.of("BLIK", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Beta", "Terapia poznawczo-behawioralna", "08:00:00", "19:00:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Gamma", "Pomoc psychologiczna młodzieży", "08:30:00", "18:00:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Delta", "Wsparcie osób starszych", "07:00:00", "16:00:00", List.of("BLIK", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Epsilon", "Terapia grupowa", "08:00:00", "19:00:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Zeta", "Leczenie lęków i stresu", "09:00:00", "20:00:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Omega", "Wsparcie psychiczne dla rodzin", "08:00:00", "18:00:00", List.of("BLIK", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Sigma", "Psychoterapia indywidualna", "07:30:00", "19:30:00", List.of("CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Theta", "Leczenie depresji i zaburzeń nastroju", "08:00:00", "20:00:00", List.of("BLIK", "CARD", "CASH")),
                new Clinic(UUID.randomUUID(), "Klinika Lambda", "Terapia uzależnień", "09:00:00", "21:00:00", List.of("BLIK", "CARD")),
                new Clinic(UUID.randomUUID(), "Klinika Kappa", "Wsparcie psychologiczne", "08:00:00", "19:00:00", List.of("BLIK", "CARD", "CASH"))
        );
    }

    private Clinic getAuthor(SearchSession searchSession, UUID id) {
        return searchSession.search(Clinic.class)
                .where(f -> f.id().matching(id))
                .fetchSingleHit()
                .orElseThrow(NotFoundException::new);
    }


    @GET
    @Path("/search")
    public List<Clinic> searchAuthors(@RestQuery String pattern,
                                      @RestQuery Optional<Integer> size) {
        try (var searchSession = searchMapping.createSession()) {
            List<Clinic> clinics = searchSession.search(Clinic.class)
                    .where(f -> pattern == null || pattern.isBlank()
                            ? f.matchAll()
                            : f.simpleQueryString()
                            .fields("name", "description","open","close","paymentMethods").matching(pattern))
                    .sort(f -> f.field("name_sort").then().field("description_sort"))
                    .fetchHits(size.orElse(20));
            return clinics;
        }
    }
}
