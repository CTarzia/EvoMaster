using System;
using System.Collections.Generic;
using Controller.Api;

namespace Controller {
  public interface ISutHandler {

    /**
     * There might be different settings based on when the SUT is run during the
     * search of EvoMaster, and when it is later started in the generated tests.
     */
    void SetupForGeneratedTest () { }

    /**
     * <p>
     * Start a new instance of the SUT.
     * </p>
     *
     * <p>
     * This method must be blocking until the SUT is initialized.
     *</p>
     *
     * <p>
     * How this method is implemented depends on the library/framework in which
     * the application is written.
     * For example, in Spring applications you can use something like:
     * {@code SpringApplication.run()}
     * </p>
     *
     *
     * @return the base URL of the running SUT, eg "http://localhost:8080"
     */
    string StartSut ();

    /**
     * <p>
     * Stop the SUT.
     * </p>
     *
     * <p>
     * How to implement this method depends on the library/framework in which
     * the application is written.
     * For example, in Spring applications you can save in a variable the {@code ConfigurableApplicationContext}
     * returned when starting the application, and then call {@code stop()} on it here.
     * </p>
     */
    void StopSut ();

    /**
     * <p>
     * Make sure the SUT is in a clean state (eg, reset data in database).
     * </p>
     *
     * <p>
     * A possible (likely very inefficient) way to implement this would be to
     * call {@code stopSUT} followed by {@code startSUT}.
     * </p>
     *
     * <p>
     * When dealing with databases, you can look at the utility functions from
     * the class {@link DbCleaner}.
     * How to access the database depends on the application.
     * To access a {@code java.sql.Connection}, in Spring applications you can use something like:
     * {@code ctx.getBean(JdbcTemplate.class).getDataSource().getConnection()}.
     * </p>
     */
    void ResetStateOfSUT ();

    /**
     * Execute the given data insertions into the database (if any)
     *
     * @param insertions DTOs for each insertion to execute
     */
    void ExecInsertionsIntoDatabase (IList<InsertionDto> insertions);
  }

}