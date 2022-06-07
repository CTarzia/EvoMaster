package org.evomaster.e2etests.spring.examples.wiremock.service;

import com.foo.rest.examples.spring.wiremock.service.ServiceController;
import com.google.inject.Injector;
import org.evomaster.core.EMConfig;
import org.evomaster.core.problem.external.service.ExternalServiceInfo;
import org.evomaster.core.problem.external.service.ExternalServices;
import org.evomaster.core.problem.rest.RestIndividual;
import org.evomaster.core.problem.rest.service.ResourceSampler;
import org.evomaster.core.problem.rest.service.RestResourceFitness;
import org.evomaster.e2etests.spring.examples.SpringTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalServiceSpoofingEMTest extends SpringTestBase {

    @BeforeAll
    public static void initClass() throws Exception {
        ServiceController serviceController = new ServiceController();
        EMConfig config = new EMConfig();
        config.setInstrumentMR_NET(true);
        SpringTestBase.initClass(serviceController,config);
    }

    /**
     * The test is disabled since, encountered some errors within the
     * existing e2e tests. Will be completed once those sorted.
     */
    @Test
    public void externalServiceSuccessTest() throws Throwable {
        String[] args = new String[]{
                "--createTests", "false",
                "--seed", "42",
                "--sutControllerPort", "" + controllerPort,
                "--maxActionEvaluations", "1",
                "--stoppingCriterion", "FITNESS_EVALUATIONS",
                "--executiveSummary", "false",
                "--expectationsActive" , "true",
                "--outputFormat", "JAVA_JUNIT_5",
                "--outputFolder", "target/em-tests/ExternalServiceEM",
                "--externalServiceIPSelectionStrategy", "USER",
                "--externalServiceIP", "127.0.0.20"
        };

        Injector injector = init(Arrays.asList(args));

        ExternalServices externalServices = injector.getInstance(ExternalServices.class);

        RestResourceFitness restResourceFitness = injector.getInstance(RestResourceFitness.class);
        ResourceSampler resourceSampler = injector.getInstance(ResourceSampler.class);
        RestIndividual restIndividual = resourceSampler.sample();

        // asserts whether the call made during the start-up is captured
        assertEquals(1, externalServices.getExternalServices().size(), externalServices.getExternalServices().stream().map(ExternalServiceInfo::getRemoteHostname).collect(Collectors.joining(",")));
        assertEquals("baz.bar", externalServices.getExternalServices().get(0).getRemoteHostname());
        restResourceFitness.calculateCoverage(restIndividual, Collections.emptySet());

        // assertion after the execution
        assertEquals(2, externalServices.getExternalServices().size());

    }
}
