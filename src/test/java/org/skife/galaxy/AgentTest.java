package org.skife.galaxy;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.skife.galaxy.TestingHelpers.exists;
import static org.skife.galaxy.TestingHelpers.file;


public class AgentTest
{
    private File  tempDir;
    private Slot  s;
    private Agent agent;

    @Before
    public void setUp() throws Exception
    {
        tempDir = Files.createTempDir();
        agent = new Agent(tempDir);

        File tarball = new File("src/test/resources/echo.tar.gz");
        Deployment d = new Deployment("test", tarball.toURI().toURL());

        s = agent.deploy(d);

    }

    @After
    public void tearDown() throws Exception
    {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testDeploy() throws Exception
    {
        File deploy_dir = file(s.getRoot(), "deploy");
        assertThat(deploy_dir, exists());
        assertThat(file(deploy_dir, "bin"), exists());
        assertThat(file(deploy_dir, "bin", "control"), exists());
    }

    @Test
    public void testStart() throws Exception
    {
        s.start();
        assertThat(file(s.getRoot(), "deploy", "running"), exists());
    }

    @Test
    public void testStop() throws Exception
    {
        s.start();
        s.stop();
        assertThat(file(s.getRoot(), "deploy", "running"), not(exists()));
    }

    @Test
    public void testStatusOnRunning() throws Exception
    {
        s.start();
        Status status = s.status();
        assertThat(status, equalTo(Status.success("running")));
    }

    @Test
    public void testStatusOnStopped() throws Exception
    {
        s.start();
        s.stop();
        Status status = s.status();
        assertThat(status, equalTo(Status.success("stopped")));
    }

}
