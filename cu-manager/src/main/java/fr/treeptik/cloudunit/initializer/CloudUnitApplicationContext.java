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

package fr.treeptik.cloudunit.initializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import fr.treeptik.cloudunit.config.EmailActiveCondition;
import fr.treeptik.cloudunit.docker.core.DockerCloudUnitClient;
import fr.treeptik.cloudunit.docker.core.SimpleDockerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@EnableAspectJAutoProxy
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"fr.treeptik.cloudunit.controller",
        "fr.treeptik.cloudunit.dao", "fr.treeptik.cloudunit.docker",
        "fr.treeptik.cloudunit.docker.model", "fr.treeptik.cloudunit.config",
        "fr.treeptik.cloudunit.exception", "fr.treeptik.cloudunit.model",
        "fr.treeptik.cloudunit.service", "fr.treeptik.cloudunit.service.impl",
        "fr.treeptik.cloudunit.utils", "fr.treeptik.cloudunit.aspects",
        "fr.treeptik.cloudunit.manager", "fr.treeptik.cloudunit.manager.impl",
        "fr.treeptik.cloudunit.schedule", "fr.treeptik.cloudunit.schedule.tasks",
        "fr.treeptik.cloudunit.logs"
})
@PropertySource({"classpath:/application.properties"})
@PropertySource({"classpath:/maven.properties"})
public class CloudUnitApplicationContext
        extends WebMvcConfigurerAdapter {

    // Allow for longer running Docker commands, esp. database scripts.
    private static final int READ_TIMEOUT_MILLIS = 1 * 60 * 1000;

    // Max file size
    private static final int MAX_UPLOAD_SIZE = 300 * 1000 * 1000;

    private final static Logger logger = LoggerFactory.getLogger(CloudUnitApplicationContext.class);

    @Value("${cloudunit.instance.name}")
    private String cuInstanceName;

    @PostConstruct
    public void getCuINstanceName() {
        logger.info("CloudUnit instance name: {}", cuInstanceName);
    }

    @Bean
    @Profile("vagrant")
    public static PropertySourcesPlaceholderConfigurer properties()
            throws Exception {
        String file = "application-vagrant.properties";
        PropertySourcesPlaceholderConfigurer pspc =
                new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(getResources(file));
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setLocalOverride(true);
        return pspc;
    }

    @Bean
    @Profile("production")
    public static PropertySourcesPlaceholderConfigurer propertiesForProduction()
            throws Exception {
        String file = "application-production.properties";
        PropertySourcesPlaceholderConfigurer pspc =
                new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(getResources(file));
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setLocalOverride(true);
        return pspc;
    }

    @Bean
    @Profile("vagrant-demo")
    public static PropertySourcesPlaceholderConfigurer propertiesForDemo()
            throws Exception {
        String file = "application-vagrant-demo.properties";
        PropertySourcesPlaceholderConfigurer pspc =
                new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(getResources(file));
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setLocalOverride(true);
        return pspc;
    }

    @Bean
    @Profile("integration")
    public static PropertySourcesPlaceholderConfigurer propertiesForIntegration()
            throws Exception {
        String file = "application-integration-local.properties";

        String envIntegration = System.getenv("CLOUDUNIT_JENKINS_CI");
        if ("true".equalsIgnoreCase(envIntegration)) {
            file = "application-integration.properties";
        }

        PropertySourcesPlaceholderConfigurer pspc =
                new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(getResources(file));
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setLocalOverride(true);
        return pspc;
    }

    @Bean
    @Profile("test")
    public static PropertySourcesPlaceholderConfigurer propertiesForTest()
            throws Exception {
        PropertySourcesPlaceholderConfigurer pspc =
                new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = new Resource[]
                {new ClassPathResource("application-test.properties")};
        pspc.setLocations(resources);
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setLocalOverride(true);
        return pspc;
    }

    @Bean
    public ViewResolver contentNegotiatingViewResolver() {
        logger.debug("Configuring the ContentNegotiatingViewResolver");
        ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
        List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();

        UrlBasedViewResolver urlBasedViewResolver = new UrlBasedViewResolver();
        urlBasedViewResolver.setViewClass(JstlView.class);
        urlBasedViewResolver.setSuffix(".html");
        viewResolvers.add(urlBasedViewResolver);

        viewResolver.setViewResolvers(viewResolvers);

        List<View> defaultViews = new ArrayList<View>();
        defaultViews.add(new MappingJackson2JsonView());
        viewResolver.setDefaultViews(defaultViews);

        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Hibernate4Module().enable(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION));
        messageConverter.setObjectMapper(mapper);
        return messageConverter;
    }

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        converters.add(jacksonMessageConverter());
        super.configureMessageConverters(converters);
    }

    @Bean
    public SessionLocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("message");
        return messageSource;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(MAX_UPLOAD_SIZE);
        return multipartResolver;
    }

    @Bean
    public DockerCloudUnitClient dockerCloudUnitClient(@Value("${docker.endpoint.mode}") String endpoint,
                                                       @Value("${docker.socket.location}") String dockerSocketLocation,
                                                       @Value("${docker.certs.dir.path:}") String certPathDirectory) {
        boolean useUnixSocket = endpoint.equalsIgnoreCase("unix");
        logger.info("Socket mode : " + (useUnixSocket ? "unix" : "tcp"));
        DockerCloudUnitClient dockerCloudUnitClient = new DockerCloudUnitClient();
        if (useUnixSocket) {
            dockerCloudUnitClient.setDriver(new SimpleDockerDriver(true, endpoint, dockerSocketLocation, null));
        } else {
            dockerCloudUnitClient.setDriver(new SimpleDockerDriver(false,  endpoint, dockerSocketLocation, certPathDirectory));
        }
        return dockerCloudUnitClient;
    }

    @Bean
    public DockerClient dockerClient(@Value("${docker.endpoint.mode}") String endpoint,
                                     @Value("${docker.socket.location}") String dockerSocketLocation,
                                     @Value("${docker.certs.dir.path}") String certPathDirectory) {
        com.spotify.docker.client.DockerClient dockerClient = null;
        boolean useUnixSocket = endpoint.equalsIgnoreCase("unix");
        boolean useTLS = endpoint.equalsIgnoreCase("https");
        logger.info("Socket mode : " + (useUnixSocket ? "unix" : "tcp"));
        logger.info("Socket TLS : " + (useTLS ? "yes" : "no"));
        try {
            if (useUnixSocket) {
                dockerClient = DefaultDockerClient.fromEnv().build();
            } else {
                if (useTLS) {
                    final DockerCertificates certs = new DockerCertificates(Paths.get(certPathDirectory));
                    dockerClient = DefaultDockerClient
                            .builder()
                            .uri("https://"+dockerSocketLocation).dockerCertificates(certs).build();
                } else {
                    dockerClient = DefaultDockerClient
                            .builder()
                            .uri("http://"+dockerSocketLocation).build();
                }
            }
        } catch (Exception e) {
            logger.error("cannot instance docker client : ", e);
        }
        return dockerClient;
    }

    @Bean
    @Conditional(value = EmailActiveCondition.class)
    public JavaMailSender mailSender(@Value("${email.host}") String host,
                                     @Value("${email.port}") Integer port,
                                     @Value("${email.protocol}") String protocol,
                                     @Value("${email.username}") String username,
                                     @Value("${email.password}") String password) throws IOException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setJavaMailProperties(javaMailProperties());
        return mailSender;
    }


    private Properties javaMailProperties() throws IOException {
        Properties properties = new Properties();
        return properties;
    }


    @Bean
    @Profile("integration")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    /**
     * Get Resources to load for CloudUnit Context.
     *
     * @param profileProperties The filename of the profile properties.
     * @return An array of Resource.
     * The array will have at least the profileProperties given by parameter, and eventually a custom
     * configuration file if found in the {@code $HOME/.cloudunit/} repertory.
     */
    private static Resource[] getResources(String profileProperties) {
        final File customFile = new File(System.getProperty("user.home") + "/.cloudunit/configuration.properties");
        Resource[] resources = null;

        if (customFile.exists()) {
            logger.warn("Custom file configuration found ! : {}", customFile.getAbsolutePath());

            resources =
                    new Resource[]
                            {
                                    new ClassPathResource(profileProperties),
                                    new FileSystemResource(customFile)
                            };
        } else {
            logger.warn(customFile.getAbsolutePath() + " is missing. Needed for production !");
            resources =
                    new Resource[]
                            {
                                    new ClassPathResource(profileProperties),
                            };
        }
        return resources;
    }

}
