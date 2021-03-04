package jp.mss.MSS_Public_Bot_Kotlin.Utils.Object

import jp.mss.MSS_Public_Bot_Kotlin.Main
import java.util.regex.Pattern

class CommandUtil {
    companion object {

        fun isCommandMatches(str: String) =
            Pattern.compile("^${Pattern.quote(Main.PREFIX)}([A-z0-9?])+").matcher(str).find()

        fun getCommand(str: String) = Main.commands.firstOrNull { it.isCommand(str) }
    }
}