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

package fr.treeptik.cloudunit.filters.explorer;

import fr.treeptik.cloudunit.dto.FileUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nicolas on 19/06/15.
 */
public class ApacheFilter
    implements ExplorerFilter {

    private final Logger logger = LoggerFactory.getLogger(ApacheFilter.class);

    @Override
    public boolean isValid(FileUnit fileUnit) {
        if (!fileUnit.isDir())
            return true;

        if (logger.isDebugEnabled()) {
            logger.debug("--> fileUnit : " + fileUnit.getBreadcrump());
        }

        String breadcrumb = fileUnit.getBreadcrump();
            if (   breadcrumb.startsWith("/cloudunit")
                    || breadcrumb.startsWith("/opt")
                || breadcrumb.startsWith("/etc")
                || breadcrumb.startsWith("/usr")
                || breadcrumb.startsWith("/var")) {
            return true;
        }

        return false;
    }

    @Override
    public void isRemovable(FileUnit fileUnit) {

        if (logger.isDebugEnabled()) {
            logger.debug("--> fileUnit : " + fileUnit.getBreadcrump());
        }

        String breadcrumb = fileUnit.getBreadcrump();
        if (breadcrumb.equalsIgnoreCase("/cloudunit")
            || breadcrumb.equalsIgnoreCase("/cloudunit/backup")
            || breadcrumb.equalsIgnoreCase("/cloudunit/tmp")) {
            fileUnit.removable(false);
        } else {
            fileUnit.removable(true);
        }
    }

    @Override
    public void isSafe(FileUnit fileUnit) {
        if (logger.isDebugEnabled()) {
            logger.debug("--> fileUnit : " + fileUnit.getBreadcrump());
        }

        String breadcrumb = fileUnit.getBreadcrump();
        if (breadcrumb.equalsIgnoreCase("/cloudunit")
            || breadcrumb.equalsIgnoreCase("/cloudunit/backup")
            || breadcrumb.equalsIgnoreCase("/cloudunit/appconf")
            || breadcrumb.equalsIgnoreCase("/cloudunit/binaries/lib")) {
            fileUnit.safe(true);
        } else {
            fileUnit.safe(false);
        }
    }
}
