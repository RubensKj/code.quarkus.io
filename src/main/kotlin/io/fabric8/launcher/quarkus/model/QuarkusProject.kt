package io.fabric8.launcher.quarkus.model

import javax.validation.constraints.NotBlank
import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam


class QuarkusProject {
    @NotBlank
    @QueryParam("g")
    @DefaultValue("org.example")
    val groupId: String = ""

    @NotBlank
    @QueryParam("a")
    @DefaultValue("quarkus-app")
    val artifactId: String = "p"

    @NotBlank
    @QueryParam("v")
    @DefaultValue("0.0.1-SNAPSHOT")
    val version: String = ""

    @NotBlank
    @QueryParam("d")
    @DefaultValue("My application with Quarkus")
    val description: String = ""

    @NotBlank
    @QueryParam("c")
    @DefaultValue("org.example.quarkus-app.QuarkusApp")
    val className: String = ""

    @QueryParam("e")
    val dependencies: Set<String> = mutableSetOf()
}