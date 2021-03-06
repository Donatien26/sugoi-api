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
package fr.insee.sugoi.config;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.ldap.utils.LdapFilter;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.ldap.utils.mapper.RealmLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserStorageLdapMapper;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
/*
 * This class load the realm configuration from an ldap
 */
@ConditionalOnProperty(
    value = "fr.insee.sugoi.realm.config.type",
    havingValue = "ldap",
    matchIfMissing = false)
public class LdapRealmProviderDAOImpl implements RealmProvider {

  @Value("${fr.insee.sugoi.config.ldap.profils.url:}")
  private String url;

  @Value("${fr.insee.sugoi.config.ldap.profils.port:}")
  private int port;

  @Value("${fr.insee.sugoi.config.ldap.profils.branche:}")
  private String baseDn;

  @Value("${fr.insee.sugoi.store.defaultReader:}")
  private String defaultReader;

  @Value("${fr.insee.sugoi.store.defaultWriter:}")
  private String defaultWriter;

  @Value("${fr.insee.sugoi.config.ldap.profils.pattern:cn=Profil_{realm}_WebServiceLdap}")
  private String realmEntryPattern;

  @Value("${fr.insee.sugoi.default.app_managed_attribute_keys:}")
  private String defaultAppManagedAttributeKeyList;

  @Value("${fr.insee.sugoi.default.app_managed_attribute_patterns:}")
  private String defaultAppManagedAttributePatternList;

  @Value("${fr.insee.sugoi.config.ldap.default.sortKey:}")
  private String defaultSortKey;

  private static final Logger logger = LogManager.getLogger(LdapRealmProviderDAOImpl.class);

  private static LDAPConnectionPool ldapConnectionPool;

  @Override
  public Realm load(String realmName) {
    logger.info("Loading configuration from ldap://{}:{}/{}", url, port, baseDn);
    try {
      LDAPConnectionPool ldapConnection = initLdapPoolConnection();
      SearchResultEntry realmEntry =
          ldapConnection.getEntry(realmEntryPattern.replace("{realm}", realmName) + "," + baseDn);
      if (realmEntry != null) {
        logger.debug("Found entry {}", realmEntry.getDN());
        Realm realm = RealmLdapMapper.mapFromSearchEntry(realmEntry);
        if (realm.getReaderType() == null) {
          realm.setReaderType(defaultReader);
        }
        if (realm.getWriterType() == null) {
          realm.setWriterType(defaultWriter);
        }
        logger.debug("Parsing as realm {}", realm);
        realm.setUserStorages(loadUserStorages(realmEntry, ldapConnection));
        if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST) == null) {
          realm.addProperty(
              GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST, defaultAppManagedAttributeKeyList);
        }
        if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST)
            == null) {
          realm.addProperty(
              GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST,
              defaultAppManagedAttributePatternList);
        }
        realm.getProperties().putIfAbsent(LdapConfigKeys.SORT_KEY, defaultSortKey);
        return realm;
      }
      throw new RealmNotFoundException("Erreur lors du chargement du realm " + realmName);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Erreur lors du chargement du realm " + realmName);
    }
  }

  @Override
  public List<Realm> findAll() {
    logger.info("Loading realms configurations from ldap://{}:{}/{}", url, port, baseDn);
    try {
      LDAPConnectionPool ldapConnection = initLdapPoolConnection();
      SearchRequest searchRequest =
          new SearchRequest(
              baseDn, SearchScope.ONE, LdapFilter.create("(objectClass=*)"), "*", "+");
      SearchResult searchResult = ldapConnection.search(searchRequest);
      return searchResult.getSearchEntries().stream()
          .map(
              e -> {
                Realm realm = RealmLdapMapper.mapFromSearchEntry(e);
                if (realm.getReaderType() == null) {
                  realm.setReaderType(defaultReader);
                }
                if (realm.getWriterType() == null) {
                  realm.setWriterType(defaultWriter);
                }
                if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST)
                    == null) {
                  realm.addProperty(
                      GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST,
                      defaultAppManagedAttributeKeyList);
                }
                if (realm.getProperties().get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST)
                    == null) {
                  realm.addProperty(
                      GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST,
                      defaultAppManagedAttributePatternList);
                }
                realm.setUserStorages(loadUserStorages(e, ldapConnection));
                return realm;
              })
          .collect(Collectors.toList());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RealmNotFoundException("Impossible de charger les realms");
    }
  }

  private List<UserStorage> loadUserStorages(
      SearchResultEntry realmEntry, LDAPConnectionPool ldapConnection) {
    try {
      String baseName = realmEntry.getAttribute("cn").getValue();
      SearchResult userStoragesResult =
          ldapConnection.search(
              "cn=" + baseName + "," + baseDn,
              SearchScope.SUBORDINATE_SUBTREE,
              Filter.create("objectClass=*"));
      if (userStoragesResult.getEntryCount() > 0) {
        return userStoragesResult.getSearchEntries().stream()
            .map(
                searchEntry -> UserStorageLdapMapper.mapFromAttributes(searchEntry.getAttributes()))
            .collect(Collectors.toList());
      } else {
        return List.of(UserStorageLdapMapper.mapFromAttributes(realmEntry.getAttributes()));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createRealm(Realm realm) {
    try {
      LDAPConnectionPool ldapConnectionPool = initLdapPoolConnection();
      AddRequest addRequest =
          new AddRequest(
              getRealmDn(realm.getName()),
              RealmLdapMapper.mapToAttributes(realm, realmEntryPattern, baseDn));
      if (realm.getUserStorages().size() == 1) {
        UserStorageLdapMapper.mapToAttributes(realm.getUserStorages().get(0)).stream()
            .forEach(attribute -> addRequest.addAttribute(attribute));
        ldapConnectionPool.add(addRequest);
      } else {
        ldapConnectionPool.add(addRequest);
        for (UserStorage userStorage : realm.getUserStorages()) {
          AddRequest userStorageAddRequest =
              new AddRequest(
                  String.format("cn=%s,%s", userStorage.getName(), getRealmDn(realm.getName())),
                  UserStorageLdapMapper.mapToAttributes(userStorage));
          ldapConnectionPool.add(userStorageAddRequest);
        }
      }
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to create realm " + realm.getName(), e);
    }
  }

  @Override
  public void updateRealm(Realm realm) {
    try {
      LDAPConnectionPool ldapConnectionPool = initLdapPoolConnection();
      ModifyRequest modifyRequest;
      if (realm.getUserStorages().size() == 1) {
        List<Attribute> realmAttributes =
            RealmLdapMapper.mapToAttributes(realm, realmEntryPattern, baseDn);
        List<Attribute> userStorageAttributes =
            UserStorageLdapMapper.mapToAttributes(realm.getUserStorages().get(0));
        realmAttributes.addAll(userStorageAttributes);
        modifyRequest =
            new ModifyRequest(
                getRealmDn(realm.getName()),
                LdapUtils.convertAttributesToModifications(realmAttributes));
        ldapConnectionPool.modify(modifyRequest);
      } else {
        for (UserStorage userStorage : realm.getUserStorages()) {
          ModifyRequest userStorageModifyRequest =
              new ModifyRequest(
                  getUserStorageDn(userStorage.getName(), realm.getName()),
                  UserStorageLdapMapper.createMods(userStorage));
          ldapConnectionPool.modify(userStorageModifyRequest);
        }
        modifyRequest =
            new ModifyRequest(
                getRealmDn(realm.getName()),
                RealmLdapMapper.createMods(realm, realmEntryPattern, baseDn));
      }
      ldapConnectionPool.modify(modifyRequest);
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to update realm " + realm.getName(), e);
    }
  }

  @Override
  public void deleteRealm(String realmName) {
    try {
      LDAPConnectionPool ldapConnectionPool = initLdapPoolConnection();
      List<UserStorage> userStorages = load(realmName).getUserStorages();
      if (userStorages.size() > 1) {
        for (UserStorage userStorage : load(realmName).getUserStorages()) {
          ldapConnectionPool.delete(getUserStorageDn(userStorage.getName(), realmName));
        }
      }
      ldapConnectionPool.delete(getRealmDn(realmName));
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to delete realm " + realmName, e);
    }
  }

  private synchronized LDAPConnectionPool initLdapPoolConnection() {
    try {
      if (ldapConnectionPool == null
          || !ldapConnectionPool.getConnectionPoolName().equals(url + "-" + port)) {
        ldapConnectionPool = new LDAPConnectionPool(new LDAPConnection(url, port), 10);
        ldapConnectionPool.setConnectionPoolName(url + "-" + port);
        ldapConnectionPool.setCreateIfNecessary(true);
      }

      return ldapConnectionPool;
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to init ldap connection", e);
    }
  }

  private String getRealmDn(String realmName) {
    return realmEntryPattern.replace("{realm}", realmName) + "," + baseDn;
  }

  private String getUserStorageDn(String userStorageName, String realmName) {
    return String.format("cn=%s,%s", userStorageName, getRealmDn(realmName));
  }
}
