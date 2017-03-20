package fr.treeptik.cloudunit.utils;

import fr.treeptik.cloudunit.enums.RemoteExecAction;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by nicolas on 26/09/2016.
 */
public class NamingUtilsTest {

    @org.junit.Test
    public void classicRootArchive1() throws Exception {
        String context = NamingUtils.getContext.apply("ROOT.war");
        assertThat(context, Matchers.equalTo("/"));
    }

    @org.junit.Test
    public void classicRootArchive2() throws Exception {
        String context = NamingUtils.getContext.apply("ROOT.WAR");
        assertThat(context, Matchers.equalTo("/"));
    }

    @org.junit.Test
    public void classicRootArchive3() throws Exception {
        String context = NamingUtils.getContext.apply("root.war");
        assertThat(context, Matchers.equalToIgnoringCase("/"));
    }

    @org.junit.Test
    public void classicArchive1() throws Exception {
        String context = NamingUtils.getContext.apply("helloworld.war");
        assertThat(context, Matchers.equalTo("/helloworld"));
    }

    @org.junit.Test
    public void classicArchive2() throws Exception {
        String context = NamingUtils.getContext.apply("HELLOWORLD.WAR");
        assertThat(context, Matchers.equalTo("/HELLOWORLD"));
    }

    @org.junit.Test
    public void containerServerNameEmptyNature() throws Exception {
        String context = NamingUtils.getContainerName("App123", "", "johndoe");
        assertThat(context, Matchers.equalTo("app123-johndoe"));
    }

    @org.junit.Test
    public void containerServerNameNullNature() throws Exception {
        String context = NamingUtils.getContainerName("App123", null, "johndoe");
        assertThat(context, Matchers.equalTo("app123-johndoe"));
    }

    @org.junit.Test
    public void containerModuleName() throws Exception {
        String context = NamingUtils.getContainerName("App123", "mysql", "johndoe");
        assertThat(context, Matchers.equalTo("app123-mysql-johndoe"));
    }

    @org.junit.Test
    public void containerModuleCompleteName() throws Exception {
        String context = NamingUtils.getContainerName("App123", "mysql-5-6", "johndoe");
        assertThat(context, Matchers.equalTo("app123-mysql-johndoe"));
    }

}