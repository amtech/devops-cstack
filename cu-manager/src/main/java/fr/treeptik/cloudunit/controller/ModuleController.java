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

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

import fr.treeptik.cloudunit.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import fr.treeptik.cloudunit.aspects.CloudUnitSecurable;
import fr.treeptik.cloudunit.config.events.ApplicationPendingEvent;
import fr.treeptik.cloudunit.config.events.ApplicationStartEvent;
import fr.treeptik.cloudunit.dto.HttpOk;
import fr.treeptik.cloudunit.dto.JsonInput;
import fr.treeptik.cloudunit.dto.JsonResponse;
import fr.treeptik.cloudunit.dto.ModulePortResource;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Module;
import fr.treeptik.cloudunit.model.Status;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.service.ModuleService;
import fr.treeptik.cloudunit.utils.AuthentificationUtils;

@Controller
@RequestMapping("/module")
public class ModuleController implements Serializable {

    private static final long serialVersionUID = 1L;

    Locale locale = Locale.ENGLISH;

    private Logger logger = LoggerFactory.getLogger(ModuleController.class);

    @Inject
    private ModuleService moduleService;

    @Inject
    private ApplicationService applicationService;

    @Inject
    private AuthentificationUtils authentificationUtils;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Add a module to an existing application
     *
     * @param input
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @CloudUnitSecurable
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse addModule(@RequestBody JsonInput input) throws ServiceException, CheckException {
        // validate the input
        input.validateAddModule();

        String applicationName = input.getApplicationName();

        String imageName = input.getImageName();

        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);

        if (application == null) {
            throw new CheckException("Unknown application");
        }

        applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));

        try {
            moduleService.create(imageName, application, user);
            logger.info("--initModule " + imageName + " to " + applicationName + " successful--");
        } finally {
            applicationEventPublisher.publishEvent(new ApplicationStartEvent(application));

        }
        return new HttpOk();
    }

    /**
     * Add a module to an existing application
     *
     * @param id
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @CloudUnitSecurable
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/ports/{number}")
    @ResponseBody
    public JsonResponse publishPort(@PathVariable("id") Integer id,
        @PathVariable("number") String number,
    @RequestBody ModulePortResource request)
            throws ServiceException, CheckException {
        request.validatePublishPort();

        User user = authentificationUtils.getAuthentificatedUser();
        Module module = moduleService.findById(id);
        Application application = module.getApplication();

        applicationEventPublisher.publishEvent(new ApplicationPendingEvent(application));
        moduleService.publishPort(id, request.getPublishPort(), number, user);
        applicationEventPublisher.publishEvent(new ApplicationStartEvent(application));
        return new HttpOk();
    }

    /**
     * Remove a module to an existing application
     *
     * @param jsonInput
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @CloudUnitSecurable
    @RequestMapping(value = "/{applicationName}/{moduleName}", method = RequestMethod.DELETE)
    @ResponseBody
    public JsonResponse removeModule(JsonInput jsonInput) throws ServiceException, CheckException {

        // validate the input
        jsonInput.validateRemoveModule();

        String applicationName = jsonInput.getApplicationName();
        String moduleName = jsonInput.getModuleName();

        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);
        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, locale);
        Status previousApplicationStatus = application.getStatus();
        try {
            // Application occupée
            applicationService.setStatus(application, Status.PENDING);
            String containerName = moduleName;
            if (!moduleName.contains("-")) {
                containerName = NamingUtils.getContainerName(applicationName, moduleName, user.getLogin());
            }
            moduleService.remove(user, containerName, true, previousApplicationStatus);
            logger.info("-- removeModule " + applicationName + " to " + moduleName + " successful-- ");
        } catch (Exception e) {
            // Application en erreur
            logger.error(applicationName + " // " + moduleName, e);
        } finally {
            applicationService.setStatus(application, previousApplicationStatus);
        }

        return new HttpOk();
    }
    
    @RequestMapping(value = "/{moduleName}/run-script", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    public ResponseEntity<?> runScript(@PathVariable String moduleName, @RequestPart("file") MultipartFile file)
            throws ServiceException, CheckException {
        String result = moduleService.runScript(moduleName, file);
        
        return ResponseEntity.ok(result);
    }

}
