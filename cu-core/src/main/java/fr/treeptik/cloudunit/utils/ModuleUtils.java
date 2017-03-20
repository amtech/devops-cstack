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

package fr.treeptik.cloudunit.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * TODO : reprendre pour factoriser les méthodes et les objets. Etendre Module
 * en fonction de la BDD
 */
public class ModuleUtils {

	/**
	 * Génére un couple login/motdepasse indépendant de la BDD
	 *
	 * @return
	 */
	public static String generateRamdomUser() {
		SecureRandom random = new SecureRandom();
		String username = "admin" + new BigInteger(130, random).toString(32).substring(2, 10);
		return username;
	}

	public static String generateRamdomPassword() {
		SecureRandom random = new SecureRandom();
		String password = new BigInteger(130, random).toString(32).substring(2, 10);
		return password;
	}

}
