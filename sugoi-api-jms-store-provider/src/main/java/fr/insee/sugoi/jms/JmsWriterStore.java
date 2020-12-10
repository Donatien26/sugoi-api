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
package fr.insee.sugoi.jms;

import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.jms.writer.JmsWriter;
import fr.insee.sugoi.model.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JmsWriterStore implements WriterStore {

  @Autowired JmsWriter jmsWriter;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueRequestName;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueResponseName;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueUrgentRequestName;

  @Value("${fr.insee.sugoi.jms.queue.requests.name:}")
  private String queueUrgentResponseName;

  @Override
  public String deleteUser(String domain, String id) {
    Map<String, Object> params = new HashMap<>();
    params.put("domain", domain);
    params.put("id", id);
    jmsWriter.writeInQueue(queueRequestName, "deleteUser", params);
    return "done";
  }

  @Override
  public User createUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }
}
