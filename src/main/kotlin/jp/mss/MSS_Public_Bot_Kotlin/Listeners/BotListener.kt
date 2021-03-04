package jp.mss.MSS_Public_Bot_Kotlin.Listeners

import jp.mss.MSS_Public_Bot_Kotlin.Main
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.commons.collections4.ListUtils
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class BotListener : ListenerAdapter() {

    val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    var scheduledFuture: ScheduledFuture<*>? = null

    private var isLoaded = false
    private var restart = false

    override fun onReady(e: ReadyEvent) {
        val jda = e.jda

        Main.LOGGER.info("Sharding #${jda.shardInfo.shardId} loading completed. (${jda.shardInfo.shardId + 1} / ${Main.instance.shardsTotal})")

        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay({
            Main.LOGGER.info("Please wait until the loading is completed... (${Main.instance.shards.joinToString { "#${it.shardInfo.shardId} -> ${it.status}" }}, $isLoaded)")

            if (Main.instance.shards.all { it.status == JDA.Status.CONNECTED } && !isLoaded) {
                Main.LOGGER.info("All shards have been loaded. Please wait a moment for activation to complete... (Loaded: $isLoaded)")
                Main.instance.setPresence(
                    OnlineStatus.DO_NOT_DISTURB,
                    Activity.playing("Bot Starting Completed. Please wait...")
                )

                isLoaded = true
                restart = false

                ListUtils.partition(Main.instance.guilds, 100).forEachIndexed { i, guilds ->
                    for (guild in guilds) {
                        Main.scheduledExecutorService.schedule({
                            Main.LOGGER.info(
                                "Guild ${guild.name} (${guild.id}, ${
                                    guild.selfMember.hasPermission(
                                        Permission.MANAGE_SERVER
                                    )
                                }) ready!"
                            )
                        }, (i * 10).toLong(), TimeUnit.SECONDS)
                    }
                }

                /*
                // 5時間50分後に実行
                var restartMin = 10
                Main.scheduledExecutorService.scheduleWithFixedDelay({
                    Main.instance.setPresence(OnlineStatus.IDLE, Activity.playing("The bot restarts in ${restartMin--} minutes."))
                }, TimeUnit.DAYS.toMinutes(6) + TimeUnit.HOURS.toMinutes(23) + 50, 1, TimeUnit.MINUTES)

                // 6時間後に再起動
                Main.scheduledExecutorService.schedule({
                    if (restart) return@schedule
                    Main.LOGGER.info("Rebooting. Please wait a moment for the reboot to complete...")
                    Main.instance.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Bot restarted. Please wait..."))
                    restart = true
                }, 7, TimeUnit.DAYS)
                */

                Main.loadListeners()
                Runtime.getRuntime().gc()
                System.gc()

                Main.LOGGER.info("Activation complete. Finish the process.")

                scheduledFuture?.cancel(true)
                scheduledExecutorService.shutdownNow()
            }
        }, 0, 10, TimeUnit.SECONDS)
    }
}