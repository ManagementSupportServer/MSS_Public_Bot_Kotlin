package jp.mss.MSS_Public_Bot_Kotlin.Utils.Object

import com.opencsv.CSVParserBuilder
import com.opencsv.enums.CSVReaderNullFieldIndicator
import jp.aoichaan0513.Kotlin_Utils.joinToString

class ParseUtil {
    companion object {

        private val parser = CSVParserBuilder().withSeparator(' ').withQuoteChar('"')
            .withFieldAsNull(CSVReaderNullFieldIndicator.NEITHER).build()

        @JvmStatic
        fun splitString(str: String): List<String> = parser.parseLine(str).toList()

        @JvmStatic
        @JvmOverloads
        fun splitString(collection: Collection<String>, separatorString: String = " ") =
            splitString(collection.joinToString("", separatorString, { it }))

        @JvmStatic
        @JvmOverloads
        fun splitString(array: Array<String>, separatorString: String = " ") =
            splitString(array.toList(), separatorString)
    }
}