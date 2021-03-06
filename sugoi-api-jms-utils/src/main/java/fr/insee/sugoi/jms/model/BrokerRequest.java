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
package fr.insee.sugoi.jms.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrokerRequest implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private UUID uuid;
  private String method;
  private Map<String, Object> methodParams = new HashMap<>();

  public BrokerRequest(String method, Map<String, Object> methodParams) {
    this.method = method;
    uuid = UUID.randomUUID();
    this.methodParams = methodParams;
  }

  public BrokerRequest() {
    uuid = UUID.randomUUID();
  }

  public String getMethod() {
    return this.method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, Object> getmethodParams() {
    return this.methodParams;
  }

  public void setmethodParams(String name, Object value) {
    this.methodParams.put(name, value);
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public String toString() {
    return "{uuid: "
        + uuid.toString()
        + " method: "
        + method
        + " params: "
        + methodParams.toString()
        + "}";
  }
}
