/**
 * Created by htomaka on 05/01/16.
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

(function () {
  'use strict';
  angular.module ( 'webuiApp.tag', [] )
    .directive ( 'cuTag', CuTag );

  function CuTag () {
    return {
      restrict: 'E',
      template: [
      '<div class="cu__tag">',
        '<div class="cu__tag-content">',
          '<span>{{tag.name}}</span>',
        '</div>',
        '<div class="cu__tag-remove-container">',
          '<button type="button" class="cu__tag-remove" ng-click="tag.handleRemove($event, tag.index)"><i class="icon-whhg remove"></i></button>',
        '</div>',
      '</div>'
      ].join ( '' ),
      scope: {
        name:'=tag',
        index: '=tagIndex',
        onRemove: '&'
      },
      controller: [function(){
        this.handleRemove = function(event, index){
          event.stopPropagation();
          this.onRemove({event: event, index: index});
        };
      }],
      controllerAs: 'tag',
      bindToController: true
    }
  }
} ());
