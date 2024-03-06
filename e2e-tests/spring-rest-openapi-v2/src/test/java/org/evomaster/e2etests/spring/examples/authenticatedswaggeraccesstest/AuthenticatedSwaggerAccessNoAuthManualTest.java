package org.evomaster.e2etests.spring.examples.authenticatedswaggeraccesstest;

import com.foo.rest.examples.spring.authenticatedswaggeraccessnoauth.AuthenticatedSwaggerAccessNoAuthController;
import org.evomaster.e2etests.spring.examples.SpringTestBase;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class AuthenticatedSwaggerAccessNoAuthManualTest extends SpringTestBase {

    @BeforeAll
    public static void initClass() throws Exception {

        SpringTestBase.initClass(new AuthenticatedSwaggerAccessNoAuthController());
    }

    /**
     * Authentication should throw exception since we donot have the right authentication object for accessing the
     * swagger.
     */
    @Test
    public void testAuthenticationThrowsException() {

        // Check that exception is thrown
        Assert.assertThrows(Exception.class, () ->
                runTestHandlingFlakyAndCompilation(
                        "UnauthenticatedSwaggerAccessNoAuthEM",
                        "org.bar.UnauthenticatedSwaggerAccessNoAuthEM",
                        1,
                        (args) -> {

                            initAndRun(args);

                        })
                );

        // Check that exception gives the right message
        try {

            runTestHandlingFlakyAndCompilation(
                    "UnauthenticatedSwaggerAccessNoAuthEM",
                    "org.bar.UnauthenticatedSwaggerAccessNoAuthEM",
                    1,
                    (args) -> {

                        initAndRun(args);

                    });
        }
        catch (Throwable throwable) {

            String errorMessage = ((InvocationTargetException) throwable).getTargetException().getMessage();

            Assert.assertTrue(errorMessage.contains("Cannot retrieve OpenAPI schema from http://localhost"));

        }


    }
}
