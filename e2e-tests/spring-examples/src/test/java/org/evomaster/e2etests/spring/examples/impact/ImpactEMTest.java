package org.evomaster.e2etests.spring.examples.impact;

import com.foo.rest.examples.spring.impact.ImpactRestController;
import org.evomaster.core.problem.rest.RestIndividual;
import org.evomaster.core.problem.rest.util.ParamUtil;
import org.evomaster.core.search.EvaluatedIndividual;
import org.evomaster.core.search.Individual;
import org.evomaster.core.search.Solution;
import org.evomaster.core.search.gene.Gene;
import org.evomaster.core.search.impact.GeneImpact;
import org.evomaster.core.search.impact.ImpactMutationSelection;
import org.evomaster.core.search.impact.ImpactUtils;
import org.evomaster.e2etests.spring.examples.SpringTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * created by manzh on 2019-09-12
 */
public class ImpactEMTest extends SpringTestBase {

    @Test
    public void testAwayBad() throws Throwable {
        testRunEM(ImpactMutationSelection.AWAY_NOIMPACT);
    }

    public void testRunEM(ImpactMutationSelection method) throws Throwable {

        runTestHandlingFlakyAndCompilation(
                "",
                "",
                2_000,
                false,
                (args) -> {

                    args.add("--probOfArchiveMutation");
                    args.add("1.0");

                    args.add("--geneSelectionMethod");
                    args.add(method.toString());

                    args.add("--enableTrackEvaluatedIndividual");
                    args.add("true");

                    Solution<RestIndividual> solution = initAndRun(args);

                    assertTrue(solution.getIndividuals().size() >= 1);

                    boolean impactInfoCollected = solution.getIndividuals().stream().allMatch(
                            s -> s.getImpactOfGenes().size() > 0 && checkNoImpact("noimpactIntField", s)
                    );

                    assertTrue(impactInfoCollected);

                });
    }

    private String getGeneIdByName(String geneName, EvaluatedIndividual<RestIndividual> ind){

        Gene gene = ind.getIndividual().seeGenes(Individual.GeneFilter.NO_SQL).stream().filter(g -> ParamUtil.Companion.getValueGene(g).getName().equals(geneName))
                .findAny()
                .orElse(null);

        assertNotNull(gene);

        return ImpactUtils.Companion.generateGeneId(ind.getIndividual(), gene);
    }

    private boolean checkNoImpact(String geneName, EvaluatedIndividual<RestIndividual> ind){

        if (ind.getImpactOfGenes().values().stream().map(s -> ((GeneImpact) s).getTimesToManipulate()).mapToInt(Integer::intValue).sum() == 0 ) return true;

        String id = getGeneIdByName(geneName, ind);

        boolean last = true;

        GeneImpact noimpactGene = ind.getImpactOfGenes().get(id);
        for (String keyId : ind.getImpactOfGenes().keySet()){
            if (keyId != id){
                last = last &&
                        // getTimesOfImpact should be less than any others OR getTimesOfNoImpact should be more than any others
                        (noimpactGene.getTimesOfImpact() <= ind.getImpactOfGenes().get(keyId).getTimesOfImpact()
                                || noimpactGene.getTimesOfNoImpacts() >= ind.getImpactOfGenes().get(keyId).getTimesOfNoImpacts())
                        &&
                        // getTimesToManipulate should be less than any others
                        (noimpactGene.getTimesToManipulate() <= ind.getImpactOfGenes().get(keyId).getTimesToManipulate());
            }
        }
        return last;
    }

    @BeforeAll
    public static void initClass() throws Exception {
        SpringTestBase.initClass(new ImpactRestController(Arrays.asList("/api/intImpact")));
    }

}
