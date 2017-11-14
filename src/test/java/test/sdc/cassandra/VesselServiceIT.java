package test.sdc.cassandra;

import com.datastax.driver.core.ResultSet;
import org.assertj.core.api.SoftAssertions;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import test.sdc.model.CenterReference;
import test.sdc.model.Vessel;
import test.sdc.model.VesselCategoryReference;
import test.sdc.model.VisibilityType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static test.sdc.model.VisibilityType.ALL_CENTERS;
import static test.sdc.model.VisibilityType.CREATION_CENTER_ONLY;

public class VesselServiceIT {

    private static final Long STARTUP_TIMEOUT = 30_000L; // In milliseconds

    @Rule
    public CassandraCQLUnit cqlUnit = new CassandraCQLUnit(
            new ClassPathCQLDataSet("vessel_schema.cql", "vessel"),
            "test-cassandra.yaml", STARTUP_TIMEOUT);

    @InjectMocks
    private VesselService service;

    private static Vessel.Builder initVessel(final String name, final VisibilityType visibility, final CenterReference creationCenter) {
        return Vessel.newInstance()
                .withName(name)
                .withCategory("Cargo")
                .withVisibility(visibility)
                .withCreationCenter(creationCenter);
    }

    @Before
    public void init()
            throws Exception {
        MockitoAnnotations.initMocks(this);
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        this.service.setSession(EmbeddedCassandraServerHelper.getSession());
        this.service.postConstruct();
    }

    @Test
    public void should_add_vessel() {
        final String[] tables = {"vessels", "vessels_by_uuid"};
        final Vessel inputVessel = Vessel.newInstance()
                .withName("Le_Name")
                .withCategory("Cargo")
                .withVisibility(ALL_CENTERS)
                .withCreationCenter("Le_center")
                .build();
        for (final String table : tables) {
            final ResultSet contentBeforeOperation = cqlUnit.session.execute("SELECT * FROM " + table);
            assertThat(contentBeforeOperation).isEmpty();
        }

        this.service.add(inputVessel);

        final SoftAssertions softly = new SoftAssertions();
        for (final String table : tables) {
            final ResultSet result = cqlUnit.session.execute("SELECT * FROM " + table);
            softly.assertThat(result).hasSize(1);
        }
        softly.assertAll();
    }

    @Test
    public void should_add_vessel_on_update_if_absent() {
        final String[] tables = {"vessels", "vessels_by_uuid"};
        final Vessel inputVessel = Vessel.newInstance()
                .withName("Le_Name")
                .withCategory("Cargo")
                .withVisibility(ALL_CENTERS)
                .withCreationCenter("Le_center")
                .build();
        for (final String table : tables) {
            final ResultSet contentBeforeOperation = cqlUnit.session.execute("SELECT * FROM " + table);
            assertThat(contentBeforeOperation).isEmpty();
        }

        this.service.update(inputVessel);

        final SoftAssertions softly = new SoftAssertions();
        for (final String table : tables) {
            final ResultSet result = cqlUnit.session.execute("SELECT * FROM " + table);
            softly.assertThat(result).hasSize(1);
        }
        softly.assertAll();
    }

    @Test
    public void should_update_vessel() {
        final Vessel formerVessel = Vessel.newInstance()
                .withName("Le_Name")
                .withCategory("Cargo")
                .withVisibility(ALL_CENTERS)
                .withCreationCenter("Le_center")
                .build();
        this.service.add(formerVessel);
        final Vessel inputVessel = Vessel.fromUuid(formerVessel.getUuid())
                .withName("Le_Name_modified")
                .withCategory("Cargo")
                .withVisibility(CREATION_CENTER_ONLY)
                .withCreationCenter("Le_center")
                .build();

        this.service.update(inputVessel);

        final Optional<Vessel> actual = this.service.find(inputVessel.getUuid());
        assertThat(actual).contains(inputVessel);
    }

    @Test
    public void should_remove_vessel() {
        final Vessel formerVessel = Vessel.newInstance()
                .withName("Le_Name")
                .withCategory("Cargo")
                .withVisibility(ALL_CENTERS)
                .withCreationCenter("Le_center")
                .build();
        this.service.update(formerVessel);

        this.service.remove(formerVessel.getUuid());

        final Optional<Vessel> actual = this.service.find(formerVessel.getUuid());
        assertThat(actual).isEmpty();
    }

    @Test
    public void should_do_nothing_on_remove_if_absent() {
        final String inputUuid = UUID.randomUUID().toString();

        this.service.remove(inputUuid);

        final Optional<Vessel> actual = this.service.find(inputUuid);
        assertThat(actual).isEmpty();
    }

    @Test
    public void should_expose_vessels_depending_on_visibility() {
        final CenterReference localCenter = CenterReference.of("123");
        final CenterReference otherCenter = CenterReference.of("456");
        final Vessel globalVessel = initVessel("Global", ALL_CENTERS, otherCenter).build();
        final Vessel localVessel = initVessel("Local", CREATION_CENTER_ONLY, localCenter).build();
        final Vessel hiddenVessel = initVessel("Hidden", CREATION_CENTER_ONLY, otherCenter).build();
        for (final Vessel vessel : new Vessel[]{globalVessel, localVessel, hiddenVessel}) {
            this.service.update(vessel);
        }

        final List<Vessel> actual = this.service.findAll(localCenter);

        assertThat(actual).contains(globalVessel, localVessel)
                .doesNotContain(hiddenVessel);
    }

    @Test
    public void should_not_find_vessel_from_absent_UUID() {
        final String inputUuid = UUID.randomUUID().toString();

        final Optional<Vessel> actual = this.service.find(inputUuid);

        assertThat(actual).isEmpty();
    }

    @Test
    public void should_find_vessel_by_UUID() {
        final CenterReference otherCenter = CenterReference.of("123");
        final Vessel testVessel = initVessel("Hidden", CREATION_CENTER_ONLY, otherCenter).build();
        this.service.update(testVessel);

        final Optional<Vessel> actual = this.service.find(testVessel.getUuid());

        assertThat(actual).contains(testVessel);
    }

    @Test
    public void should_filter_list_of_visible_vessels_by_name_fragment() {
        final String inputNameFragment = "ENT";
        final CenterReference localCenter = CenterReference.of("123");
        final CenterReference otherCenter = CenterReference.of("456");
        final Vessel nonMatchingGlobalVessel = initVessel("Global", ALL_CENTERS, otherCenter).build();
        final Vessel matchingGlobalVessel = initVessel("Global ENT", ALL_CENTERS, otherCenter).build();
        final Vessel nonMatchingLocalVessel = initVessel("Local", CREATION_CENTER_ONLY, localCenter).build();
        final Vessel matchingLocalVessel = initVessel("ENT Local", CREATION_CENTER_ONLY, localCenter).build();
        final Vessel nonMatchingHiddenVessel = initVessel("Hidden", CREATION_CENTER_ONLY, otherCenter).build();
        final Vessel matchingHiddenVessel = initVessel("Hidden ENT", CREATION_CENTER_ONLY, otherCenter).build();
        for (final Vessel vessel : new Vessel[]{nonMatchingGlobalVessel, matchingGlobalVessel,
                nonMatchingLocalVessel, matchingLocalVessel, nonMatchingHiddenVessel, matchingHiddenVessel}) {
            this.service.update(vessel);
        }

        final List<Vessel> actual = this.service.findByNameFragment(localCenter, inputNameFragment);

        assertThat(actual).contains(matchingGlobalVessel, matchingLocalVessel)
                .doesNotContain(matchingHiddenVessel)
                .doesNotContain(nonMatchingGlobalVessel, nonMatchingHiddenVessel, nonMatchingLocalVessel);
    }

    @Test
    public void should_filter_list_of_visible_vessels_by_category() {
        final VesselCategoryReference inputCategory = VesselCategoryReference.of("cargo");
        final VesselCategoryReference otherCategory = VesselCategoryReference.of("windsurf");
        final CenterReference localCenter = CenterReference.of("123");
        final CenterReference otherCenter = CenterReference.of("456");
        final Vessel nonMatchingGlobalVessel = initVessel("Global nok", ALL_CENTERS, otherCenter)
                .withCategory(otherCategory).build();
        final Vessel matchingGlobalVessel = initVessel("Global ok", ALL_CENTERS, otherCenter)
                .withCategory(inputCategory).build();
        final Vessel nonMatchingLocalVessel = initVessel("Local nok", CREATION_CENTER_ONLY, localCenter)
                .withCategory(otherCategory).build();
        final Vessel matchingLocalVessel = initVessel("Local ok", CREATION_CENTER_ONLY, localCenter)
                .withCategory(inputCategory).build();
        final Vessel nonMatchingHiddenVessel = initVessel("Hidden nok", CREATION_CENTER_ONLY, otherCenter)
                .withCategory(otherCategory).build();
        final Vessel matchingHiddenVessel = initVessel("Hidden ok", CREATION_CENTER_ONLY, otherCenter)
                .withCategory(inputCategory).build();
        for (final Vessel vessel : new Vessel[]{nonMatchingGlobalVessel, matchingGlobalVessel,
                nonMatchingLocalVessel, matchingLocalVessel, nonMatchingHiddenVessel, matchingHiddenVessel}) {
            this.service.update(vessel);
        }

        final List<Vessel> actual = this.service.findByCategory(localCenter, inputCategory);

        assertThat(actual).contains(matchingGlobalVessel, matchingLocalVessel)
                .doesNotContain(matchingHiddenVessel)
                .doesNotContain(nonMatchingGlobalVessel, nonMatchingHiddenVessel, nonMatchingLocalVessel);
    }

    @Test
    public void should_expose_list_of_vessels_that_departed_recently_by_port() {
        throw new RuntimeException("TODO!");
    }

}