package jp.mss.MSS_Public_Bot_Kotlin

import jp.mss.MSS_Public_Bot_Kotlin.Commands.Command
import jp.mss.MSS_Public_Bot_Kotlin.Listeners.BotListener
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.ApplicationInfo
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.FileSystems
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

class Main {
    companion object {

        // 仮プレフィックス
        const val PREFIX = "m!"

        val CHARSET = Charsets.UTF_8
        val SPLIT_REGEX = Regex("\\s+")
        val FILE_SEPARATOR = FileSystems.getDefault().separator ?: "/"

        const val TEMPORARY_DIRECTORY_NAME = "tmp"
        val TEMPORARY_DIRECTORY = File(TEMPORARY_DIRECTORY_NAME)

        val LOGGER = LoggerFactory.getLogger(Main::class.java)

        // ------------------------------------------------------------- //

        // シャード、Bot情報、設定
        lateinit var instance: ShardManager
        lateinit var applicationInfo: ApplicationInfo

        // コマンド
        lateinit var commands: ArrayList<Command>
        lateinit var listeners: ArrayList<Any>

        // 非同期関係
        // スレッドプール
        lateinit var executorService: ExecutorService
        lateinit var scheduledExecutorService: ScheduledExecutorService

        @Throws(LoginException::class, IllegalArgumentException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            LOGGER.info("Starting Bot. Please wait...")

            if (!loadConfigs())
                exitProcess(0)

            executorService = Executors.newCachedThreadPool()
            scheduledExecutorService = Executors.newScheduledThreadPool(100)

            loadCommands()
            loadBot()

            scheduledExecutorService.schedule({
                LOGGER.info("GC Start...")
                System.gc()
                Runtime.getRuntime().gc()
                LOGGER.info("GC Completed.")
            }, 15, TimeUnit.MINUTES)
        }


        @Throws(LoginException::class)
        fun loadBot() {
            LOGGER.info("Loading Bot. Please wait...")

            instance = DefaultShardManagerBuilder.create(
                "SET_TOKEN", // botConfig.token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_VOICE_STATES,
                // GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS
            )
                // .setShardsTotal(botConfig.totalShards)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                // .setActivity(Activity.playing("Bot Starting. Please wait... | ${MainAPI.botVersion} (${MainAPI.botChannel}, Build ${MainAPI.botBuild})"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .setRawEventsEnabled(true)
                .setRequestTimeoutRetry(true)
                .setAutoReconnect(true)
                .setRelativeRateLimit(false)
                // .setAudioSendFactory(NativeAudioSendFactory())
                .build()

            instance.addEventListener(BotListener())
            instance.retrieveApplicationInfo().queue { applicationInfo = it }
            Runtime.getRuntime().addShutdownHook(ShutdownEvent())
        }

        fun unloadBot() {
            LOGGER.info("Unloading Bot. Please wait...")

            commands.forEach { instance.removeEventListener(it) }
            listeners.forEach { instance.removeEventListener(it) }

            for (jda in instance.shards)
                jda.shutdownNow()

            LOGGER.info("GC Start...")

            System.gc()
            Runtime.getRuntime().gc()

            LOGGER.info("GC Completed.")
        }

        private fun loadCommands() {
            LOGGER.info("Loading Commands. Please wait...")

            commands = arrayListOf(
            )

            LOGGER.info("${commands.size} Commands Loading Completed.")
        }

        fun loadListeners() {
            listeners = arrayListOf(
            )

            commands.forEach { instance.addEventListener(it) }
            listeners.filter { it is EventListener || it is ListenerAdapter }.forEach { instance.addEventListener(it) }
        }

        private fun loadConfigs(): Boolean {
            TEMPORARY_DIRECTORY.mkdir()

            val fileName = "config.json"
            val file = File(fileName)

            return true
        }


        private class ShutdownEvent : Thread() {
            override fun run() {
                LOGGER.info("Bot Shutdown. Please wait...")
                LOGGER.info("Bot Shutdown. Bye :)")
            }
        }
    }
}