package jp.mss.MSS_Public_Bot_Kotlin.Commands

import jp.aoichaan0513.JDA_Utils.*
import jp.mss.MSS_Public_Bot_Kotlin.Main
import jp.mss.MSS_Public_Bot_Kotlin.Utils.Object.CommandUtil
import jp.mss.MSS_Public_Bot_Kotlin.Utils.Object.ParseUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class Command(
    val name: String,
    val description: String = "No description.",
    val aliases: Array<String> = emptyArray(),
    val usages: Array<String> = emptyArray(),
    val userPermissions: Set<Permission> = emptySet(),
    val botPermissions: Set<Permission> = emptySet(),
) : ListenerAdapter() {

    protected abstract fun onCommand(e: MessageReceivedEvent, label: String, args: Array<String>)

    override fun onMessageReceived(e: MessageReceivedEvent) {
        val channel = e.channel
        val user = e.author
        val msg = e.message
        val content = msg.contentRaw

        // 仮プレフィックス
        val prefix = Main.PREFIX

        if (user.isBot || e.isWebhookMessage || !CommandUtil.isCommandMatches(content)
            || !isCommand(content.split(Main.SPLIT_REGEX)[0].substring(prefix.length))
        ) return

        val label = content.split(Main.SPLIT_REGEX)[0].substring(prefix.length)
        val args =
            if (content.split(Main.SPLIT_REGEX).size > 1)
                ParseUtil.splitString(content.substring("$prefix$label ".length)).toTypedArray()
            else
                emptyArray()

        GlobalScope.launch {
            try {
                if (channel.isGuildTextChannel) {
                    val guild = e.guild
                    val member = e.member ?: return@launch

                    if (userPermissions.isNotEmpty() && botPermissions.isEmpty()) {
                        // ユーザーの権限は1つ以上要求されていて、Botの権限は要求されていない場合
                        if (member.hasPermission(userPermissions) ||
                            member.hasPermission(e.textChannel, userPermissions)
                        ) {
                            onCommand(e, label, args)
                        } else {
                            channel.send("${"エラー".bold()} » ユーザーの権限が不足しています。")?.queue()
                        }
                    } else if (userPermissions.isEmpty() && botPermissions.isNotEmpty()) {
                        // ユーザーの権限は要求されてなく、Botの権限は1つ以上要求されている場合
                        if (guild.selfMember.hasPermission(botPermissions)) {
                            onCommand(e, label, args)
                        } else {
                            channel.send("${"エラー".bold()} » Botの権限が不足しています。")?.queue()
                        }
                    } else if (userPermissions.isNotEmpty() && botPermissions.isNotEmpty()) {
                        // ユーザー、Botどちらも権限が1つ以上要求されている場合
                        val userPermissionsResult = member.hasPermission(userPermissions) || member.hasPermission(
                            e.textChannel,
                            userPermissions
                        )
                        val botPermissionsResult =
                            guild.selfMember.hasPermission(botPermissions) || guild.selfMember.hasPermission(
                                e.textChannel,
                                botPermissions
                            )

                        if (userPermissionsResult && botPermissionsResult) {
                            onCommand(e, label, args)
                        } else {
                            if (!userPermissionsResult && botPermissionsResult) {
                                // ユーザーは要求されている権限を満たしてなく、Botは要求されている権限を満たしている場合
                                channel.send("${"エラー".bold()} » ユーザーの権限が不足しています。")?.queue()
                            } else if (userPermissionsResult && !botPermissionsResult) {
                                // ユーザーは要求されている権限を満たしており、Botは要求されている権限を満たしていない場合
                                channel.send("${"エラー".bold()} » Botの権限が不足しています。")?.queue()
                            } else {
                                // ユーザー、Botどちらも要求されている権限を満たしていない場合
                                channel.send("${"エラー".bold()} » ユーザーとBotの権限が不足しています。")?.queue()
                            }
                        }
                    } else {
                        onCommand(e, label, args)
                    }
                } else {
                    onCommand(e, label, args)
                }
            } catch (err: Exception) {
                // MainAPI.sendErrorMessage(err)
            }
        }
    }

    val command
        get() = this

    fun isCommand(str: String) = name.equals(str, true) || aliases.any { it.equals(str, true) }
}