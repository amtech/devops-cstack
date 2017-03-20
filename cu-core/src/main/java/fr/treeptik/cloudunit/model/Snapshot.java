package fr.treeptik.cloudunit.model;/*
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
public class Snapshot
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String tag;

    private String displayTag;

    private String fullTag;

    private String cuInstanceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String applicationName;

    private String applicationDisplayName;

    private String type;

    private String jvmRelease;

    private String jvmOptions;

    private Long jvmMemory;

    private String deploymentStatus;

    @ElementCollection
    @JsonIgnore
    private Map<String, String> digests;

    public Map<String, String> getDigests() {
        return digests;
    }

    public void setDigests(Map<String, String> digests) {
        this.digests = digests;
    }

    @ManyToOne
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ElementCollection
    @JsonIgnore
    private Map<String, ModuleConfiguration> appConfig;

    @ElementCollection
    @JsonIgnore
    private List<String> images;

    @ElementCollection
    private List<String> savedPorts;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public String getDisplayTag() {
        return displayTag;
    }

    @JsonIgnore
    public String getFullTag() {
        return fullTag;
    }

    public void setTag(String tag) {
        if (tag != null) {
            tag = tag.toLowerCase();
            tag = Normalizer.normalize(tag, Normalizer.Form.NFD);
            tag = tag.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            tag = tag.replaceAll("[^a-z0-9]", "");
        }
        this.tag = tag;
    }

    public void setDisplayTag(String displayTag) {
        this.displayTag = displayTag;
    }

    public void setFullTag(String fullTag) {
        if (fullTag != null) {
            fullTag = fullTag.toLowerCase();
            fullTag = Normalizer.normalize(fullTag, Normalizer.Form.NFD);
            fullTag = fullTag.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            fullTag = fullTag.replaceAll("[^a-z0-9-]", "");
        }
        this.fullTag = fullTag;
    }

    public String getCuInstanceName() {
        return cuInstanceName;
    }

    public void setCuInstanceName(String cuInstanceName) {
        this.cuInstanceName = cuInstanceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        applicationName = applicationName.toLowerCase();
        applicationName = Normalizer.normalize(applicationName, Normalizer.Form.NFD);
        applicationName = applicationName.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        this.applicationName = applicationName.replaceAll("[^a-z0-9]", "");
    }

    public String getApplicationDisplayName() { return applicationDisplayName; }

    public void setApplicationDisplayName(String applicationDisplayName) { this.applicationDisplayName = applicationDisplayName; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, ModuleConfiguration> getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(Map<String, ModuleConfiguration> appConfig) {
        this.appConfig = appConfig;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJvmRelease() {
        return jvmRelease;
    }

    public void setJvmRelease(String jvmRelease) {
        this.jvmRelease = jvmRelease;
    }

    public String getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(String jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public Long getJvmMemory() {
        return jvmMemory;
    }

    public void setJvmMemory(Long jvmMemory) {
        this.jvmMemory = jvmMemory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    public List<String> getSavedPorts() {
        return this.savedPorts;
    }

    public void setSavedPorts(List<String> savedPorts) {
        this.savedPorts = savedPorts;
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", type='" + type + '\'' +
                ", jvmRelease='" + jvmRelease + '\'' +
                ", jvmOptions='" + jvmOptions + '\'' +
                ", jvmMemory=" + jvmMemory +
                '}';
    }

}
