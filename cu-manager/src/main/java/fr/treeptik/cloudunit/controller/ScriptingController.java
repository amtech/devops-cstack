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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.treeptik.cloudunit.dto.*;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Script;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ScriptingService;
import fr.treeptik.cloudunit.service.UserService;
import fr.treeptik.cloudunit.utils.AuthentificationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Controller for Script execution coming from CLI Syntax
 */
@Controller
@RequestMapping("/scripting")
public class ScriptingController implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(ScriptingController.class);

	@Inject
	private ScriptingService scriptingService;

	@Inject
	private AuthentificationUtils authentificationUtils;

	@Inject
	private UserService userService;

	@RequestMapping(value = "/exec", method = RequestMethod.POST)
	public void scriptingExecute(@RequestBody ScriptRequest scriptRequest, HttpServletResponse response)
			throws ServiceException, CheckException, IOException, InterruptedException {
		logger.info("Execute");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			// We must be sure there is no running action before starting new
			// one
			this.authentificationUtils.canStartNewAction(null, null, Locale.ENGLISH);

			if (logger.isDebugEnabled()) {
				logger.debug("scriptRequestBody: " + scriptRequest.getScriptContent());
			}

			scriptingService.execute(scriptRequest.getScriptContent(), user.getLogin(), user.getPassword());
		} catch (ServiceException e) {
			throw e;
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
	public @ResponseBody JsonNode scriptingSave(@RequestBody ScriptRequest scriptRequest)
			throws ServiceException, IOException, CheckException {
		logger.info("Save");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			if (scriptRequest.getScriptName().isEmpty() || scriptRequest.getScriptContent().isEmpty())
				throw new CheckException("Name or content cannot be empty");

			Script script = new Script();

			List<Script> scripts = scriptingService.loadAllScripts();
			for (Script script1 : scripts) {
				if (script1.getTitle().equals(scriptRequest.getScriptName()))
					throw new CheckException("Script name already exists");
			}

			Date now = new Timestamp(Calendar.getInstance().getTimeInMillis());

			script.setCreationUserId(user.getId());
			script.setTitle(scriptRequest.getScriptName());
			script.setContent(scriptRequest.getScriptContent());
			script.setCreationDate(now);

			scriptingService.save(script);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("id", script.getId());
			((ObjectNode) rootNode).put("title", script.getTitle());
			((ObjectNode) rootNode).put("content", script.getContent());
			((ObjectNode) rootNode).put("creation_date", script.getCreationDate().toString());
			((ObjectNode) rootNode).put("creation_user", user.getFirstName() + " " + user.getLastName());

			return rootNode;
		} finally {
			authentificationUtils.allowUser(user);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public @ResponseBody JsonNode scriptingLoad(@PathVariable @RequestBody Integer id)
			throws ServiceException, JsonProcessingException {
		logger.info("Load");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			Script script = scriptingService.load(id);
			User user1 = userService.findById(script.getCreationUserId());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("id", script.getId());
			((ObjectNode) rootNode).put("title", script.getTitle());
			((ObjectNode) rootNode).put("content", script.getContent());
			((ObjectNode) rootNode).put("creation_date", script.getCreationDate().toString());
			((ObjectNode) rootNode).put("creation_user", user1.getFirstName() + " " + user1.getLastName());

			return rootNode;
		} finally {
			authentificationUtils.allowUser(user);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody ArrayNode scriptingLoadAll() throws ServiceException, JsonProcessingException {
		logger.info("Load All");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			List<Script> scripts = scriptingService.loadAllScripts();

			ObjectMapper mapper = new ObjectMapper();
			ArrayNode array = mapper.createArrayNode();

			for (Script script : scripts) {
				JsonNode rootNode = mapper.createObjectNode();
				User user1 = userService.findById(script.getCreationUserId());

				((ObjectNode) rootNode).put("id", script.getId());
				((ObjectNode) rootNode).put("title", script.getTitle());
				((ObjectNode) rootNode).put("content", script.getContent());
				((ObjectNode) rootNode).put("creation_date", script.getCreationDate().toString());
				((ObjectNode) rootNode).put("creation_user", user1.getFirstName() + " " + user1.getLastName());
				array.add(rootNode);
			}

			return array;
		} finally {
			authentificationUtils.allowUser(user);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public @ResponseBody JsonNode scriptingUpdate(@PathVariable @RequestBody Integer id,
			@RequestBody ScriptRequest scriptRequest) throws ServiceException, CheckException {
		logger.info("Edit");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			if (scriptRequest.getScriptName().isEmpty() || scriptRequest.getScriptContent().isEmpty())
				throw new CheckException("Name or content cannot be empty");

			Script script = scriptingService.load(id);

			List<Script> scripts = scriptingService.loadAllScripts();
			for (Script script1 : scripts) {
				if (script1.getTitle().equals(scriptRequest.getScriptName())
						&& !script1.getTitle().equals(script.getTitle()))
					throw new CheckException("Script name already exists");
			}

			Date now = new Timestamp(Calendar.getInstance().getTimeInMillis());

			script.setCreationDate(now);
			script.setTitle(scriptRequest.getScriptName());
			script.setContent(scriptRequest.getScriptContent());

			scriptingService.save(script);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("id", script.getId());
			((ObjectNode) rootNode).put("title", script.getTitle());
			((ObjectNode) rootNode).put("content", script.getContent());
			((ObjectNode) rootNode).put("creation_date", script.getCreationDate().toString());
			((ObjectNode) rootNode).put("creation_user", user.getFirstName() + " " + user.getLastName());

			return rootNode;
		} finally {
			authentificationUtils.allowUser(user);
		}
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public JsonResponse scriptingDelete(@PathVariable @RequestBody Integer id) throws ServiceException {
		logger.info("Delete");
		User user = authentificationUtils.getAuthentificatedUser();
		try {
			Script script = scriptingService.load(id);
			scriptingService.delete(script);
		} finally {
			authentificationUtils.allowUser(user);
		}
		return new HttpOk();
	}
}
