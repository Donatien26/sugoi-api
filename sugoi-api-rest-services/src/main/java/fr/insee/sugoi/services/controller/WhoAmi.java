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
package fr.insee.sugoi.services.controller;

import fr.insee.sugoi.services.services.PermissionService;
import fr.insee.sugoi.services.view.WhoamiView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhoAmi {

  @Autowired PermissionService permissionService;

  @GetMapping(value = "/whoami")
  @Operation(
      summary = "Get name and rights of the current log in user",
      description =
          "id represent the current id of the user,"
              + "readerRealm realm where user have only reading access, writerRealm realm where user have full right for writing,"
              + "passwordRealm realm where user can only perform password operations on users, and appManager represent realms where user can manage one or several application.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Request success",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = WhoamiView.class))
            })
      })
  public WhoamiView whoami(Authentication authentication) {
    WhoamiView res = new WhoamiView();
    res.setId(authentication.getName());
    res.setPasswordRealm(permissionService.getUserRealmPasswordManager());
    res.setReaderRealm(permissionService.getUserRealmReader());
    res.setWriterRealm(permissionService.getUserRealmWriter());
    res.setAppManager(permissionService.getUserRealmAppManager());
    res.setIsAdmin(permissionService.isAdmin());
    return res;
  }
}
