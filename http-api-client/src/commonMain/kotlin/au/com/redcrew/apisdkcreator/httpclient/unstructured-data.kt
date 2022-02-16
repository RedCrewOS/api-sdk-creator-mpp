package au.com.redcrew.apisdkcreator.httpclient

/**
 * Data type that does not have a pre-defined data model/type definition.
 *
 * @typedef {String} UnstructuredData
 */
sealed class UnstructuredData {
    data class String(val data: kotlin.String): UnstructuredData()
}
