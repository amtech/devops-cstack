package fr.treeptik.cloudunit.cli.integration.application;

import static fr.treeptik.cloudunit.cli.integration.ShellMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Ignore;

import fr.treeptik.cloudunit.cli.CloudUnitCliException;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;

import fr.treeptik.cloudunit.cli.integration.AbstractShellIntegrationTest;

/**
 * Created by guillaume on 16/10/15.
 */
public abstract class AbstractApplicationCommandsIT extends AbstractShellIntegrationTest {

    protected AbstractApplicationCommandsIT(String serverType) {
        super(serverType);
    }

    @Test
    public void test_shouldCreateApplication() {
        connect();
        try {
            CommandResult result = createApplication();
            
            try {
                assertThat(result, isSuccessfulCommand());
            } finally {
                removeCurrentApplication();
            }
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldNotCreateApplicationBecauseUserIsNotLogged() {
        CommandResult result = createApplication();
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("not connected"));
    }

    @Ignore
    @Test
    public void test_shouldNotCreateApplicationBecauseNameAlreadyInUse() {
        connect();
        createApplication();
        try {
            CommandResult result = createApplication();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("This application name already exists"));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Ignore
    @Test
    public void test_shouldNotCreateApplicationBecauseServerDoesNotExists() {
        connect();
        try {
            CommandResult result = createApplication(applicationName, "xxx");
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("No such image"));
        } finally {
            disconnect();
        }
    }

    @Ignore
    @Test
    public void test_shouldNotCreateApplicationNonAlphaNumericCharsBecauseApplicationAlreadyExists() {
        connect();
        createApplication(applicationName);
        try {
            CommandResult result = createApplication(applicationName+"&~~");
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("This application name already exists"));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldSelectAnApplication() {
        connect();
        createApplication();
        try {
            disconnect();
            connect();
            
            CommandResult result = useApplication(applicationName);
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            connect();
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotSelectAnApplicationBecauseUserIsNotLogged() {
        CommandResult result = useApplication(applicationName);
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("not connected"));
    }

    @Test
    public void test_shouldNotSelectAnApplicationBecauseDoesNotExist() {
        connect();
        try {
            CommandResult result = useApplication("zqdmokdzq");

            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("No such application"));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldStopAnApplication() {
        connect();
        createApplication();
        try {
            CommandResult result = getShell().executeCommand("stop");
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("stopped"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldStopAnApplicationWithArgs() {
        connect();
        createApplication();
        try {
            disconnect();
            connect();

            CommandResult result = stopApplication();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("stopped"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotStopAnApplicationBecauseUserIsNotLogged() {
        connect();
        createApplication();
        try {
            disconnect();

            CommandResult result = stopCurrentApplication();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("You are not connected"));
        } finally {
            connect();
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotStopAnApplicationBecauseNoApplicationSelected() {
        connect();
        createApplication();
        try {
            disconnect();
            connect();

            CommandResult result = stopCurrentApplication();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("not selected"));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotStopAnApplicationBecauseApplicationDoesNotExist() {
        connect();
        try {
            String name = "qmozkdmqozkd";
            CommandResult result = stopApplication(name);
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("No such application"));
            assertThat(result.getException().getMessage(), containsString(name));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldStartAnApplication() {
        connect();
        createApplication();
        try {
            stopCurrentApplication();

            CommandResult result = startCurrentApplication();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("started"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldStartAnApplicatioWithArgs() {
        connect();
        createApplication();
        stopCurrentApplication();
        try {
            CommandResult result = startApplication();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("started"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotStartAnApplicationBecauseUserIsNotLogged() {
        CommandResult result = startCurrentApplication();
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("You are not connected"));
    }


    @Test
    public void test_shouldNotStartAnApplicationBecauseNoApplicationSelected() {
        connect();
        try {
            CommandResult result = startCurrentApplication();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("not selected"));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldNotStartAnApplicationBecauseApplicationDoesNotExist() {
        connect();
        try {
            CommandResult result = getShell().executeCommand("start --name " + applicationName + "shadow");
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("No such application"));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldListApplications() {
        connect();
        createApplication();
        try {
            CommandResult result = listApplications();
            
            assertThat(result.getResult().toString(), containsString("found"));
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotListApplicationsBecauseUserIsNotLogged() {
        CommandResult result = listApplications();
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("not connected"));
    }

    @Test
    public void test_shouldDisplayApplicationInformation() {
        connect();
        createApplication();
        try {
            CommandResult result = information();
            
            assertThat(result, isSuccessfulCommand());
        } finally {
            removeApplication();
            disconnect();
        }
    }

    @Test
    public void test_shouldNotDisplayApplicationInformationBecauseUserIsNotLogged() {
        CommandResult result = information();
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("not connected"));
    }

    @Test
    public void test_shouldNotDisplayApplicationInformationBecauseNoApplicationSelected() {
        connect();
        try {
            CommandResult result = information();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("not selected"));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldRemoveApplicationWithArgs() {
        connect();
        createApplication();
        try {
            disconnect();
            connect();

            CommandResult result = removeApplication();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("removed"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldRemoveApplication() {
        connect();
        createApplication();
        try {
            CommandResult result = removeCurrentApplication();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("removed"));
            assertThat(result.getResult().toString(), containsString(applicationName.toLowerCase()));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldNotRemoveApplicationsBecauseUserIsNotLogged() {
        CommandResult result = removeCurrentApplication();
        
        assertThat(result, isFailedCommand());
        assertThat(result.getException().getMessage(), containsString("not connected"));
    }

    @Test
    public void test_shouldNotRemoveApplicationBecauseNoApplicationSelected() {
        connect();
        try {
            CommandResult result = removeCurrentApplication();
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException().getMessage(), containsString("not selected"));
        } finally {
            disconnect();
        }
    }
    
    @Test
    public void test_shouldNotRemoveApplicationButIgnoreError() {
        connect();
        try {
            CommandResult result = removeApplication("dzqmodzq", false);
            
            assertThat(result, isSuccessfulCommand());
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldNotRemoveApplicationWithArgsBecauseItDoesNotExist() {
        connect();
        try {
            CommandResult result = removeApplication("dzqmodzq", true);
            
            assertThat(result, isFailedCommand());
            assertThat(result.getException(), instanceOf(CloudUnitCliException.class));
            assertThat(result.getException().getMessage(), containsString("No such application"));
        } finally {
            disconnect();
        }
    }

    @Test
    public void test_shouldListContainers() {
        connect();
        createApplication();
        try {
            CommandResult result = listContainers();
            
            assertThat(result, isSuccessfulCommand());
            assertThat(result.getResult().toString(), containsString("found"));
        } finally {
            removeApplication();
            disconnect();
        }
    }
}
