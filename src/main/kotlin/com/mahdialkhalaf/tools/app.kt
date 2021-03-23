package com.mahdialkhalaf.tools

import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.rxkotlin.Observables
import javafx.application.Application
import javafx.scene.control.RadioButton
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleGroup
import tornadofx.*

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}

class App : tornadofx.App(View::class) {
}

class View : tornadofx.View("Gradle Groovy to Kotlin Dependency Migrator") {
    enum class Style {
        NAMED,
        COLON_SEPARATED
    }

    private var inputArea by singleAssign<TextArea>()
    private var outputArea by singleAssign<TextArea>()
    private var styleNamed by singleAssign<RadioButton>()
    private var styleColonSeparated by singleAssign<RadioButton>()
    private val styleRadioGroup = ToggleGroup()

    override val root = form {
        fieldset("Input") { inputArea = textarea { } }
        fieldset("Output") { outputArea = textarea { } }
        fieldset("Style") {
            styleNamed = radiobutton("Named", styleRadioGroup) { isSelected = true }
            styleColonSeparated = radiobutton("Colon Separated", styleRadioGroup)
        }
    }

    init {
        Observables.combineLatest(
            inputArea.textProperty().toObservable(),
            styleRadioGroup.selectedToggleProperty().toObservable()
        ).subscribe { textAndToggle ->
            val style = when (textAndToggle.second) {
                styleNamed -> {
                    Style.NAMED
                }
                styleColonSeparated -> {
                    Style.COLON_SEPARATED
                }
                else -> {
                    throw IllegalStateException()
                }
            }
            outputArea.text = textAndToggle.first.lines().joinToString("\n") { migrate(it, style) }
        }
    }


    private fun migrate(dependency: String, style: Style = Style.NAMED): String {
        // fromat similar to: implementation group: "com.example", name: "artifact", version: "1.0"
        val regex = Regex(
            "\\s*(?<configuration>\\w+)\\s+" +
                    "group:\\s+[\"'](?<group>.+)[\"'],\\s+" +
                    "name:\\s+[\"'](?<name>.+)[\"'],\\s+" +
                    "version:\\s+(?<version>\\S+)"
        )
        // format similar to: implementation "com.example:artifact:1.0"
        val regex2 = Regex(
            "\\s*(?<configuration>\\w+)\\s+" +
                    "(?<quote>[\"'])" +
                    "(?<group>.+):(?<name>.+):(?<version>.+)" +
                    "\\k<quote>"
        )
        if (!dependency.matches(regex) && !dependency.matches(regex2)) {
            return dependency
        }
        val groups = when {
            dependency.matches(regex) -> regex.matchEntire(dependency)?.groups ?: throw IllegalStateException()
            dependency.matches(regex2) -> regex2.matchEntire(dependency)?.groups ?: throw IllegalStateException()
            else -> throw IllegalStateException()
        }

        val configuration = when (groups["configuration"]?.value) {
            "compile" -> "implementation"
            "testCompile" -> "testImplementation"
            "api" -> "api"
            "testApi" -> "testApi"
            else -> groups["configuration"]?.value
        }
        return when (style) {
            Style.NAMED -> {
                "$configuration(group = \"${groups["group"]?.value}\", name = \"${groups["name"]?.value}\", version = \"${groups["version"]?.value}\")"
            }
            Style.COLON_SEPARATED -> {
                "$configuration(\"${groups["group"]?.value}:${groups["name"]?.value}:${groups["version"]?.value}\")"
            }
        }
    }
}
