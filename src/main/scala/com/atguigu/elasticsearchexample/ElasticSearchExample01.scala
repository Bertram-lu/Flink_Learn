package com.atguigu.elasticsearchexample

import java.util

import com.atguigu.datastreamapi.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.client.Requests

object ElasticSearchExample01 {

    def main(args: Array[String]): Unit = {

        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)

        val stream = env
            .addSource(new SensorSource)

        val httpHosts = new util.ArrayList[HttpHost]()
        httpHosts.add(new HttpHost("hadoop102" , 9200))
        httpHosts.add(new HttpHost("hadoop102" , 9200))
        httpHosts.add(new HttpHost("hadoop102" , 9200))

        val esSinkBuilder = new ElasticsearchSink.Builder[SensorReading](
            httpHosts,
            new ElasticsearchSinkFunction[SensorReading] {
                override def process(t: SensorReading,
                                     runtimeContext: RuntimeContext,
                                     requestIndexer: RequestIndexer): Unit = {
                    val json = new util.HashMap[String, String]()
                    json.put("data", t.toString)

                    val indexRequest = Requests
                        .indexRequest()
                        .index("sensor")
                        .`type`("readingData")
                        .source(json)
                }
            }
        )

        esSinkBuilder.setBulkFlushMaxActions(10)

        stream.addSink(esSinkBuilder.build())

        env.execute()
    }

}
