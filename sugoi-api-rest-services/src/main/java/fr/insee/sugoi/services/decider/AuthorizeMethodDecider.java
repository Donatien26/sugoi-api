/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.services.decider;

import fr.insee.sugoi.services.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("NewAuthorizeMethodDecider")
public class AuthorizeMethodDecider {

  @Value("${fr.insee.sugoi.api.enable.preauthorize:false}")
  private boolean enable;

  @Autowired PermissionService permissionService;

  public boolean isAtLeastReader(String realm, String userStorage) {
    System.out.println(enable);
    if (enable) {
      return permissionService.isAtLeastReader(realm, userStorage);
    }
    return true;
  }

  public boolean isAtLeastWriter(String realm, String userStorage) {
    if (enable) {
      return permissionService.isAtLeastWriter(realm, userStorage);
    }
    return true;
  }

  public boolean isAdmin() {
    if (enable) {
      permissionService.isAdmin();
    }
    return true;
  }
}
