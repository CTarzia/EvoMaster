package org.evomaster.client.java.controller;

import org.evomaster.client.java.controller.api.dto.ActionDto;
import org.evomaster.client.java.controller.api.dto.BootTimeInfoDto;
import org.evomaster.client.java.controller.api.dto.UnitsInfoDto;
import org.evomaster.client.java.instrumentation.*;
import org.evomaster.client.java.instrumentation.staticstate.ExecutionTracer;
import org.evomaster.client.java.utils.SimpleLogger;
import org.evomaster.client.java.controller.internal.SutController;
import org.evomaster.client.java.instrumentation.external.JarAgentLocator;
import org.evomaster.client.java.instrumentation.external.ServerController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ExternalSutController extends SutController {

    /**
     * System property to avoid printing the console output of the SUT.
     */
    public static final String PROP_MUTE_SUT = "em.muteSUT";

    protected volatile Process process;

    private volatile boolean instrumentation;
    private volatile Thread processKillHook;
    private volatile Thread outputPrinter;
    private volatile CountDownLatch latch;
    private volatile ServerController serverController;
    private volatile boolean initialized;

    /*
        If SUT output is mutated, but SUT fails to start, we
        still want to print it for debugging
     */
    private volatile StringBuffer errorBuffer;

    /**
     * Command used to run java, when starting the SUT.
     * This might need to be overridden whea dealing with experiments
     * using different versions of Java (eg, 8 vs 11)
     */
    private volatile String javaCommand = "java";

    /**
     * Path on filesystem of where JaCoCo jar file is located
     */
    private volatile String jaCoCoLocation = "";

    /**
     * Destination file for JaCoCo
     */
    private volatile String jaCoCoOutputFile = "";


    public final ExternalSutController setJaCoCo(String jaCoCoLocation, String jaCoCoOutputFile){
        this.jaCoCoLocation = jaCoCoLocation;
        this.jaCoCoOutputFile = jaCoCoOutputFile;
        return this;
    }

    public int getWaitingSecondsForIncomingConnection() {
        return 20_000;
    }

    @Override
    public final void setupForGeneratedTest(){
        //In the past, we configured P6Spy here
    }

    public final void setInstrumentation(boolean instrumentation) {
        this.instrumentation = instrumentation;
    }

    /**
     * @return the input parameters with which the system under test
     * should be started
     */
    public abstract String[] getInputParameters();

    /**
     * @return the JVM parameters (eg -X and -D) with which the system
     * under test should be started
     */
    public abstract String[] getJVMParameters();

    /**
     * @return the base URL of the running SUT, eg "http://localhost:8080".
     * Note: this value will likely depend on how getInputParameters() has
     * been implemented
     */
    public abstract String getBaseURL();


    /**
     * @return a String representing either a relative or absolute path
     * to the where the JAR of the system under test is located
     */
    public abstract String getPathToExecutableJar();


    /**
     * @return a string subtext that should be present in the logs (std output)
     * of the system under test to check if the server is up and ready.
     */
    public abstract String getLogMessageOfInitializedServer();

    /**
     * @return how long (in seconds) we should wait at most to check if SUT is ready
     * and initialized (this related to the getLogMessageOfInitializedServer() method)
     */
    public abstract long getMaxAwaitForInitializationInSeconds();

    /**
     * If the SUT needs some third-party processes (eg a non-embedded database),
     * here they can be configured and started.
     * This method is going to be called before we start the SUT.
     */
    public abstract void preStart();


    /**
     * This method is going to be called after the SUT is started.
     */
    public abstract void postStart();


    /**
     * This method is going to be called before the SUT is stopped.
     */
    public abstract void preStop();


    /**
     * If the SUT needs some third-party processes (eg a non-embedded database),
     * here we can shut them down once the SUT has been stopped.
     */
    public abstract void postStop();

    //-------------------------------------------------------------

    public final void setJavaCommand(String command){
        if(command==null || command.isEmpty()){
            throw new IllegalArgumentException("Empty java command");
        }
        javaCommand = command;
    }

    @Override
    public final String startSut() {

        SimpleLogger.info("Going to start the SUT");

        initialized = false;

        validateJarPath();

        preStart();

        /*
            the following thread is important to make sure that the external process is killed
            when current process ends
        */
        processKillHook = new Thread(() -> killProcess());
        Runtime.getRuntime().addShutdownHook(processKillHook);

        //we need a mechanism to wait until the SUT is ready
        latch = new CountDownLatch(1);


        List<String> command = new ArrayList<>();
        command.add(javaCommand);


        if (instrumentation) {
            if (serverController == null) {
                serverController = new ServerController();
            }
            int port = serverController.startServer();
            command.add("-D" + InputProperties.EXTERNAL_PORT_PROP + "=" + port);

            //this should had been setup in EMController
            String categories = System.getProperty(InputProperties.REPLACEMENT_CATEGORIES);
            if(categories!=null && !categories.isEmpty()) {
                command.add("-D"+InputProperties.REPLACEMENT_CATEGORIES+"="+categories);
            }

            String jarPath = JarAgentLocator.getAgentJarPath();
            if (jarPath == null) {
                throw new IllegalStateException("Cannot locate JAR file with EvoMaster Java Agent");
            }
            command.add("-javaagent:" + jarPath + "=" + getPackagePrefixesToCover());
        }

        for (String s : getJVMParameters()) {
            if (s != null) {
                String token = s.trim();
                if (!token.isEmpty()) {
                    command.add(token);
                }
            }
        }

        if (command.stream().noneMatch(s -> s.startsWith("-Xmx"))) {
            command.add("-Xmx2G");
        }
        if (command.stream().noneMatch(s -> s.startsWith("-Xms"))) {
            command.add("-Xms1G");
        }

        if(!jaCoCoLocation.isEmpty() && !jaCoCoOutputFile.isEmpty()){
            command.add("-javaagent:"+jaCoCoLocation+"=destfile="+jaCoCoOutputFile+",append=false,dumponexit=true");
        }

        command.add("-jar");
        command.add(getPathToExecutableJar());

        for (String s : getInputParameters()) {
            if (s != null) {
                String token = s.trim();
                if (!token.isEmpty()) {
                    command.add(token);
                }
            }
        }

        SimpleLogger.info("Going to start SUT with command:\n" + String.join(" ", command));

        // now start the process
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        try {
            process = builder.start();
        } catch (IOException e) {
            SimpleLogger.error("Failed to start external process", e);
            return null;
        }

        //this is not only needed for debugging, but also to check for when SUT is ready
        startExternalProcessPrinter();

        if (instrumentation && serverController != null) {
            boolean connected = serverController.waitForIncomingConnection(getWaitingSecondsForIncomingConnection());
            if (!connected) {
                SimpleLogger.error("Could not establish connection to retrieve code metrics");
                if(errorBuffer != null) {
                    SimpleLogger.error("SUT output:\n" + errorBuffer.toString());
                }
                stopSut();
                return null;
            }
        }

        //need to block until server is ready
        long timeout = getMaxAwaitForInitializationInSeconds();
        boolean completed;

        try {
            completed = latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            SimpleLogger.error("Interrupted controller");
            stopSut();
            return null;
        }

        if(! completed){
            SimpleLogger.error("SUT has not started properly within " + timeout + " seconds");
            if(errorBuffer != null) {
                SimpleLogger.error("SUT output:\n" + errorBuffer.toString());
            }
            stopSut();
            return null;
        }

        if (!isSutRunning()) {
            SimpleLogger.error("SUT started but then terminated. Likely a possible misconfiguration");
            if(errorBuffer != null) {
                SimpleLogger.error("SUT output:\n" + errorBuffer.toString());
            }
            //note: actual process might still be running due to Java Agent we started
            stopSut();
            return null;
        }

        if (!initialized) {
            //this could happen if SUT is hanging for some reason
            SimpleLogger.error("SUT is started but not initialized");
            if(errorBuffer != null) {
                SimpleLogger.error("SUT output:\n" + errorBuffer.toString());
            }
            //note: actual process might still be running due to Java Agent we started
            stopSut();
            return null;
        }

        postStart();

        return getBaseURL();
    }

    @Override
    public final boolean isSutRunning() {
        return process != null && process.isAlive();
    }


    @Override
    public final void stopSut() {

        SimpleLogger.info("Going to stop the SUT");

        preStop();

        if (serverController != null) {
            serverController.closeServer();
        }
        killProcess();
        initialized = false;

        postStop();
    }

    @Override
    public final boolean isInstrumentationActivated() {
        return instrumentation && serverController != null && serverController.isConnectionOn();
    }

    @Override
    public final void newSearch() {
        if (isInstrumentationActivated()) {
            serverController.resetForNewSearch();
        }
    }

    @Override
    public final void newTestSpecificHandler() {
        if (isInstrumentationActivated()) {
            serverController.resetForNewTest();
        }
    }

    @Override
    public final List<TargetInfo> getTargetInfos(Collection<Integer> ids) {
        checkInstrumentation();
        return serverController.getTargetsInfo(ids);
    }

    @Override
    public final List<AdditionalInfo> getAdditionalInfoList(){
        checkInstrumentation();
        return serverController.getAdditionalInfoList();
    }

    @Override
    public BootTimeInfoDto getBootTimeInfoDto() {
        if(!isInstrumentationActivated()){
            return null;
        }
        return getBootTimeInfoDto(serverController.handleBootTimeObjectiveInfo());
    }

    @Override
    public final void newActionSpecificHandler(ActionDto dto) {
        if (isInstrumentationActivated()) {
            serverController.setAction(new Action(dto.index, dto.inputVariables));
        }
    }

    @Override
    public final UnitsInfoDto getUnitsInfoDto(){

        if(!isInstrumentationActivated()){
            return null;
        }

        return getUnitsInfoDto(serverController.getUnitsInfoRecorder());
    }

    @Override
    public final void setKillSwitch(boolean b) {
        checkInstrumentation();

        serverController.setKillSwitch(b);
        ExecutionTracer.setKillSwitch(b);// store info locally as well, to avoid needing to do call to fetch current value
    }

    @Override
    public final void setExecutingInitSql(boolean executingInitSql) {
        checkInstrumentation();
        serverController.setExecutingInitSql(executingInitSql);
        // sync executingInitSql on the local ExecutionTracer
        ExecutionTracer.setExecutingInitSql(executingInitSql);
    }

    @Override
    public final void setExecutingAction(boolean executingAction){
        checkInstrumentation();
        serverController.setExecutingAction(executingAction);
        // sync executingAction on the local ExecutionTracer
        ExecutionTracer.setExecutingAction(executingAction);
    }

    @Override
    public final String getExecutableFullPath(){
        validateJarPath();

        //this might be relative
        String path = getPathToExecutableJar();

        return Paths.get(path).toAbsolutePath().toString();
    }

    //-----------------------------------------

    private void checkInstrumentation() {
        if (!isInstrumentationActivated()) {
            throw new IllegalStateException("Instrumentation is not active");
        }
    }

    private void validateJarPath() {

        String path = getPathToExecutableJar();
        if (!path.endsWith(".jar")) {
            throw new IllegalStateException("Invalid jar path does not end with '.jar': " + path);
        }

        if (!Files.exists(Paths.get(path))) {
            throw new IllegalArgumentException("File does not exist: " + path);
        }
    }

    private void killProcess() {
        try {
            Runtime.getRuntime().removeShutdownHook(processKillHook);
        } catch (Exception e) {
            /* do nothing. this can happen if shutdown is in progress */
        }

        if (process != null) {
            process.destroy();
            try {
                //be sure streamers are closed, otherwise process might hang on Windows
                process.getOutputStream().close();
                process.getInputStream().close();
                process.getErrorStream().close();
            } catch (Exception t) {
                SimpleLogger.error("Failed to close process stream: " + t.toString());
            }

            process = null;
        }
    }

    protected void startExternalProcessPrinter() {

        if (outputPrinter == null || !outputPrinter.isAlive()) {
            outputPrinter = new Thread(() -> {

                try {

                    boolean muted = Boolean.parseBoolean(System.getProperty(PROP_MUTE_SUT));

                    if(muted){
                        errorBuffer = new StringBuffer(4096);
                    }

                    Scanner scanner = new Scanner(new BufferedReader(
                            new InputStreamReader(process.getInputStream())));

                    while (scanner.hasNextLine()) {

                        String line = scanner.nextLine();

                        if(!muted) {
                            SimpleLogger.info("SUT: " + line);
                        } else if(errorBuffer != null){
                            errorBuffer.append(line);
                            errorBuffer.append("\n");
                        }

                        if (line.contains(getLogMessageOfInitializedServer())) {
                            initialized = true;
                            errorBuffer = null; //no need to keep track of it if everything is ok
                            latch.countDown();
                        }
                    }

                    /*
                        if we arrive here, it means the process has no more output.
                        this could happen if it was started with some misconfiguration, or
                        if it has been stopped
                     */
                    if (process == null) {
                        SimpleLogger.warn("SUT was manually terminated ('process' reference is null)");
                    } else if(!initialized) {
                        if (!process.isAlive()) {
                            SimpleLogger.warn("SUT was terminated before initialization. Exit code: " + process.exitValue());
                        } else {
                            SimpleLogger.warn("SUT is still alive, but its output was closed before" +
                                    " producing the initialization message.");
                        }
                    } else {
                        SimpleLogger.info("Process output has been closed");
                    }

                    latch.countDown();

                } catch (Exception e) {
                    SimpleLogger.error("Failed to handle external process printer", e);
                }
            });

            outputPrinter.start();
        }
    }
}
