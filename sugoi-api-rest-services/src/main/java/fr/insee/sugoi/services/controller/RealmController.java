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

import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.model.Realm;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/v2", "/"})
@Tag(name = "Gestion des Realms")
public class RealmController {

  @Autowired ConfigService configService;

  @GetMapping(value = "/realms")
  public ResponseEntity<List<Realm>> getRealms(
      @RequestParam(name = "id", required = false) String id) {
    List<Realm> realms = new ArrayList<>();
    if (id != null) {
      realms.add(configService.getRealm(id));
    } else {
      realms.addAll(configService.getRealms());
    }
    return new ResponseEntity<List<Realm>>(realms, HttpStatus.OK);
  }

  @PostMapping(
      value = "/realms",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Realm> createRealm(@RequestBody Realm realm) {
    // TODO: process POST request
    return new ResponseEntity<Realm>(realm, HttpStatus.CREATED);
  }

  @PutMapping(
      value = "/realms/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Realm> updateRealm(
      @RequestBody Realm realm, @PathVariable("id") String id) {
    // TODO: process PUT request
    return new ResponseEntity<Realm>(realm, HttpStatus.OK);
  }

  @DeleteMapping(
      value = "/realms/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> deleteRealm(@PathVariable("id") String id) {
    return new ResponseEntity<String>(id, HttpStatus.OK);
  }
}
