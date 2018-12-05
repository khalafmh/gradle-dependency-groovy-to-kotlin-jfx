package com.mahdialkhalaf.tools

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.application.Application
import javafx.scene.control.TextArea
import tornadofx.fieldset
import tornadofx.form
import tornadofx.singleAssign
import tornadofx.textarea

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}

class App : tornadofx.App(View::class) {
}

class View : tornadofx.View("Gradle Groovy to Kotlin Dependency Migrator") {
    var inputArea by singleAssign<TextArea>()
    var outputArea by singleAssign<TextArea>()

    override val root = form {
        fieldset("Input") { inputArea = textarea { } }
        fieldset("Output") { outputArea = textarea { } }
    }

    init {
        inputArea.textProperty().toObservable().subscribe { dependencies ->
            outputArea.text = dependencies.lines().joinToString("\n") { migrate(it) }
        }
    }


    private fun migrate(dependency: String): String {
        val regex = Regex("(?<scope>\\w+) group: '(?<group>.+)', name: '(?<name>.+)', version: '(?<version>.+)'")
        if (!dependency.matches(regex)) {
            return dependency
        }
        val values = regex.matchEntire(dependency)?.groups ?: throw IllegalStateException()
        val scope = when (values["scope"]?.value) {
            "compile" -> "implementation"
            "testCompile" -> "testImplementation"
            "api" -> "api"
            "testApi" -> "testApi"
            else -> values["scope"]?.value
        }
        return "$scope(group = \"${values["group"]?.value}\", name = \"${values["name"]?.value}\", version = \"${values["version"]?.value}\")"
    }
}