package au.com.redcrew.apisdkcreator.httpclient.gson

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig

@ExperimentalKotest
class ProjectConfig : AbstractProjectConfig() {
    override var testCoroutineDispatcher = true
}
