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
package fr.insee.sugoi.store.file;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.List;
import java.util.Map;

public class FileReaderStore implements ReaderStore {

  public FileReaderStore(Map<String, String> generateConfig) {}

  public FileReaderStore() {}

  @Override
  public User getUser(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<User> searchUsers(
      String identifiant,
      String nomCommun,
      String description,
      String organisationId,
      String mail,
      PageableResult pageable,
      String typeRecherche,
      List<String> habilitations,
      String application,
      String role,
      String rolePropriete,
      String certificat) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Organization getOrganization(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<User> getUsersInGroup(String groupName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Organization searchOrganizations(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group getGroup(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Group> searchGroups(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    // TODO Auto-generated method stub
    return false;
  }
}
