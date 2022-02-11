package org.evomaster.client.java.controller;

import org.evomaster.client.java.controller.api.dto.database.operations.InsertionDto;
import org.evomaster.client.java.controller.api.dto.database.operations.InsertionResultsDto;
import org.evomaster.client.java.controller.db.DbCleaner;
import org.evomaster.client.java.controller.db.SqlScriptRunner;
import org.evomaster.client.java.controller.internal.db.DbSpecification;

import java.sql.SQLException;
import java.util.List;

/**
 * Base interface used to control the System Under Test (SUT)
 * from the generated tests.
 * Needed base functionalities are for example, starting/stopping
 * the SUT, and reset its state.
 */
public interface SutHandler {

    /**
     * There might be different settings based on when the SUT is run during the
     * search of EvoMaster, and when it is later started in the generated tests.
     */
    default void setupForGeneratedTest(){}

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
    String startSut();

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
    void stopSut();

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
    void resetStateOfSUT();

    /**
     * Execute the given data insertions into the database (if any)
     *
     * @param insertions DTOs for each insertion to execute
     * @param previous an array of insertion results which were executed before this execution
     * @return insertion execution results
     */
    InsertionResultsDto execInsertionsIntoDatabase(List<InsertionDto> insertions, InsertionResultsDto... previous);


    /**
     * <p>
     * return an instance of a client of an RPC service.
     * </p>
     *
     * <p>
     * This method must be blocking until the SUT is initialized.
     * </p>
     *
     * <p>
     * This method is only required when the problem is RPC for the moment,
     * otherwise return null
     * </p>
     *
     * might change string interfaceName to class interface
     *
     * @param interfaceName a full name of an interface
     * @return a client which could send requests to the interface
     */
    default Object getRPCClient(String interfaceName){return null;}

    /**
     * <p>
     * execute an RPC endpoint with evomaster driver
     * </p>
     *
     *
     * @param json contains info of an RPC endpoint
     * @return value returned by this execution. it is nullable.
     */
    default Object executeRPCEndpoint(String json) throws Exception {return null;}

    /**
     * <p>
     * execute an RPC endpoint with evomaster driver
     * </p>
     *
     * TODO remove this later if we do not use test generation with driver
     */
    default void extractRPCSchema(){}


    /**
     * <p>
     *     authentication setup might be handled locally.
     *     then we provide this interface to define it.
     * </p>
     *
     * @param authenticationInfo info for the authentication setup
     * @return if the authentication is set up successfully
     */
    default boolean handleLocalAuthenticationSetup(String authenticationInfo){return true;}

    /**
     * <p>
     * If the system under test (SUT) uses a SQL database, we need to have a
     * configured DbSpecification to access/reset it.
     * </p>
     *
     * <p>
     * When accessing a {@code Connection} object to reset the state of
     * the application, we suggest to save it to field (eg when starting the
     * application), and set such field with {@link DbSpecification#connection}.
     * This connection object will be used by EvoMaster to analyze the state of
     * the database to create better test cases.
     * </p>
     *
     * <p>
     * To handle db in the context of testing, there might be a need to initialize
     * data into database with a sql script. such info could be specified with
     * {@link DbSpecification#dbType}
     * </p>
     *
     * <p>
     * With EvoMaster, we also support a smart DB cleaner by removing all data in tables
     * which has been accessed after each test. In order to achieve this, we requires user
     * to set a set of info such as database type with {@link DbSpecification#dbType},
     * schema name with {@link DbSpecification#schemaName} (TODO might remove later).
     * In addition, we also provide an option (default is {@code true}) to configure
     * if such cleaner is preferred with {@link DbSpecification#employSmartDbClean}.
     * </p>
     *
     * @return {@code null} if the SUT does not use any SQL database
     */

    DbSpecification setDbSpecification();


    /**
     * <p>
     * reset database if the smart db cleaning is employed
     * </p>
     */
    default void resetDatabase(){
        DbSpecification spec = setDbSpecification();
        if (spec==null || spec.connection == null || !spec.employSmartDbClean){
            return;
        }
        DbCleaner.clearDatabase(spec.connection, spec.schemaName, null, null, spec.dbType);
        if (spec.initSqlScript != null) {
            try {
                SqlScriptRunner.execScript(spec.connection, spec.initSqlScript);
            } catch (SQLException e) {
                throw new RuntimeException("Fail to execute the specified initSqlScript "+e);
            }
        }
    }


}
