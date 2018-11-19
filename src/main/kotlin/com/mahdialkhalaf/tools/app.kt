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
        val regex = Regex("compile group: '(.+)', name: '(.+)', version: '(.+)'")
        if (!dependency.matches(regex)) {
            return dependency
        }
        regex.matchEntire(dependency)?.groupValues
        val values = regex.matchEntire(dependency)?.destructured?.toList() ?: throw IllegalStateException()
        return "implementation(group = \"${values[0]}\", name = \"${values[1]}\", version = \"${values[2]}\")"
    }
}