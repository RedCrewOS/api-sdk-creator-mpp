package au.com.redcrew.apisdkcreator.httpclient

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig

@Suppress("unused")
@ExperimentalKotest
class ProjectConfig : AbstractProjectConfig() {
    override var testCoroutineDispatcher = true
}
