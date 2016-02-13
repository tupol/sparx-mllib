package com.lunatic.mlx.kddcup99.mllib.modgen

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * This is mainly useful to show the flow when starting from scratch.
  *
  * This is also useful when one want to run just a small part (I usually comment out the first 3 steps)
 */
object Main {

  def main(args: Array[String]) = {

    val applicationContext: ApplicationContext = new ClassPathXmlApplicationContext("/application-context.xml")

    // For now this is mainly a PoC for using Spring Framework with Scala and Spark in ML context
    val workflow = applicationContext.getBean("workflow").asInstanceOf[Seq[SparkRunnable]]

    DefaultSparkRunner(this.getClass.getName, args).run(workflow)

  }
}
