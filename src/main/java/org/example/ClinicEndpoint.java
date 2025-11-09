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
        // Index some test data if nothing exists
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
                new Clinic(UUID.randomUUID(), "Klinika Starogardzka ",
                        "Leczenie chorob psychicznych",
                        "08:00:00",
                        "20:00:00",
                        List.of("BLIK","CARD","CASH")),
                new Clinic(UUID.randomUUID(), "Szpital w Elblagu ",
                        "Szpital położniczy",
                        "08:00:00",
                        "20:00:00",
                        List.of("CARD","CASH")));
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
