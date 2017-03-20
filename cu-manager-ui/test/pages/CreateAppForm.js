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

/**
 * Created by htomaka on 27/03/15.
 */

var CreateAppForm = (function () {
  function CreateAppForm() {
    "use strict";
    this.formContainer = element(by.id('create-application'));
    this.createAppForm = element(by.id('create-application-form'));
    this.applicationNameInput = element(by.model('createApplication.applicationName'));
    this.dropdownToggle = this.createAppForm.element(by.css('.dropdown-toggle'));
    this.createBtn = element(by.id('create-btn'));
    this.errorMessage = element(by.binding('createApplication.message'));
    this.formatErrorMessage = element(by.css('.format'));
    this.spinner = this.formContainer.element(by.css('.spinner'));
    this.setApplicationName = function (name) {
      return this.applicationNameInput.sendKeys(name);
    };
    this.createApp = function (appName, serverChoice) {
      var self = this;
      self.setApplicationName(appName);
      self.dropdownToggle.click().then(function () {
        element(by.repeater('serverImage in createApplication.serverImages').row(serverChoice)).click()
          .then(function () {
            self.createBtn.click()
          })
      });
    }
  }

  return CreateAppForm;

})();

module.exports = CreateAppForm;





