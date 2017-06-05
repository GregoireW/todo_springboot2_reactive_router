package com.example.demo.controller

import com.example.demo.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.HeadersBuilder
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

var seq: Int = 0

val todos = HashMap<Int, Todo>()

data class Todo(var id: Int = 0, var title: String = "", var completed: Boolean = false, var order: Int = -1) {
    // Needed because of jackson
    // Can also add jackson-kotlin, but need to update mapper. 1 line vs 15 ...
    @Suppress("unused")
    constructor() : this(0)

    @Suppress("unused")
    val url: String
        get() = "${Config.root}/$id"
}

fun <B : HeadersBuilder<B>> B.cors(): B = header("Access-Control-Allow-Origin", "*").
        header("Access-Control-Allow-Methods", "DELETE,GET,HEAD,PATCH,POST,PUT,PATCH").
        header("Access-Control-Allow-Headers", "accept,content-type")


@Configuration
class ApplicationRoutes {


    @Bean
    fun mainRouter() = router {
        OPTIONS("/").or(OPTIONS("/{id}")).invoke {
            ServerResponse.ok().cors().build()
        }
        GET("/", { ServerResponse.ok().cors().body(Flux.fromIterable(todos.values), Todo::class.java) })
        POST("/", {
            val m = it.bodyToMono(Todo::class.java).map({
                todo ->
                todo.id = seq++
                todos[todo.id] = todo
                todo
            })

            ServerResponse.ok().cors().body(m, Todo::class.java)
        })
        DELETE("/", { todos.clear(); ServerResponse.ok().cors().build() })

        GET("/{id}", {
            val id = it.pathVariable("id").toInt()
            if (!todos.contains(id)) {
                ServerResponse.notFound().build()
            } else {
                ServerResponse.ok().cors().body(Mono.just(todos[id]!!), Todo::class.java)
            }
        })
        DELETE("/{id}", {
            val id = it.pathVariable("id").toInt()
            todos.remove(id)
            ServerResponse.ok().cors().build()
        })
        PATCH("/{id}", {
            val id = it.pathVariable("id").toInt()

            if (!todos.contains(id)) {
                ServerResponse.notFound().build()
            }

            val m = it.bodyToMono(Todo::class.java).map { (_, title, completed, order) ->
                val old = todos[id]!!
                if (!title.isEmpty()) old.title = title
                if (completed) old.completed = true
                if (order > -1) old.order = order
                old
            }

            ServerResponse.ok().cors().body(m, Todo::class.java)

        })

    }

}

