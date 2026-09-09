package me.prokhorov.coinfolio.util

import java.util.Locale
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the format locale for one test and puts the old one back after it. Every function under
 * test reads the default locale through [localeSymbols]. Without this rule the results would
 * depend on the machine that runs the build.
 */
class DefaultLocaleRule(private val locale: Locale) : TestWatcher() {

    private lateinit var original: Locale

    override fun starting(description: Description) {
        original = Locale.getDefault(Locale.Category.FORMAT)
        Locale.setDefault(Locale.Category.FORMAT, locale)
    }

    override fun finished(description: Description) {
        Locale.setDefault(Locale.Category.FORMAT, original)
    }
}
