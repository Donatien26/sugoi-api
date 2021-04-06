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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.model.SugoiEventTypeEnum;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.model.SearchType;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.User;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private SugoiEventPublisher sugoiEventPublisher;

  @Autowired private UserService userService;

  @Override
  public Group create(String realm, String appName, Group group) {
    group.setUsers(new ArrayList<>());
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.CREATE_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.GROUP, group),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
    return storeProvider.getWriterStore(realm).createGroup(appName, group);
  }

  @Override
  public void delete(String realm, String appName, String id) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.DELETE_GROUP,
        Map.ofEntries(Map.entry(EventKeysConfig.GROUP_ID, id)));
    storeProvider.getWriterStore(realm).deleteGroup(appName, id);
  }

  @Override
  public Group findById(String realm, String appName, String id) {
    if (id == null) {
      id = "";
    }
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_GROUP_BY_ID,
        Map.ofEntries(
            Map.entry(EventKeysConfig.GROUP_ID, id),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
    return storeProvider.getReaderStore(realm).getGroup(appName, id);
  }

  @Override
  public PageResult<Group> findByProperties(
      String realm, String appName, Group groupFilter, PageableResult pageableResult) {
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.FIND_GROUPS,
        Map.ofEntries(
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_FILTER, groupFilter)));
    return storeProvider
        .getReaderStore(realm)
        .searchGroups(appName, groupFilter, pageableResult, SearchType.AND.name());
  }

  @Override
  public void update(String realm, String appName, Group group) {
    group.setUsers(new ArrayList<>());
    sugoiEventPublisher.publishCustomEvent(
        realm,
        null,
        SugoiEventTypeEnum.UPDATE_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.GROUP, group),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName)));
    storeProvider.getWriterStore(realm).updateGroup(appName, group);
  }

  @Override
  public void addUserToGroup(String realm, String userId, String appName, String groupName) {
    User user = userService.findById(realm, null, userId);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE),
        SugoiEventTypeEnum.ADD_USER_TO_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.USER, userId),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_NAME, groupName)));
    storeProvider
        .getWriterStore(realm, (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE))
        .addUserToGroup(appName, groupName, userId);
  }

  @Override
  public void deleteUserFromGroup(String realm, String userId, String appName, String groupName) {
    User user = userService.findById(realm, null, userId);
    sugoiEventPublisher.publishCustomEvent(
        realm,
        (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE),
        SugoiEventTypeEnum.DELETE_USER_FROM_GROUP,
        Map.ofEntries(
            Map.entry(EventKeysConfig.USER, userId),
            Map.entry(EventKeysConfig.APPLICATION_NAME, appName),
            Map.entry(EventKeysConfig.GROUP_NAME, groupName)));
    storeProvider
        .getWriterStore(realm, (String) user.getMetadatas().get(EventKeysConfig.USERSTORAGE))
        .deleteUserFromGroup(appName, groupName, userId);
  }
}
