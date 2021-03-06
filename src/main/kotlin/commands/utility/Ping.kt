package commands.utility

import commandhandler.CommandContext
import commands.base.BaseCommand
import net.dv8tion.jda.api.utils.TimeUtil
import type.CommandType.Utility

class Ping : BaseCommand(
    commandName = "ping",
    commandDescription = "Get bot's ping",
    commandType = Utility
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        ctx.message.replyMsg("Pinging...") { message ->
            message.editMessage("Pong! Took ${message.idLong.timeCreatedMillis() - ctx.message.idLong.timeCreatedMillis()}ms").queue {
                it.addReaction()
            }
        }
    }

    private fun Long.timeCreatedMillis(): Long = TimeUtil.getTimeCreated(this).toInstant().toEpochMilli()

}