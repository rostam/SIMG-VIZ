package org.gradoop.vis;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.GraphHead;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.Property;

import java.util.List;

/**
 * Converts a logical graph or a read JSON into a cytoscape-conform JSON.
 */

class CytoJSONBuilder {
  /**
   * Key for vertex, edge and graph id.
   */
  private static final String IDENTIFIER = "id";
  /**
   * Key for the type of the returned JSON, either graph or collection.
   */
  private static final String TYPE = "type";
  /**
   * Key for meta Json object.
   */
  private static final String META = "meta";
  /**
   * Key for data Json object.
   */
  private static final String DATA = "data";
  /**
   * Key for vertex, edge and graph label.
   */
  private static final String LABEL = "label";
  /**
   * Key for graph identifiers at vertices and edges.
   */
  private static final String GRAPHS = "graphs";
  /**
   * Key for properties of graphs, vertices and edges.
   */
  private static final String PROPERTIES = "properties";
  /**
   * Key for vertex identifiers at graphs.
   */
  private static final String VERTICES = "nodes";
  /**
   * Key for edge identifiers at graphs.
   */
  private static final String EDGES = "edges";
  /**
   * Key for edge source vertex id.
   */
  private static final String EDGE_SOURCE = "source";
  /**
   * Key for edge target vertex id.
   */
  private static final String EDGE_TARGET = "target";

  /**
   * Takes a logical graph and converts it into a cytoscape-conform JSON.
   *
   * @param graphHead the graph head
   * @param vertices  the vertices
   * @param edges     the edges
   * @return a cytoscape-conform JSON
   * @throws JSONException if the creation of the JSON fails
   */
  static String getJSON(GraphHead graphHead, List<Vertex> vertices, List<Edge> edges) throws
          JSONException {

    JSONObject returnedJSON = new JSONObject();
    returnedJSON.put(TYPE, "graph");

    JSONArray graphArray = new JSONArray();
    JSONObject graphObject = new JSONObject();
    JSONObject graphProperties = new JSONObject();
    graphObject.put(IDENTIFIER, graphHead.getId());
    graphObject.put(LABEL, graphHead.getLabel());
    if (graphHead.getProperties() != null) {
      for (Property prop : graphHead.getProperties()) {
        graphProperties.put(prop.getKey(), prop.getValue());
      }
    }
    graphObject.put(PROPERTIES, graphProperties);
    graphArray.put(graphObject);

    returnedJSON.put(GRAPHS, graphArray);

    //Vector<String> unqiueClusterId = new Vector<>();

    JSONArray vertexArray = new JSONArray();
    for (Vertex vertex : vertices) {
      JSONObject vertexObject = new JSONObject();
      JSONObject vertexData = new JSONObject();

      vertexData.put(IDENTIFIER, vertex.getId());
      vertexData.put(LABEL, vertex.getLabel());
      JSONObject pos = new JSONObject();
      JSONObject vertexProperties = new JSONObject();
      if (vertex.getProperties() != null) {
        for (Property prop : vertex.getProperties()) {
          String key = prop.getKey();
          if (key.equals("position")) {
            String value = prop.getValue().toString();
            pos.put("x", Double.parseDouble(value.substring(0, value.indexOf(","))));
            pos.put("y", Double.parseDouble(value.substring(value.indexOf(",") + 1)));
          } else {
            vertexProperties.put(prop.getKey(), prop.getValue());
          }
        }
      }
      vertexData.put(PROPERTIES, vertexProperties);
      vertexObject.put(DATA, vertexData);
      vertexObject.put("position", pos);
      vertexArray.put(vertexObject);
    }

//    for(String cl : unqiueClusterId) {
//        JSONObject vertexObject = new JSONObject();
//        JSONObject vertexData = new JSONObject();
//        JSONObject vertexProperties = new JSONObject();
//        vertexData.put(IDENTIFIER, cl);
//        vertexData.put(LABEL, "");
//        vertexData.put(PROPERTIES, vertexProperties);
//        vertexObject.put(DATA, vertexData);
//        vertexArray.put(vertexObject);
//    }
//
//    for(Tuple2<String,Integer> t : parents) {
//      JSONObject vertexObject = new JSONObject();
//      JSONObject vertexData = new JSONObject();
//      JSONObject vertexProperties = new JSONObject();
//      vertexData.put(IDENTIFIER, t.f0);
//      vertexData.put(LABEL,"");
//      vertexData.put(PROPERTIES, vertexProperties);
//      vertexObject.put(DATA, vertexData);
//      vertexArray.put(vertexObject);
//    }
    returnedJSON.put(VERTICES, vertexArray);

    JSONArray edgeArray = new JSONArray();
    for (Edge edge : edges) {
      JSONObject edgeObject = new JSONObject();
      JSONObject edgeData = new JSONObject();
      edgeData.put(EDGE_SOURCE, edge.getSourceId());
      edgeData.put(EDGE_TARGET, edge.getTargetId());
      edgeData.put(IDENTIFIER, edge.getId());
      edgeData.put(LABEL, edge.getLabel());
      JSONObject edgeProperties = new JSONObject();
      if (edge.getProperties() != null) {
        for (Property prop : edge.getProperties()) {
          edgeProperties.put(prop.getKey(), prop.getValue());
        }
      }
      edgeData.put(PROPERTIES, edgeProperties);
      edgeObject.put(DATA, edgeData);
      edgeArray.put(edgeObject);
    }

    returnedJSON.put(EDGES, edgeArray);

    return returnedJSON.toString();
  }
}
