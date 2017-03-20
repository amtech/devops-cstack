/*
 * LICENCE : CloudUnit is available under the GNU Affero General Public License : https://gnu.org/licenses/agpl.html
 *     but CloudUnit is licensed too under a standard commercial license.
 *     Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 *     If you are not sure whether the GPL is right for you,
 *     you can always test our software under the GPL and inspect the source code before you contact us
 *     about purchasing a commercial license.
 *
 *     LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 *     or promote products derived from this project without prior written permission from Treeptik.
 *     Products or services derived from this software may not be called "CloudUnit"
 *     nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 *     For any questions, contact us : contact@treeptik.fr
 */
package fr.treeptik.cloudunit.cli.commands;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import fr.treeptik.cloudunit.cli.utils.ApplicationUtils;

@Component
public class ApplicationCommands implements CommandMarker {
	private static final String HELP_APPLICATION_NAME =
	        "Application name. Use list-apps to show all available apps on this account";

    private static final String HELP_SERVER_TYPE = "Server type: \n"
	        + " Available servers are the following:\n"
	        + " - Wildfly 8: --type wildfly-8\n"
	        + " - Apache Tomcat 6: --type tomcat-6\n"
	        + " - Apache Tomcat 7: --type tomcat-7\n"
	        + " - Apache Tomcat 8: --type tomcat-8";
	
    @Autowired
	private ApplicationUtils applicationUtils;

	@CliCommand(value = "informations", help = "Display informations about the current application")
	public String getApplication() {
		return applicationUtils.getInformations();
	}

	@CliCommand(value = "use", help = "Take control of an application")
	public String useApp(
	        @CliOption(key = {"","name"}, mandatory = true, help = HELP_APPLICATION_NAME) String name) {
		return applicationUtils.useApplication(name);
	}

	@CliCommand(value = "create-app", help = "Create an application")
	public String createApp(
	        @CliOption(key = "name", mandatory = true, help = "Application name") String name,
			@CliOption(key = "type", mandatory = true, help = HELP_SERVER_TYPE) String serverName) {
		return applicationUtils.createApp(name, serverName);
	}

	@CliCommand(value = "rm-app", help = "Remove an application")
	public String rmApp(
			@CliOption(key = "name", mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
			@CliOption(key = "errorIfNotExists", mandatory = false, help = "Throw an error if not exists",
			    specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") Boolean errorIfNotExists,
			@CliOption(key = "scriptUsage", mandatory = false, help = "Non-interactive mode",
			    specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") Boolean scriptUsage) {
		return applicationUtils.rmApp(applicationName, errorIfNotExists, scriptUsage ? null : new CliPrompter());
	}

	@CliCommand(value = "start", help = "Start the current application and all its services")
	public String startApp(
	        @CliOption(key = "name", mandatory = false, help = HELP_APPLICATION_NAME) String applicationName) {
		return applicationUtils.startApp(applicationName);
	}

	@CliCommand(value = "stop", help = "Stop the current application and all its services")
	public String stopApp(
	        @CliOption(key = "name", mandatory = false, help = HELP_APPLICATION_NAME) String applicationName) {
		return applicationUtils.stopApp(applicationName);
	}

	@CliCommand(value = "list-apps", help = "List all applications")
	public String list() {
		return applicationUtils.listAll();
	}

	@CliCommand(value = "deploy", help = "Deploy an archive ear/war on the app servers")
	public String deploy(
	        @CliOption(key = "path", mandatory = true, help = "Path of the archive file") File path,
			@CliOption(key = "openBrowser", mandatory = false, help = "Open a browser to location",
			    unspecifiedDefaultValue = "true") boolean openBrowser)
			throws URISyntaxException, MalformedURLException {
	    return applicationUtils.deployFromAWar(path, openBrowser);
	}

	@CliCommand(value = "list-aliases", help = "Display all application aliases")
	public String listAlias(
			@CliOption(key = { "", "name" }, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName) {
		return applicationUtils.listAllAliases(applicationName);
	}

	@CliCommand(value = "add-alias", help = "Add a new alias")
	public String addAlias(
			@CliOption(key = { "" }, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
			@CliOption(key = { "alias" }, mandatory = true, help = "Alias to access to your apps") String alias) {
		return applicationUtils.addNewAlias(applicationName, alias);
	}

	@CliCommand(value = "rm-alias", help = "Remove an existing alias")
	public String rmAlias(
			@CliOption(key = { "", "name" }, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
			@CliOption(key = { "alias" }, mandatory = true, help = "Alias to access to your apps") String alias) {
		return applicationUtils.removeAlias(applicationName, alias);
	}

	@CliCommand(value = "create-env-var", help = "Create a new environment variable")
	public String createEnvironmentVariable(
			@CliOption(key = {"name"}, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
			@CliOption(key = {"", "key"}, mandatory = true, help = "Key to the environment variable") String key,
			@CliOption(key = {"", "value"}, mandatory = true, help = "Value to the environment variable") String value) {
		return applicationUtils.createEnvironmentVariable(applicationName, key, value);
	}

    @CliCommand(value = "rm-env-var", help = "Remove an environment variable")
    public String removeEnvironmentVariable(
            @CliOption(key = {"name"}, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
            @CliOption(key = {"", "key"}, mandatory = true, help = "Key to the environment variable") String key) {
        return applicationUtils.removeEnvironmentVariable(applicationName, key);
    }

	@CliCommand(value = "set-env-var", help = "Set an existing environment variable")
	public String updateEnvironmentVariable(
			@CliOption(key = {"name"}, mandatory = false, help = HELP_APPLICATION_NAME) String applicationName,
			@CliOption(key = {"", "old-key"}, mandatory = true, help = "Old key to the environment variable") String oldKey,
			@CliOption(key = {"", "new-key"}, mandatory = true, help = "New key to the environment variable") String newKey,
			@CliOption(key = {"", "value"}, mandatory = true, help = "New value to the environment variable") String value) {
		return applicationUtils.updateEnvironmentVariable(applicationName, oldKey, newKey, value);
	}

    @CliCommand(value = "list-env-var", help = "List all environment variables")
    public String listEnvironmentVariables(
            @CliOption(key = "name", mandatory = false, help = HELP_APPLICATION_NAME)
            String applicationName,
            @CliOption(key = "export", mandatory = false, help = "Generate export script",
                    unspecifiedDefaultValue = "false", specifiedDefaultValue = "true")
            boolean export) {
        return applicationUtils.listAllEnvironmentVariables(applicationName, export);
    }

    @CliCommand(value = "list-containers", help = "List all containers")
	public String listContainers(
			@CliOption(key = "name", mandatory = false, help = HELP_APPLICATION_NAME) String applicationName) {
    	return applicationUtils.listContainers(applicationName);
	}
}
