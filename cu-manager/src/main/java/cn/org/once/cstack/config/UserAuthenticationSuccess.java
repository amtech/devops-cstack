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

package cn.org.once.cstack.config;

import java.util.Date;

import javax.inject.Inject;

import cn.org.once.cstack.model.User;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import cn.org.once.cstack.exception.ServiceException;
import cn.org.once.cstack.service.UserService;

@Component
public class UserAuthenticationSuccess implements ApplicationListener<AuthenticationSuccessEvent> {

	@Inject
	private UserService userService;

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		try {
			User user = userService.findByLogin(((UserDetails) event.getAuthentication().getPrincipal()).getUsername());
			user.setLastConnection(new Date());
			userService.update(user);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

}