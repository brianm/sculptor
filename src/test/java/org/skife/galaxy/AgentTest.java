package org.skife.galaxy;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.galaxy.agent.Agent;
import org.skife.galaxy.agent.Deployment;
import org.skife.galaxy.agent.Slot;
import org.skife.galaxy.agent.Status;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.skife.galaxy.TestingHelpers.isExistingFile;
import static org.skife.galaxy.TestingHelpers.file;


public class AgentTest
{
    private File       tempDir;
    private Agent      agent;
    private Deployment d;

    @Before
    public void setUp() throws Exception
    {
        tempDir = Files.createTempDir();
        agent = new Agent(tempDir);

        File tarball = new File("src/test/resources/echo.tar.gz");
        d = new Deployment("test", tarball.toURI(), Collections.<String, URI>emptyMap());
    }

    @After
    public void tearDown() throws Exception
    {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testDeploy() throws Exception
    {
        Slot s = agent.deploy(d);
        File deploy_dir = file(s.getRoot(), "deploy");
        assertThat(deploy_dir, isExistingFile());
        assertThat(file(deploy_dir, "bin"), isExistingFile());
        assertThat(file(deploy_dir, "bin", "control"), isExistingFile());
    }

    @Test
    public void testStart() throws Exception
    {
        Slot s = agent.deploy(d);
        s.start();
        assertThat(file(s.getRoot(), "deploy", "running"), isExistingFile());
    }

    @Test
    public void testStop() throws Exception
    {
        Slot s = agent.deploy(d);
        s.start();
        s.stop();
        assertThat(file(s.getRoot(), "deploy", "running"), not(isExistingFile()));
    }

    @Test
    public void testStatusOnRunning() throws Exception
    {
        Slot s = agent.deploy(d);
        s.start();
        Status status = s.status();
        assertThat(status, equalTo(Status.success("running")));
    }

    @Test
    public void testStatusOnStopped() throws Exception
    {
        Slot s = agent.deploy(d);
        s.start();
        s.stop();
        Status status = s.status();
        assertThat(status, equalTo(Status.success("stopped")));
    }

    @Test
    public void testEnvConfig() throws Exception
    {
        File cfg = new File("src/test/resources/some_config.properties");
        agent.addEnvironmentConfiguration("/env/runtime.properties", cfg.toURI());

        Slot s = agent.deploy(d);
        File deployed = file(s.getDeployDir(), "env", "runtime.properties");
        assertThat(deployed, isExistingFile());
    }

    @Test
    public void testDeploymentConfig() throws Exception
    {
        File cfg = new File("src/test/resources/some_config.properties");
        agent.addEnvironmentConfiguration("/env/runtime.properties", cfg.toURI());

        File tarball = new File("src/test/resources/echo.tar.gz");
        Deployment d2 = new Deployment("test", tarball.toURI(), ImmutableMap.of("/env/deploy.conf",
                                                                                cfg.toURI()));
        Slot s = agent.deploy(d2);
        File dep_conf = file(s.getDeployDir(), "env", "deploy.conf");

        assertThat(dep_conf, isExistingFile());
    }

}
