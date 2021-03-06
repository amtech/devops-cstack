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

package cn.org.once.cstack.cli.utils;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * @author guillaume check and replace non alpha numerics chars
 */
public class AlphaNumericsCharactersCheckUtils {

    public static String convertToAlphaNumerics(String value, Integer countApp)
            throws UnsupportedEncodingException {

        value = new String(value.getBytes("ISO-8859-1"), "UTF-8");
        value = Normalizer.normalize(value, Form.NFD);
        value = value.replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "").replace(" ", "");

        if (value.equalsIgnoreCase("")) {
            value = "default" + countApp;
        }

        return value;

    }
}
