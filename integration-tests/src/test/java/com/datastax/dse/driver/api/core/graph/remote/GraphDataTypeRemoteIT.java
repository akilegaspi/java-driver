/*
 * Copyright DataStax, Inc.
 *
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
package com.datastax.dse.driver.api.core.graph.remote;

import com.datastax.dse.driver.api.core.graph.DseGraph;
import com.datastax.dse.driver.api.core.graph.GraphDataTypeITBase;
import com.datastax.dse.driver.api.core.graph.SampleGraphScripts;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.testinfra.DseRequirement;
import com.datastax.oss.driver.api.testinfra.ccm.CustomCcmRule;
import com.datastax.oss.driver.api.testinfra.session.SessionRule;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@DseRequirement(min = "5.0.3", description = "DSE 5.0.3 required for remote TinkerPop support")
public class GraphDataTypeRemoteIT extends GraphDataTypeITBase {

  private static CustomCcmRule ccmRule =
      CustomCcmRule.builder()
          .withDseWorkloads("graph")
          .withDseConfiguration(
              "graph.gremlin_server.scriptEngines.gremlin-groovy.config.sandbox_enabled", "false")
          .build();

  private static SessionRule<CqlSession> sessionRule =
      SessionRule.builder(ccmRule).withCreateGraph().build();

  @ClassRule public static TestRule chain = RuleChain.outerRule(ccmRule).around(sessionRule);

  @BeforeClass
  public static void setupSchema() {
    sessionRule.session().execute(ScriptGraphStatement.newInstance(SampleGraphScripts.ALLOW_SCANS));
    sessionRule.session().execute(ScriptGraphStatement.newInstance(SampleGraphScripts.MAKE_STRICT));
  }

  @Override
  public CqlSession session() {
    return sessionRule.session();
  }

  private final GraphTraversalSource g =
      DseGraph.g.withRemote(DseGraph.remoteConnectionBuilder(sessionRule.session()).build());

  @Override
  public Vertex insertVertexAndReturn(String vertexLabel, String propertyName, Object value) {
    return g.addV(vertexLabel).property(propertyName, value).next();
  }
}
