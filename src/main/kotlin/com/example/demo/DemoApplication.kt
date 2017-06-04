package com.example.demo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class DemoApplication

object Config{
    var root: String = "http://localhost:8080";
}

fun main(args: Array<String>) {
    if (args.size>0) Config.root=args[0]

    SpringApplication.run(DemoApplication::class.java, *args)
}
