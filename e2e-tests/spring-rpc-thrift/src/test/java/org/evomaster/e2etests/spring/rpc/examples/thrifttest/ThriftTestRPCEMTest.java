package org.evomaster.e2etests.spring.rpc.examples.thrifttest;

import com.foo.rpc.examples.spring.thrifttest.ThriftTestRPCController;
import org.evomaster.core.problem.rpc.RPCIndividual;
import org.evomaster.core.search.Solution;
import org.evomaster.e2etests.spring.rpc.examples.SpringRPCTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThriftTestRPCEMTest extends SpringRPCTestBase {

    @BeforeAll
    public static void initClass() throws Exception {

        ThriftTestRPCController controller = new ThriftTestRPCController();
        SpringRPCTestBase.initClass(controller);
    }

    @Test
    public void testRunEM() throws Throwable {

        runTestHandlingFlakyAndCompilation(
                "ThriftTestRPCEM",
                "org.foo.ThriftTestRPCEM",
                5000,
                (args) -> {

                    Solution<RPCIndividual> solution = initAndRun(args);

                    assertTrue(solution.getIndividuals().size() >= 1);

                });
    }
}
