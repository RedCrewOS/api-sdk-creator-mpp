package au.com.redcrew.apisdkcreator.httpclient.gson

import au.com.redcrew.apisdkcreator.httpclient.UNMARSHALLING_ERROR_TYPE
import au.com.redcrew.apisdkcreator.httpclient.UnstructuredData
import com.google.gson.Gson
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

data class MyType(
    val x: Int,
    val y: List<String>
)

class GsonAdapterTest: DescribeSpec({
    describe("Gson adapter") {
        val gson = Gson()
        val obj = MyType(1, listOf("a", "b", "c"))
        val json = "{\"x\":1,\"y\":[\"a\",\"b\",\"c\"]}"

        describe("marshalling") {
            it("should marshall any object") {
                gsonMarshaller(gson)(obj).shouldBeRight(UnstructuredData.String(json))
            }
        }

        describe("unmarshalling") {
            val unmarshaller = gsonUnmarshaller(gson)(MyType::class)

            it("should deserialise any object") {
                unmarshaller(UnstructuredData.String(json)).shouldBeRight(obj)
            }

            it("should catch error deserialising object") {
                val result = unmarshaller(UnstructuredData.String("{")).shouldBeLeft()

                result.type.shouldBe(UNMARSHALLING_ERROR_TYPE)
            }
        }
    }
})
