/*
 * LICENCE : CloudUnit is available under the GNU Affero General Public License : https://gnu.org/licenses/agpl.html
 * but CloudUnit is licensed too under a standard commercial license.
 * Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 * If you are not sure whether the AGPL is right for you,
 * you can always test our software under the AGPL and inspect the source code before you contact us
 * about purchasing a commercial license.
 *
 * LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 * or promote products derived from this project without prior written permission from Treeptik.
 * Products or services derived from this software may not be called "CloudUnit"
 * nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 * For any questions, contact us : contact@treeptik.fr
 */

package fr.treeptik.cloudunit.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.treeptik.cloudunit.dto.*;
import fr.treeptik.cloudunit.model.PortToOpen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fr.treeptik.cloudunit.aspects.CloudUnitSecurable;
import fr.treeptik.cloudunit.config.events.ApplicationFailEvent;
import fr.treeptik.cloudunit.config.events.ApplicationPendingEvent;
import fr.treeptik.cloudunit.config.events.ApplicationStartEvent;
import fr.treeptik.cloudunit.config.events.ApplicationStopEvent;
import fr.treeptik.cloudunit.enums.RemoteExecAction;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.FatalDockerJSONException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.factory.EnvUnitFactory;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Status;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.service.DockerService;
import fr.treeptik.cloudunit.utils.AuthentificationUtils;
import fr.treeptik.cloudunit.utils.CheckUtils;

/**
 * Controller about Application lifecycle Application is the main concept for
 * CloudUnit : it composed by Server, Module and Metadata
 */
@Controller
@RequestMapping("/application")
public class ApplicationController implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	@Inject
	private ApplicationService applicationService;

	@Inject
	private AuthentificationUtils authentificationUtils;

	@Inject
	private DockerService dockerService;

	@Inject
	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * To verify if an application exists or not.
	 *
	 * @param applicationName
	 * @param serverName
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@ResponseBody
	@RequestMapping(value = "/verify/{applicationName}/{serverName}", method = RequestMethod.GET)
	public JsonResponse isValid(@PathVariable String applicationName, @PathVariable String serverName)
			throws ServiceException, CheckException {

		User user = authentificationUtils.getAuthentificatedUser();
		if (logger.isInfoEnabled()) {
			logger.info("applicationName:" + applicationName);
			logger.info("serverName:" + serverName);
		}

		CheckUtils.validateInput(applicationName, "check.app.name");
		CheckUtils.validateInput(serverName, "check.server.name");

		applicationService.checkCreate(user, applicationName);

		return new HttpOk();
	}

	/**
	 * CREATE AN APPLICATION
	 *
	 * @param input
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 * @throws InterruptedException
	 */
	@ResponseBody
	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public JsonResponse createApplication(@RequestBody JsonInput input)
			throws ServiceException, CheckException, InterruptedException {

		// validate the input
		input.validateCreateApp();

		// We must be sure there is no running action before starting new one
		User user = authentificationUtils.getAuthentificatedUser();
		authentificationUtils.canStartNewAction(user, null, Locale.ENGLISH);

		// CREATE AN APP
		applicationService.create(input.getApplicationName(), input.getServerName());

		return new HttpOk();
	}

	/**
	 * START AN APPLICATION
	 *
	 * @param input
	 *            {applicatioName:myApp-johndoe-admin}
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 * @throws InterruptedException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/restart", method = RequestMethod.POST)
	public JsonResponse restartApplication(@RequestBody JsonInput input)
			throws ServiceException, CheckException, InterruptedException {

		// validate the input
		input.validateStartApp();

		String applicationName = input.getApplicationName();
		User user = authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, applicationName);

		if (application != null && application.getStatus().equals(Status.PENDING)) {
			// If application is pending do nothing
			return new HttpErrorServer("application is pending. No action allowed.");
		}

		// We must be sure there is no running action before starting new one
		authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

		if (application.getStatus().equals(Status.START)) {
			applicationService.stop(application);
			applicationService.start(application);
		} else if (application.getStatus().equals(Status.STOP)) {
			applicationService.start(application);
		}

		return new HttpOk();
	}

	/**
	 * START AN APPLICATION
	 *
	 * @param input
	 *            {applicatioName:myApp-johndoe-admin}
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 * @throws InterruptedException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public JsonResponse startApplication(@RequestBody JsonInput input)
			throws ServiceException, CheckException, InterruptedException {

		// validate the input
		input.validateStartApp();

		String applicationName = input.getApplicationName();
		User user = authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, applicationName);

		if (application != null && application.getStatus().equals(Status.START)) {
			// If appliction is already start, we return the status
			return new HttpErrorServer("application already started");
		}

		// We must be sure there is no running action before starting new one
		authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

		// set the application in pending mode
		applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));

		applicationService.start(application);

		// wait for modules and servers starting
		applicationEventPublisher.publishEvent(new ApplicationStartEvent(application));

		return new HttpOk();
	}

	/**
	 * STOP a running application
	 *
	 * @param input
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public JsonResponse stopApplication(@RequestBody JsonInput input) throws ServiceException, CheckException {

		if (logger.isDebugEnabled()) {
			logger.debug(input.toString());
		}

		String name = input.getApplicationName();
		User user = authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, name);

		// We must be sure there is no running action before starting new one
		authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

		// set the application in pending mode
		applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));

		// stop the application
		applicationService.stop(application);

		applicationEventPublisher.publishEvent(new ApplicationStopEvent(application));

		return new HttpOk();
	}

	/**
	 * DELETE AN APPLICATION
	 *
	 * @param jsonInput
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/{applicationName}", method = RequestMethod.DELETE)
	public JsonResponse deleteApplication(JsonInput jsonInput) throws ServiceException, CheckException {

		jsonInput.validateRemoveApp();

		String applicationName = jsonInput.getApplicationName();
		User user = this.authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, applicationName);

		// We must be sure there is no running action before starting new one
		authentificationUtils.canStartDeleteApplicationAction(user, application, Locale.ENGLISH);

		try {
			// Application busy
			// set the application in pending mode
			applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));

			logger.info("delete application :" + applicationName);
			applicationService.remove(application, user);

		} catch (ServiceException e) {
			// set the application in pending mode
			applicationEventPublisher.publishEvent(new ApplicationFailEvent(application));
		}

		logger.info("Application " + applicationName + " is deleted.");

		return new HttpOk();
	}

	/**
	 * Return detail information about application
	 *
	 * @return
	 * @throws ServiceException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/{applicationName}", method = RequestMethod.GET)
	public Application detail(JsonInput jsonInput) throws ServiceException, CheckException {

		jsonInput.validateDetail();

		User user = authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, jsonInput.getApplicationName());
		return application;
	}

	/**
	 * Return the list of applications for an User
	 *
	 * @return
	 * @throws ServiceException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public List<Application> findAllByUser() throws ServiceException {
		User user = this.authentificationUtils.getAuthentificatedUser();
		List<Application> applications = applicationService.findAllByUser(user);

		logger.debug("Number of applications " + applications.size());
		return applications;
	}

	/**
	 * Deploy a web application
	 *
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@ResponseBody
	@RequestMapping(value = "/{applicationName}/deploy", method = RequestMethod.POST, consumes = {
			"multipart/form-data" })
	public JsonResponse deploy(@RequestPart("file") MultipartFile fileUpload, @PathVariable String applicationName,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServiceException, CheckException {

		logger.info("applicationName = " + applicationName + "file = " + fileUpload.getOriginalFilename());

		User user = authentificationUtils.getAuthentificatedUser();
		Application application = applicationService.findByNameAndUser(user, applicationName);

		// We must be sure there is no running action before starting new one
		authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        application = applicationService.deploy(fileUpload, application);

		String needRestart = dockerService.getEnv(application.getServer().getContainerID(),
				"CU_SERVER_RESTART_POST_DEPLOYMENT");
		if ("true".equalsIgnoreCase(needRestart)){
            // set the application in pending mode
            applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));
            applicationService.stop(application);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			applicationService.start(application);
            // wait for modules and servers starting
            applicationEventPublisher.publishEvent(new ApplicationStartEvent(application));
		}

		logger.info("--DEPLOY APPLICATION WAR ENDED--");
		return new HttpOk();
	}

	/**
	 * Return the list of containers for an application (module, server or
	 * tools)
	 *
	 * @param applicationName
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@ResponseBody
	@RequestMapping(value = "/{applicationName}/containers", method = RequestMethod.GET)
	public List<ContainerUnit> listContainer(@PathVariable String applicationName)
			throws ServiceException, CheckException {
		logger.debug("applicationName:" + applicationName);
		return applicationService.listContainers(applicationName);
	}

	/**
	 * Display env variables for a container
	 *
	 * @param applicationName
	 * @return
	 * @throws ServiceException
	 * @throws CheckException
	 */
	@CloudUnitSecurable
	@ResponseBody
	@RequestMapping(value = "/{applicationName}/container/{containerName}/env", method = RequestMethod.GET)
	public List<EnvUnit> displayEnv(@PathVariable String applicationName, @PathVariable String containerName)
			throws ServiceException, CheckException {
		List<EnvUnit> envUnits = null;
		try {
			User user = this.authentificationUtils.getAuthentificatedUser();
			String content = dockerService.execCommand(containerName,
					RemoteExecAction.GATHER_CU_ENV.getCommand() + " " + user.getLogin());
			logger.debug(content);
			envUnits = EnvUnitFactory.fromOutput(content);
		} catch (FatalDockerJSONException e) {
			throw new ServiceException(applicationName + ", " + containerName, e);
		}
		return envUnits;
	}
}