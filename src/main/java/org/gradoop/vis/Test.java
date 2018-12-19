package org.gradoop.vis;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.flink.util.GradoopFlinkConfig;

public class Test {
    public static void main(String[] args) {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        GradoopFlinkConfig gfc = GradoopFlinkConfig.createConfig(env);
        System.out.println(RequestHandler.class.getResource("/data/").getPath().toString() + "g1/Center");
    }
}
