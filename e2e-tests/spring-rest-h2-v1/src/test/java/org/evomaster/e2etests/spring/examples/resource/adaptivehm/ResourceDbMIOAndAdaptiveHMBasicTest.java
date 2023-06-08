package org.evomaster.e2etests.spring.examples.resource.adaptivehm;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.evomaster.core.problem.rest.RestIndividual;
import org.evomaster.core.problem.enterprise.SampleType;
import org.evomaster.core.problem.rest.resource.RestResourceCalls;
import org.evomaster.core.problem.rest.service.ResourceManageService;
import org.evomaster.core.problem.rest.service.ResourceRestMutator;
import org.evomaster.core.problem.rest.service.RestResourceFitness;
import org.evomaster.core.search.ActionFilter;
import org.evomaster.core.search.EvaluatedIndividual;
import org.evomaster.core.search.impact.impactinfocollection.ImpactsOfIndividual;
import org.evomaster.core.search.service.Archive;
import org.evomaster.e2etests.spring.examples.resource.ResourceMIOHWTestBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceDbMIOAndAdaptiveHMBasicTest extends ResourceMIOHWTestBase {

    @Test
    public void testResourceWithSQLAndAHW(){
        List<String> args = generalArgs(6, 42);
        hypmutation(args, true);
        adaptiveMutation(args, 0.5);
        defaultResourceConfig(args, true);
        //always employ SQL to create POST
        args.add("--probOfApplySQLActionToCreateResources");
        args.add("1.0");
        args.add("--structureMutationProbability");
        args.add("0.0");

        //test impactinfo
        Injector injector = init(args);

        ResourceManageService rmanger = injector.getInstance(ResourceManageService.class);
        ResourceRestMutator mutator = injector.getInstance(ResourceRestMutator.class);
        RestResourceFitness ff = injector.getInstance(RestResourceFitness.class);
        Archive<RestIndividual> archive = injector.getInstance(Key.get(
                new TypeLiteral<Archive<RestIndividual>>() {}));

        List<RestResourceCalls> calls = new ArrayList<>();


        String raIdkey = "/api/rA/{rAId}";
        String rdkey = "/api/rd";

        rmanger.sampleCall(raIdkey, true, calls, 10, false, Collections.emptyList(), "POST-GET");
        rmanger.sampleCall(rdkey, true, calls, 8, false, Collections.emptyList(), "POST-POST");


        RestIndividual twoCalls = new RestIndividual(calls, SampleType.SMART_RESOURCE, null, Collections.emptyList(), null, 1);
        twoCalls.doInitializeLocalId();
        EvaluatedIndividual<RestIndividual> twoCallsEval = ff.calculateCoverage(twoCalls, Collections.emptySet());

        assertNotNull(twoCallsEval);
        ImpactsOfIndividual impactInd = twoCallsEval.getImpactInfo();
        // impactinfo should be initialized
        assertNotNull(impactInd);
        assertEquals(0, impactInd.getSizeOfActionImpacts(true));
        assertEquals(5, impactInd.getSizeOfActionImpacts(false));
        assertEquals(twoCalls.seeActions(ActionFilter.NO_INIT).size(), impactInd.getSizeOfActionImpacts(false));
        //tracking is null if the eval is generated by sampler
        assertNull(twoCallsEval.getTracking());

        EvaluatedIndividual<RestIndividual> twoCallsEvalNoWorse = mutator.mutateAndSave(1, twoCallsEval, archive);
        //history should affect both of evaluated individual
        assertNotNull(twoCallsEval.getTracking());
        assertNotNull(twoCallsEvalNoWorse.getTracking());
        assertEquals(2, twoCallsEval.getTracking().getHistory().size());
        assertEquals(2, twoCallsEvalNoWorse.getTracking().getHistory().size());
        //this should be determinate with a specific seed
        assertNotNull(twoCallsEvalNoWorse.getByIndex(twoCallsEvalNoWorse.getIndex()));
        assert(twoCallsEvalNoWorse.getByIndex(twoCallsEvalNoWorse.getIndex()).getEvaluatedResult().isImpactful());
    }

}
