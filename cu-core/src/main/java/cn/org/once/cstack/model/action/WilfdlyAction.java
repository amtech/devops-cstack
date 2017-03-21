package cn.org.once.cstack.model.action;/*
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

/*
 * LICENCE : CloudUnit is available under the Affero Gnu Public License GPL V3 : https://www.gnu.org/licenses/agpl-3.0.html
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

import cn.org.once.cstack.enums.JavaRelease;
import cn.org.once.cstack.model.Server;
import cn.org.once.cstack.model.Snapshot;

public class WilfdlyAction
    extends ServerAction {

    private static final long serialVersionUID = 1L;

    public WilfdlyAction(Server parent) {
        super(parent);
    }

    @Override
    public String getServerManagerPath() {
        return "";
    }

    @Override
    public String getServerManagerPort() {
        return "9990";
    }

    @Override
    public String getServerPort() {
        return "8080";
    }

    @Override
    public Snapshot cloneProperties(Snapshot snapshot) {
        snapshot.setType(parent.getImage().getName());
        snapshot.setJvmOptions(parent.getJvmOptions());
        snapshot.setJvmMemory(parent.getJvmMemory());
        return snapshot;
    }

    @Override
    public String cleanCommand() {
        return null;
    }

    @Override
    public String getLogLocation() {
        return "cloudunit/appconf/logs";
    }

    @Override
    public String getDefaultLogFile() { return "server.log"; };

    @Override
    public boolean hasDefaultPort() {
        return true;
    }

    @Override
    public String getDefaultJavaRelease() {
        return JavaRelease.Java8.getVersion();
    }

}