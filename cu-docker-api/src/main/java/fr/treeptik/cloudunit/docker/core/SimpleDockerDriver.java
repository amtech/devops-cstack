/*
 * Copyright (c) 2015
 *
 * LICENCE : CloudUnit is available under the GNU Affero General Public License : https://gnu.org/licenses/agpl.html
 * but CloudUnit is licensed too under a standard commercial license.
 * Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 * If you are not sure whether the AGPL is right for you,
 * you can always test our software under the AGPL and inspect the source code before you contact us
 * about purchasing a commercial license.
 *
 * LEGAL TERMS : CloudUnit is a registered trademark of Treeptik and cannot be used to endorse
 * or promote products derived from this project without prior written permission from Treeptik.
 * Products or services derived from this software may not be called "CloudUnit"
 * nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 * For any questions, contact us : contact@treeptik.fr
 */

package fr.treeptik.cloudunit.docker.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import fr.treeptik.cloudunit.docker.model.Network;
import fr.treeptik.cloudunit.utils.NamingUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.treeptik.cloudunit.docker.builders.ImageBuilder;
import fr.treeptik.cloudunit.docker.model.DockerContainer;
import fr.treeptik.cloudunit.docker.model.Image;
import fr.treeptik.cloudunit.docker.model.Volume;
import fr.treeptik.cloudunit.dto.DockerResponse;
import fr.treeptik.cloudunit.exception.FatalDockerJSONException;
import fr.treeptik.cloudunit.exception.JSONClientException;
import fr.treeptik.cloudunit.utils.JSONClient;

public class SimpleDockerDriver implements DockerDriver {

    private static Logger logger = LoggerFactory.getLogger(SimpleDockerDriver.class);

    private JSONClient client;
    private ObjectMapper objectMapper;
    private Boolean isUnixSocket;
    private String host;
    private String mode;

    public SimpleDockerDriver(Boolean isUnixSocket, String mode, String host, String certPathDirectory) {
        this.isUnixSocket = isUnixSocket;
        this.mode = mode;
        if (isUnixSocket) {
            client = new JSONClient(isUnixSocket, "/var/run/docker.sock", null);
        } else {
            client = new JSONClient(isUnixSocket, host, certPathDirectory);
        }
        this.host = host;
        objectMapper = new ObjectMapper();
    }

    @Override
    public DockerResponse find(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host)
                    .setPath("/containers/" + container.getName() + "/json").build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for find a container request due to " + e.getMessage(), e);
        }

        return dockerResponse;
    }

    @Override
    public DockerResponse findAll() throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/containers/json").build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for find all containers request due to " + e.getMessage(), e);
        }

        return dockerResponse;
    }

    @Override
    public DockerResponse create(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/containers/create")
                    .setParameter("name", container.getName()).build();
            body = objectMapper.writeValueAsString(container.getConfig());
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (URISyntaxException | IOException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse start(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host)
                    .setPath("/containers/" + container.getName() + "/start").build();
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (Exception e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for start container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse stop(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host)
                    .setPath("/containers/" + container.getName() + "/stop").setParameter("t", "10").build();
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for stop container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse kill(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host)
                    .setPath("/containers/" + container.getName() + "/kill").build();
            dockerResponse = client.sendPost(uri, "", "application/json");
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for kill container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse remove(DockerContainer container) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/containers/" + container.getName())
                    .setParameter("v", "1").setParameter("force", "true").build();
            dockerResponse = client.sendDelete(uri, false);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for remove request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse findAnImage(Image image) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/images/" + image.getName() + "/json")
                    .build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for find a container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse commit(DockerContainer container, String tag, String repository)
            throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            DockerResponse response = findAnImage(
                    ImageBuilder.anImage().withName(container.getConfig().getImage() + ":" + tag).build());
            Image image = objectMapper.readValue(response.getBody(), Image.class);
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/commit")
                    .setParameter("container", container.getName()).setParameter("tag", tag)
                    .setParameter("repo", repository).build();
            dockerResponse = client.sendPost(uri, "", "application/json");
            if (dockerResponse.getStatus() == 201 && image != null) {
                removeImage(image);
            }
        } catch (Exception e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for commit request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse pull(String tag, String repository) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/images/create")
                    .setParameter("fromImage", repository).setParameter("tag", tag.toLowerCase()).build();
            dockerResponse = client.sendPostToRegistryHost(uri, "", "application/json");
            dockerResponse = client.sendPost(uri, "", "application/json");
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for pull request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse removeImage(Image image) throws FatalDockerJSONException {

        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/images/" + image.getId()).build();
            dockerResponse = client.sendDelete(uri, false);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for removeImage request due to " + e.getMessage(),
                    e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse createVolume(Volume volume) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/volumes/create").build();
            body = objectMapper.writeValueAsString(volume);
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (URISyntaxException | IOException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse findVolume(Volume volume) throws FatalDockerJSONException {
        URI uri = null;
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/volumes/" + volume.getName()).build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse removeVolume(Volume volume) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/volumes/" + volume.getName()).build();
            dockerResponse = client.sendDelete(uri, false);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for removeImage request due to " + e.getMessage(),
                    e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse createNetwork(Network network) throws FatalDockerJSONException, IOException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;

		/*
        check network name
		 */
        DockerResponse response = listNetworks();

        List<Network> networks = objectMapper.readValue(response.getBody(), new TypeReference<List<Network>>() {
        });
        if (networks.stream()
                .filter(n -> n.getName().equalsIgnoreCase(network.getName()))
                .findAny()
                .isPresent()) {
            throw new RuntimeException("this network already exists");
        }

        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/networks/create").build();
            body = objectMapper.writeValueAsString(network);
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (URISyntaxException | IOException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse findNetwork(Network network) throws FatalDockerJSONException {
        URI uri = null;
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/networks/" + network.getId()).build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse listNetworks() throws FatalDockerJSONException {
        URI uri = null;
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/networks").build();
            dockerResponse = client.sendGet(uri);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse connectToNetwork(Network network, String containerId) throws FatalDockerJSONException {
        URI uri = null;
        DockerResponse dockerResponse = null;
        String body = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/networks/" + network.getId() + "/connect").build();
            body = "{ \"Container\":\"" + containerId + "\" }";
            dockerResponse = client.sendPost(uri, body, "application/json");
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException(
                    "An error has occurred for create container request due to " + e.getMessage(), e);
        }
        return dockerResponse;
    }

    @Override
    public DockerResponse removeNetwork(Network network) throws FatalDockerJSONException {
        URI uri = null;
        String body = new String();
        DockerResponse dockerResponse = null;
        try {
            uri = new URIBuilder().setScheme(NamingUtils.getProtocolSocket(isUnixSocket, mode)).setHost(host).setPath("/networks/" + network.getId()).build();
            dockerResponse = client.sendDelete(uri, false);
        } catch (URISyntaxException | JSONClientException e) {
            StringBuilder contextError = new StringBuilder(256);
            contextError.append("uri : " + uri + " - ");
            contextError.append("request body : " + body + " - ");
            contextError.append("server response : " + dockerResponse);
            logger.error(contextError.toString());
            throw new FatalDockerJSONException("An error has occurred for removeImage request due to " + e.getMessage(),
                    e);
        }
        return dockerResponse;
    }

    public JSONClient getClient() {
        return client;
    }

    public void setClient(JSONClient client) {
        this.client = client;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
