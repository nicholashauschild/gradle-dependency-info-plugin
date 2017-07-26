package com.github.nicholashauschild.depinfo

import java.io.File

open class DepInfoExtension(
    var configuration: String = "runtime",
    var destinationDir: File? = null
)
